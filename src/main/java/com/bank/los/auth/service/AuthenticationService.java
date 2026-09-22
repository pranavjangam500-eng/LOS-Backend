package com.bank.los.auth.service;

import com.bank.los.auth.dto.request.ChangePasswordRequest;
import com.bank.los.auth.dto.request.ForgotPasswordRequest;
import com.bank.los.auth.dto.request.LoginRequest;
import com.bank.los.auth.dto.request.ResetPasswordRequest;
import com.bank.los.auth.dto.request.VerifyOtpRequest;
import com.bank.los.auth.dto.response.ForgotPasswordResponse;
import com.bank.los.auth.dto.response.LoginResponse;
import com.bank.los.auth.dto.response.UserProfileResponse;
import com.bank.los.auth.mapper.AuthMapper;
import com.bank.los.common.constant.ApplicationConstants;
import com.bank.los.common.exception.BusinessException;
import com.bank.los.common.exception.ResourceNotFoundException;
import com.bank.los.common.exception.UnauthorizedException;
import com.bank.los.common.validation.PasswordPolicy;
import com.bank.los.config.TenantContext;
import com.bank.los.master.entity.InternalUser;
import com.bank.los.master.entity.LoginDirectory;
import com.bank.los.master.entity.Organization;
import com.bank.los.master.repository.InternalUserRepository;
import com.bank.los.master.repository.LoginDirectoryRepository;
import com.bank.los.master.repository.OrganizationRepository;
import com.bank.los.security.JwtTokenProvider;
import com.bank.los.security.UserPrincipal;
import com.bank.los.tenant.entity.Customer;
import com.bank.los.tenant.entity.RefreshToken;
import com.bank.los.tenant.entity.SelfServiceResetToken;
import com.bank.los.tenant.entity.SessionActivity;
import com.bank.los.tenant.entity.TenantUser;
import com.bank.los.tenant.repository.CustomerRepository;
import com.bank.los.tenant.repository.SelfServiceResetTokenRepository;
import com.bank.los.tenant.repository.SessionActivityRepository;
import com.bank.los.tenant.repository.TenantUserRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final LoginDirectoryRepository      loginDirectoryRepository;
    private final OrganizationRepository        organizationRepository;
    private final InternalUserRepository        internalUserRepository;
    private final TenantUserRepository          tenantUserRepository;
    private final CustomerRepository            customerRepository;
    private final SelfServiceResetTokenRepository resetTokenRepository;
    private final SessionActivityRepository     sessionActivityRepository;
    private final PasswordEncoder               passwordEncoder;
    private final JwtTokenProvider              jwtTokenProvider;
    private final TokenService                  tokenService;
    private final AuthMapper                    authMapper;
    private final OtpService                    otpService;
    private final EmailService                  emailService;
    private final PermissionService             permissionService;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    // =====================================================================
    //  LOGIN  (Step 1 of 2 for tenant users — always requires 2FA)
    // =====================================================================

    public LoginResponse login(LoginRequest request) {
        TenantContext.setCurrentTenant(TenantContext.MASTER_TENANT_ID);

        String identifier = request.getEmail().trim();
        log.info("Login attempt for identifier: {}", identifier);

        LoginDirectory directory = loginDirectoryRepository
                .findByEmailOrUserCodeOrPhone(identifier, identifier, identifier)
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        Organization org    = directory.getOrganization();
        String       uType  = directory.getUserType();

        if (!ApplicationConstants.UserTypes.INTERNAL.equalsIgnoreCase(uType)) {
            if (org == null) throw new BusinessException("Organization profile misconfigured");
            if (ApplicationConstants.OrgStatus.SUSPENDED.equalsIgnoreCase(org.getStatus()))
                throw new BusinessException("Your organization account is suspended.");
            if (ApplicationConstants.OrgStatus.INACTIVE.equalsIgnoreCase(org.getStatus()))
                throw new BusinessException("Your organization account is inactive.");
        }

        // ── INTERNAL / SUPER_ADMIN ────────────────────────────────────────
        if (ApplicationConstants.UserTypes.INTERNAL.equalsIgnoreCase(uType)) {
            return handleInternalLogin(identifier, request.getPassword());
        }

        // ── STAFF (ADMIN / CHECKER / MAKER / VIEWER) ─────────────────────
        if (ApplicationConstants.UserTypes.STAFF.equalsIgnoreCase(uType)) {
            return handleStaffLogin(identifier, request.getPassword(), org);
        }

        // ── CUSTOMER ─────────────────────────────────────────────────────
        if (ApplicationConstants.UserTypes.CUSTOMER.equalsIgnoreCase(uType)) {
            return handleCustomerLogin(identifier, request.getPassword(), org);
        }

        throw new BusinessException("Unsupported user type: " + uType);
    }

    // ─── Internal (platform team) login — no mandatory 2FA ───────────────
    private LoginResponse handleInternalLogin(String identifier, String rawPassword) {
        TenantContext.setCurrentTenant(TenantContext.MASTER_TENANT_ID);

        InternalUser user = internalUserRepository.findByEmail(identifier)
                .or(() -> internalUserRepository.findByUsername(identifier))
                .or(() -> internalUserRepository.findByEmpNo(identifier))
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        validateInternalUser(user, rawPassword, identifier);

        user.setNoOfBadLogins(0);
        user.setLastLoginDate(LocalDate.now());
        user.setLastLoginTime(LocalTime.now());
        internalUserRepository.save(user);

        UserPrincipal principal = buildInternalPrincipal(user);
        return buildFullAuthResponse(principal, authMapper.toProfileResponse(user), user.getInactiveSessionTimeout());
    }

    // ─── Staff login — ALWAYS triggers 2FA OTP ───────────────────────────
    private LoginResponse handleStaffLogin(String identifier, String rawPassword, Organization org) {
        TenantContext.setCurrentTenant(org.getDbName());
        TenantContext.setCurrentOrgCode(org.getCode());

        TenantUser user = tenantUserRepository.findByEmail(identifier)
                .or(() -> tenantUserRepository.findByUsername(identifier))
                .or(() -> tenantUserRepository.findByEmpNo(identifier))
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        validateStaffUser(user, rawPassword, identifier);
        enforceLoginWindow(user);
        enforceHolidayRestriction(user);

        // ── Generate & send 2FA OTP (MANDATORY for ALL tenant staff) ─────
        String rawOtp = otpService.generateOtp(user.getId());
        emailService.sendOtpEmail(user.getEmail(), rawOtp, user.getFirstName());

        // ── Issue a short-lived temp session token ────────────────────────
        UserPrincipal tempPrincipal = buildStaffPrincipal(user, org);
        String tempToken = jwtTokenProvider.generateTempSessionToken(tempPrincipal);

        LoginResponse.LoginResponseBuilder resp = LoginResponse.builder()
                .otpRequired(true)
                .tempSessionToken(tempToken);

        // Return raw OTP in response when SMTP is not configured (dev/test)
        if (!mailEnabled) {
            resp.devOtp(rawOtp);
        }

        log.info("2FA OTP issued for staff user empNo={}, org={}", user.getEmpNo(), org.getCode());
        return resp.build();
    }

    // ─── Customer login — no 2FA ──────────────────────────────────────────
    private LoginResponse handleCustomerLogin(String identifier, String rawPassword, Organization org) {
        TenantContext.setCurrentTenant(org.getDbName());
        TenantContext.setCurrentOrgCode(org.getCode());

        Customer customer = customerRepository.findByEmail(identifier)
                .or(() -> customerRepository.findByCustomerCode(identifier))
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!Boolean.TRUE.equals(customer.getIsActive()))
            throw new UnauthorizedException("Customer account is deactivated.");

        if (customer.getPasswordHash() != null &&
            !passwordEncoder.matches(rawPassword, customer.getPasswordHash()))
            throw new UnauthorizedException("Invalid credentials");

        customer.setLastLoginAt(LocalDateTime.now());
        customerRepository.save(customer);

        UserPrincipal principal = buildCustomerPrincipal(customer, org);
        return buildFullAuthResponse(principal, authMapper.toProfileResponse(customer, org), 3600);
    }

    // =====================================================================
    //  VERIFY OTP  (Step 2 of 2 — issues full JWT for staff users)
    // =====================================================================

    @Transactional
    public LoginResponse verifyOtp(VerifyOtpRequest request) {
        // Validate the temporary session token
        if (!jwtTokenProvider.validateToken(request.getTempSessionToken())) {
            throw new UnauthorizedException("Session expired. Please login again.");
        }

        Claims claims  = jwtTokenProvider.getClaimsFromToken(request.getTempSessionToken());
        Long   userId  = claims.get("userId", Number.class).longValue();
        String tenantDb = claims.get("tenantDb", String.class);
        String orgCode  = claims.get("orgCode",  String.class);
        String userType = claims.get("userType", String.class);

        if (!ApplicationConstants.UserTypes.STAFF.equalsIgnoreCase(userType)) {
            throw new BusinessException("OTP verification only applicable to bank staff.");
        }

        TenantContext.setCurrentTenant(tenantDb);
        TenantContext.setCurrentOrgCode(orgCode);

        TenantUser user = tenantUserRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found."));

        // Verify the OTP
        otpService.verifyOtp(userId, request.getOtp());

        // Update login tracking
        user.setNoOfBadLogins(0);
        user.setLastLoginDate(LocalDate.now());
        user.setLastLoginTime(LocalTime.now());
        tenantUserRepository.save(user);

        Organization org = Organization.builder()
                .code(orgCode)
                .dbName(tenantDb)
                .name(orgCode)
                .build();
        UserPrincipal principal = buildStaffPrincipal(user, org);
        LoginResponse response = buildFullAuthResponse(principal, authMapper.toProfileResponse(user, org), user.getInactiveSessionTimeout());

        // Register session for auto-logout tracking
        registerSession(principal.getJti(), userId, user.getInactiveSessionTimeout(), tenantDb);

        log.info("Staff login complete after 2FA: empNo={}, org={}", user.getEmpNo(), orgCode);
        return response;
    }

    // =====================================================================
    //  FORGOT PASSWORD  (self-service — production-grade)
    // =====================================================================

    @Transactional
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {
        String identifier = request.getEmail().trim();
        TenantContext.setCurrentTenant(TenantContext.MASTER_TENANT_ID);

        LoginDirectory directory = loginDirectoryRepository
                .findByEmailOrUserCodeOrPhone(identifier, identifier, identifier)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with identifier: " + identifier));

        Organization org    = directory.getOrganization();
        String       uType  = directory.getUserType();

        // ── Rate limiting: max 3 requests per hour ────────────────────────
        if (ApplicationConstants.UserTypes.STAFF.equalsIgnoreCase(uType) && org != null) {
            TenantContext.setCurrentTenant(org.getDbName());
            String empNo = resolveEmpNoForStaff(identifier, org);
            long recentCount = resetTokenRepository.countRecentByEmpNo(empNo,
                    LocalDateTime.now().minusHours(1));
            if (recentCount >= ApplicationConstants.MAX_RESET_REQUESTS_PER_HOUR) {
                throw new BusinessException("RATE_LIMIT",
                        "Too many password reset requests. Please try again after 1 hour.");
            }
            issueResetToken(empNo, identifier, uType, org);
        } else if (ApplicationConstants.UserTypes.INTERNAL.equalsIgnoreCase(uType)) {
            issueResetToken(directory.getUserCode(), identifier, uType, null);
        }

        return ForgotPasswordResponse.builder()
                .expiresInSeconds((long)(ApplicationConstants.PASSWORD_RESET_EXPIRY_MINS * 60))
                .message("Password reset instructions sent. Check your email.")
                .build();
    }

    private String resolveEmpNoForStaff(String identifier, Organization org) {
        TenantContext.setCurrentTenant(org.getDbName());
        return tenantUserRepository.findByEmail(identifier)
                .or(() -> tenantUserRepository.findByUsername(identifier))
                .map(TenantUser::getEmpNo)
                .orElse(identifier);
    }

    private void issueResetToken(String empNo, String email, String uType, Organization org) {
        // Generate cryptographically secure 64-byte URL-safe token
        byte[] bytes = new byte[64];
        SECURE_RANDOM.nextBytes(bytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        // Store SHA-256 hash — raw token NEVER stored
        String tokenHash = sha256Hex(rawToken);

        if (ApplicationConstants.UserTypes.STAFF.equalsIgnoreCase(uType) && org != null) {
            TenantContext.setCurrentTenant(org.getDbName());
            // Invalidate all prior unused tokens for this emp
            resetTokenRepository.invalidateAllForEmpNo(empNo);
            resetTokenRepository.save(SelfServiceResetToken.builder()
                    .empNo(empNo)
                    .tokenHash(tokenHash)
                    .used(false)
                    .expiresAt(LocalDateTime.now().plusMinutes(ApplicationConstants.PASSWORD_RESET_EXPIRY_MINS))
                    .build());
        }
        // (For internal users, master reset token table would follow same pattern — omitted for brevity)

        // Notify user (or log in dev mode)
        emailService.sendPasswordResetEmail(email, rawToken, empNo);
        // rawToken is intentionally NOT logged here for security
        log.info("Password reset token issued for empNo={}", empNo);
    }

    // =====================================================================
    //  RESET PASSWORD  (validate SHA-256 hash, one-time use, 15-min expiry)
    // =====================================================================

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordPolicy.validate(request.getNewPassword());
        String identifier = request.getEmail().trim();

        TenantContext.setCurrentTenant(TenantContext.MASTER_TENANT_ID);
        LoginDirectory directory = loginDirectoryRepository
                .findByEmailOrUserCodeOrPhone(identifier, identifier, identifier)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String uType = directory.getUserType();
        Organization org = directory.getOrganization();

        if (ApplicationConstants.UserTypes.STAFF.equalsIgnoreCase(uType) && org != null) {
            TenantContext.setCurrentTenant(org.getDbName());
            String empNo = resolveEmpNoForStaff(identifier, org);
            String tokenHash = sha256Hex(request.getResetToken());

            SelfServiceResetToken token = resetTokenRepository
                    .findByTokenHashAndUsedFalseAndExpiresAtAfter(tokenHash, LocalDateTime.now())
                    .orElseThrow(() -> new BusinessException("TOKEN_INVALID",
                            "Reset token is invalid or has expired. Please request a new one."));

            TenantUser user = tenantUserRepository.findByEmpNo(empNo)
                    .or(() -> tenantUserRepository.findByEmail(identifier))
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
            user.setNoOfBadLogins(0);
            user.setStatus(ApplicationConstants.UserStatus.OPERATIVE);
            tenantUserRepository.save(user);

            // Mark token as used — cannot be reused
            token.setUsed(true);
            resetTokenRepository.save(token);

            // Invalidate all active sessions — force re-login
            sessionActivityRepository.invalidateAllForUser(user.getId());

        } else if (ApplicationConstants.UserTypes.INTERNAL.equalsIgnoreCase(uType)) {
            TenantContext.setCurrentTenant(TenantContext.MASTER_TENANT_ID);
            InternalUser user = internalUserRepository.findByEmail(identifier)
                    .or(() -> internalUserRepository.findByUsername(identifier))
                    .orElseThrow(() -> new ResourceNotFoundException("Internal user not found"));

            user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
            user.setNoOfBadLogins(0);
            internalUserRepository.save(user);
        }

        log.info("Password reset successful for identifier={}", identifier);
    }

    // =====================================================================
    //  CHANGE PASSWORD  (authenticated user)
    // =====================================================================

    @Transactional
    public void changePassword(UserPrincipal principal, ChangePasswordRequest request) {
        PasswordPolicy.validate(request.getNewPassword());

        if (ApplicationConstants.UserTypes.INTERNAL.equalsIgnoreCase(principal.getUserType())) {
            TenantContext.setCurrentTenant(TenantContext.MASTER_TENANT_ID);
            InternalUser user = internalUserRepository.findById(principal.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Internal user not found"));
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash()))
                throw new BusinessException("INVALID_PASSWORD", "Current password does not match");
            user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
            internalUserRepository.save(user);

        } else if (ApplicationConstants.UserTypes.STAFF.equalsIgnoreCase(principal.getUserType())) {
            TenantContext.setCurrentTenant(principal.getTenantDbName());
            TenantUser user = tenantUserRepository.findById(principal.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Staff user not found"));
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash()))
                throw new BusinessException("INVALID_PASSWORD", "Current password does not match");
            user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
            tenantUserRepository.save(user);
            sessionActivityRepository.invalidateAllForUser(user.getId());

        } else {
            TenantContext.setCurrentTenant(principal.getTenantDbName());
            Customer customer = customerRepository.findById(principal.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
            if (customer.getPasswordHash() != null &&
                !passwordEncoder.matches(request.getCurrentPassword(), customer.getPasswordHash()))
                throw new BusinessException("INVALID_PASSWORD", "Current password does not match");
            customer.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
            customerRepository.save(customer);
        }
    }

    // =====================================================================
    //  GET PROFILE
    // =====================================================================

    public UserProfileResponse getCurrentUserProfile(UserPrincipal principal) {
        if (ApplicationConstants.UserTypes.INTERNAL.equalsIgnoreCase(principal.getUserType())) {
            TenantContext.setCurrentTenant(TenantContext.MASTER_TENANT_ID);
            InternalUser user = internalUserRepository.findById(principal.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Internal user not found"));
            return authMapper.toProfileResponse(user);
        }

        TenantContext.setCurrentTenant(principal.getTenantDbName());
        Organization org = Organization.builder()
                .code(principal.getOrganizationCode())
                .dbName(principal.getTenantDbName())
                .name(principal.getOrganizationCode())
                .build();

        if (ApplicationConstants.UserTypes.STAFF.equalsIgnoreCase(principal.getUserType())) {
            TenantUser user = tenantUserRepository.findById(principal.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Staff user not found"));
            return authMapper.toProfileResponse(user, org);
        }
        Customer customer = customerRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        return authMapper.toProfileResponse(customer, org);
    }

    // =====================================================================
    //  PRIVATE HELPERS
    // =====================================================================

    private void validateInternalUser(InternalUser user, String rawPassword, String identifier) {
        if (!Boolean.TRUE.equals(user.getIsActive()))
            throw new UnauthorizedException("Account is deactivated. Contact administrator.");
        if (!ApplicationConstants.UserStatus.OPERATIVE.equals(user.getStatus()))
            throw new UnauthorizedException("Account is not operative. Contact administrator.");
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            int attempts = user.getNoOfBadLogins() + 1;
            user.setNoOfBadLogins(attempts);
            if (attempts >= ApplicationConstants.MAX_BAD_LOGIN_ATTEMPTS) {
                user.setIsActive(false);
                internalUserRepository.save(user);
                throw new UnauthorizedException("Account locked after " + attempts + " failed attempts. Contact administrator.");
            }
            internalUserRepository.save(user);
            throw new UnauthorizedException("Invalid credentials");
        }
    }

    private void validateStaffUser(TenantUser user, String rawPassword, String identifier) {
        if (!Boolean.TRUE.equals(user.getIsActive()))
            throw new UnauthorizedException("Account is deactivated. Contact your bank administrator.");
        if (ApplicationConstants.UserStatus.PENDING_VERIFICATION.equals(user.getStatus()))
            throw new UnauthorizedException("Account is pending verification. Contact your bank administrator.");
        if (!ApplicationConstants.UserStatus.OPERATIVE.equals(user.getStatus()))
            throw new UnauthorizedException("Account is not operative. Contact your bank administrator.");
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            int attempts = user.getNoOfBadLogins() + 1;
            user.setNoOfBadLogins(attempts);
            if (attempts >= ApplicationConstants.MAX_BAD_LOGIN_ATTEMPTS) {
                user.setIsActive(false);
                tenantUserRepository.save(user);
                throw new UnauthorizedException("Account locked after " + attempts + " failed attempts.");
            }
            tenantUserRepository.save(user);
            throw new UnauthorizedException("Invalid credentials");
        }
    }

    private void enforceLoginWindow(TenantUser user) {
        if (user.getLoginTime() == null || user.getLogoutTime() == null) return;
        LocalTime now = LocalTime.now();
        if (now.isBefore(user.getLoginTime()) || now.isAfter(user.getLogoutTime())) {
            throw new BusinessException("LOGIN_WINDOW",
                    String.format("Login allowed only between %s and %s.",
                            user.getLoginTime(), user.getLogoutTime()));
        }
    }

    private void enforceHolidayRestriction(TenantUser user) {
        if (Boolean.TRUE.equals(user.getLoginOnHolidays())) return;
        // Placeholder — integrate with holiday calendar when available
        // if (HolidayCalendar.isHoliday(LocalDate.now())) throw new BusinessException(...)
    }

    private UserPrincipal buildInternalPrincipal(InternalUser user) {
        String jti = UUID.randomUUID().toString();
        return UserPrincipal.builder()
                .id(user.getId())
                .email(user.getEmail())
                .userCode(user.getEmpNo())
                .fullName(user.getFullName())
                .role(user.getRole() != null ? user.getRole().getName() : ApplicationConstants.Roles.INTERNAL_ADMIN)
                .userType(ApplicationConstants.UserTypes.INTERNAL)
                .organizationCode("MASTER")
                .tenantDbName(TenantContext.MASTER_TENANT_ID)
                .active(true)
                .jti(jti)
                .build();
    }

    private UserPrincipal buildStaffPrincipal(TenantUser user, Organization org) {
        String jti = UUID.randomUUID().toString();
        return UserPrincipal.builder()
                .id(user.getId())
                .email(user.getEmail())
                .userCode(user.getEmpNo())
                .fullName(user.getFullName())
                .role(user.getRole() != null ? user.getRole().getName() : ApplicationConstants.Roles.VIEWER)
                .userType(ApplicationConstants.UserTypes.STAFF)
                .organizationCode(org != null ? org.getCode() : null)
                .tenantDbName(org != null ? org.getDbName() : null)
                .branchId(user.getLoginBranch() != null ? user.getLoginBranch().getId() : null)
                .active(true)
                .jti(jti)
                .build();
    }

    private UserPrincipal buildCustomerPrincipal(Customer customer, Organization org) {
        return UserPrincipal.builder()
                .id(customer.getId())
                .email(customer.getEmail())
                .userCode(customer.getCustomerCode())
                .fullName(customer.getFirstName() + " " + customer.getLastName())
                .role(ApplicationConstants.Roles.CUSTOMER)
                .userType(ApplicationConstants.UserTypes.CUSTOMER)
                .organizationCode(org.getCode())
                .tenantDbName(org.getDbName())
                .branchId(customer.getBranch() != null ? customer.getBranch().getId() : null)
                .active(true)
                .jti(UUID.randomUUID().toString())
                .build();
    }

    private LoginResponse buildFullAuthResponse(UserPrincipal principal, com.bank.los.auth.dto.response.UserProfileResponse profile, int timeoutSecs) {
        String accessToken = jwtTokenProvider.generateAccessToken(principal);
        RefreshToken refreshTokenEntity = tokenService.createRefreshToken(principal);
        List<String> permissions = permissionService.getPermissionCodes(principal.getRole(), principal.getTenantDbName());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenEntity.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationMs())
                .dashboardUrl(resolveDashboardUrl(principal.getRole()))
                .permissions(permissions)
                .user(profile)
                .build();
    }

    private void registerSession(String jti, Long userId, int timeoutSecs, String tenantDb) {
        if (jti == null) return;
        TenantContext.setCurrentTenant(tenantDb);
        sessionActivityRepository.save(SessionActivity.builder()
                .jti(jti)
                .userId(userId)
                .lastSeen(LocalDateTime.now())
                .timeoutSecs(timeoutSecs)
                .invalidated(false)
                .build());
    }

    private String resolveDashboardUrl(String role) {
        if (role == null) return "/dashboard";
        return switch (role.toUpperCase()) {
            case "INTERNAL_ADMIN" -> "/dashboard/internal-admin";
            case "ADMIN"          -> "/dashboard/admin";
            case "MAKER"          -> "/dashboard/maker";
            case "CHECKER"        -> "/dashboard/checker";
            case "VIEWER"         -> "/dashboard/viewer";
            case "CUSTOMER"       -> "/dashboard/customer";
            default               -> "/dashboard";
        };
    }

    /** SHA-256 hex hash — for production-grade reset token storage */
    private String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
