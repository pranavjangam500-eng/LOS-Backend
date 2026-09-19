package com.bank.los.auth.service;

import com.bank.los.auth.dto.request.ChangePasswordRequest;
import com.bank.los.auth.dto.request.LoginRequest;
import com.bank.los.auth.dto.response.LoginResponse;
import com.bank.los.auth.dto.response.UserProfileResponse;
import com.bank.los.auth.mapper.AuthMapper;
import com.bank.los.common.constant.ApplicationConstants;
import com.bank.los.common.exception.BusinessException;
import com.bank.los.common.exception.ResourceNotFoundException;
import com.bank.los.common.exception.UnauthorizedException;
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
import com.bank.los.tenant.entity.TenantUser;
import com.bank.los.tenant.repository.CustomerRepository;
import com.bank.los.tenant.repository.TenantUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final LoginDirectoryRepository loginDirectoryRepository;
    private final OrganizationRepository organizationRepository;
    private final InternalUserRepository internalUserRepository;
    private final TenantUserRepository tenantUserRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;
    private final AuthMapper authMapper;

    public LoginResponse login(LoginRequest request) {
        // Always query Master DB first for global routing
        TenantContext.setCurrentTenant(TenantContext.MASTER_TENANT_ID);

        String identifier = request.getEmail().trim();
        log.info("Login attempt for identifier: {}", identifier);

        // 1. Look up routing in Master Login Directory
        LoginDirectory directory = loginDirectoryRepository.findByEmailOrUserCodeOrPhone(identifier, identifier, identifier)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        Organization org = directory.getOrganization();
        String userType = directory.getUserType();

        // 2. Validate organization status for non-internal users
        if (!ApplicationConstants.UserTypes.INTERNAL.equalsIgnoreCase(userType)) {
            if (org == null) {
                throw new BusinessException("User organization profile is misconfigured");
            }
            if (ApplicationConstants.OrgStatus.SUSPENDED.equalsIgnoreCase(org.getStatus())) {
                throw new BusinessException("Your organization account is suspended. Please contact platform support.");
            }
            if (ApplicationConstants.OrgStatus.INACTIVE.equalsIgnoreCase(org.getStatus())) {
                throw new BusinessException("Your organization account is inactive.");
            }
        }

        // 3. Process Login based on user type
        UserPrincipal userPrincipal;
        UserProfileResponse userProfile;

        if (ApplicationConstants.UserTypes.INTERNAL.equalsIgnoreCase(userType)) {
            TenantContext.setCurrentTenant(TenantContext.MASTER_TENANT_ID);
            InternalUser internalUser = internalUserRepository.findByEmail(identifier)
                    .or(() -> internalUserRepository.findByUserCode(identifier))
                    .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

            if (!Boolean.TRUE.equals(internalUser.getIsActive())) {
                throw new UnauthorizedException("Your account is deactivated. Please contact administrator.");
            }

            if (!passwordEncoder.matches(request.getPassword(), internalUser.getPasswordHash())) {
                internalUser.setFailedLoginAttempts(internalUser.getFailedLoginAttempts() + 1);
                internalUserRepository.save(internalUser);
                throw new UnauthorizedException("Invalid email or password");
            }

            // Reset failed login attempts and update last login
            internalUser.setFailedLoginAttempts(0);
            internalUser.setLastLoginAt(LocalDateTime.now());
            internalUserRepository.save(internalUser);

            String roleName = internalUser.getRole() != null ? internalUser.getRole().getName() : ApplicationConstants.Roles.INTERNAL_ADMIN;

            userPrincipal = UserPrincipal.builder()
                    .id(internalUser.getId())
                    .email(internalUser.getEmail())
                    .userCode(internalUser.getUserCode())
                    .fullName(internalUser.getFirstName() + " " + internalUser.getLastName())
                    .role(roleName)
                    .userType(ApplicationConstants.UserTypes.INTERNAL)
                    .organizationCode("MASTER")
                    .tenantDbName(TenantContext.MASTER_TENANT_ID)
                    .active(true)
                    .build();

            userProfile = authMapper.toProfileResponse(internalUser);

        } else if (ApplicationConstants.UserTypes.STAFF.equalsIgnoreCase(userType)) {
            TenantContext.setCurrentTenant(org.getDbName());
            TenantContext.setCurrentOrgCode(org.getCode());

            TenantUser staffUser = tenantUserRepository.findByEmail(identifier)
                    .or(() -> tenantUserRepository.findByUserCode(identifier))
                    .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

            if (!Boolean.TRUE.equals(staffUser.getIsActive())) {
                throw new UnauthorizedException("Your staff account is deactivated. Please contact your bank administrator.");
            }

            if (!passwordEncoder.matches(request.getPassword(), staffUser.getPasswordHash())) {
                staffUser.setFailedLoginAttempts(staffUser.getFailedLoginAttempts() + 1);
                tenantUserRepository.save(staffUser);
                throw new UnauthorizedException("Invalid email or password");
            }

            staffUser.setFailedLoginAttempts(0);
            staffUser.setLastLoginAt(LocalDateTime.now());
            tenantUserRepository.save(staffUser);

            String roleName = staffUser.getRole() != null ? staffUser.getRole().getName() : ApplicationConstants.Roles.VIEWER;

            userPrincipal = UserPrincipal.builder()
                    .id(staffUser.getId())
                    .email(staffUser.getEmail())
                    .userCode(staffUser.getUserCode())
                    .fullName(staffUser.getFirstName() + " " + staffUser.getLastName())
                    .role(roleName)
                    .userType(ApplicationConstants.UserTypes.STAFF)
                    .organizationCode(org.getCode())
                    .tenantDbName(org.getDbName())
                    .branchId(staffUser.getBranch() != null ? staffUser.getBranch().getId() : null)
                    .active(true)
                    .build();

            userProfile = authMapper.toProfileResponse(staffUser, org);

        } else if (ApplicationConstants.UserTypes.CUSTOMER.equalsIgnoreCase(userType)) {
            TenantContext.setCurrentTenant(org.getDbName());
            TenantContext.setCurrentOrgCode(org.getCode());

            Customer customer = customerRepository.findByEmail(identifier)
                    .or(() -> customerRepository.findByCustomerCode(identifier))
                    .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

            if (!Boolean.TRUE.equals(customer.getIsActive())) {
                throw new UnauthorizedException("Your customer account is deactivated.");
            }

            if (customer.getPasswordHash() != null && !passwordEncoder.matches(request.getPassword(), customer.getPasswordHash())) {
                throw new UnauthorizedException("Invalid email or password");
            }

            customer.setLastLoginAt(LocalDateTime.now());
            customerRepository.save(customer);

            userPrincipal = UserPrincipal.builder()
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
                    .build();

            userProfile = authMapper.toProfileResponse(customer, org);

        } else {
            throw new BusinessException("Unsupported user type: " + userType);
        }

        // 4. Generate Tokens
        String accessToken = jwtTokenProvider.generateAccessToken(userPrincipal);
        RefreshToken refreshTokenEntity = tokenService.createRefreshToken(userPrincipal);

        String dashboardUrl = resolveDashboardUrl(userPrincipal.getRole());
        List<String> permissions = resolvePermissions(userPrincipal.getRole());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenEntity.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationMs())
                .user(userProfile)
                .dashboardUrl(dashboardUrl)
                .permissions(permissions)
                .build();
    }

    @Transactional
    public void changePassword(UserPrincipal principal, ChangePasswordRequest request) {
        String userType = principal.getUserType();

        if (ApplicationConstants.UserTypes.INTERNAL.equalsIgnoreCase(userType)) {
            TenantContext.setCurrentTenant(TenantContext.MASTER_TENANT_ID);
            InternalUser user = internalUserRepository.findById(principal.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Internal user not found"));

            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
                throw new BusinessException("INVALID_PASSWORD", "Current password does not match");
            }

            user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
            internalUserRepository.save(user);

        } else if (ApplicationConstants.UserTypes.STAFF.equalsIgnoreCase(userType)) {
            TenantContext.setCurrentTenant(principal.getTenantDbName());
            TenantUser user = tenantUserRepository.findById(principal.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Staff user not found"));

            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
                throw new BusinessException("INVALID_PASSWORD", "Current password does not match");
            }

            user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
            tenantUserRepository.save(user);

        } else if (ApplicationConstants.UserTypes.CUSTOMER.equalsIgnoreCase(userType)) {
            TenantContext.setCurrentTenant(principal.getTenantDbName());
            Customer customer = customerRepository.findById(principal.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

            if (customer.getPasswordHash() != null && !passwordEncoder.matches(request.getCurrentPassword(), customer.getPasswordHash())) {
                throw new BusinessException("INVALID_PASSWORD", "Current password does not match");
            }

            customer.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
            customerRepository.save(customer);
        }
    }

    public UserProfileResponse getCurrentUserProfile(UserPrincipal principal) {
        String userType = principal.getUserType();

        if (ApplicationConstants.UserTypes.INTERNAL.equalsIgnoreCase(userType)) {
            TenantContext.setCurrentTenant(TenantContext.MASTER_TENANT_ID);
            InternalUser user = internalUserRepository.findById(principal.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Internal user not found"));
            return authMapper.toProfileResponse(user);
        }

        TenantContext.setCurrentTenant(principal.getTenantDbName());
        Organization org = organizationRepository.findByCode(principal.getOrganizationCode()).orElse(null);

        if (ApplicationConstants.UserTypes.STAFF.equalsIgnoreCase(userType)) {
            TenantUser user = tenantUserRepository.findById(principal.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Staff user not found"));
            return authMapper.toProfileResponse(user, org);
        } else {
            Customer customer = customerRepository.findById(principal.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
            return authMapper.toProfileResponse(customer, org);
        }
    }

    private String resolveDashboardUrl(String role) {
        if (role == null) return "/dashboard";
        return switch (role.toUpperCase()) {
            case ApplicationConstants.Roles.INTERNAL_ADMIN -> "/dashboard/internal-admin";
            case ApplicationConstants.Roles.SUPER_ADMIN -> "/dashboard/tenant-admin";
            case ApplicationConstants.Roles.MAKER -> "/dashboard/maker";
            case ApplicationConstants.Roles.CHECKER -> "/dashboard/checker";
            case ApplicationConstants.Roles.VIEWER -> "/dashboard/viewer";
            case ApplicationConstants.Roles.CUSTOMER -> "/dashboard/customer";
            default -> "/dashboard";
        };
    }

    private List<String> resolvePermissions(String role) {
        List<String> permissions = new ArrayList<>();
        if (role == null) return permissions;

        switch (role.toUpperCase()) {
            case ApplicationConstants.Roles.INTERNAL_ADMIN -> {
                permissions.add("ORGANIZATION_CREATE");
                permissions.add("ORGANIZATION_VIEW");
                permissions.add("ORGANIZATION_UPDATE");
                permissions.add("SYSTEM_AUDIT_VIEW");
                permissions.add("GLOBAL_USER_MANAGEMENT");
            }
            case ApplicationConstants.Roles.SUPER_ADMIN -> {
                permissions.add("BRANCH_MANAGE");
                permissions.add("STAFF_USER_MANAGE");
                permissions.add("LOAN_SCHEME_CONFIG");
                permissions.add("REPORTS_EXPORT");
                permissions.add("CUSTOMER_VIEW_ALL");
            }
            case ApplicationConstants.Roles.MAKER -> {
                permissions.add("CUSTOMER_CREATE");
                permissions.add("CUSTOMER_EDIT");
                permissions.add("LOAN_APPLICATION_CREATE");
                permissions.add("LOAN_APPLICATION_SUBMIT");
                permissions.add("DOCUMENT_UPLOAD");
            }
            case ApplicationConstants.Roles.CHECKER -> {
                permissions.add("LOAN_APPLICATION_VERIFY");
                permissions.add("LOAN_APPLICATION_APPROVE");
                permissions.add("LOAN_APPLICATION_REJECT");
                permissions.add("DOCUMENT_VERIFY");
            }
            case ApplicationConstants.Roles.VIEWER -> {
                permissions.add("LOAN_APPLICATION_READ");
                permissions.add("REPORTS_VIEW");
                permissions.add("DASHBOARD_ANALYTICS_VIEW");
            }
            case ApplicationConstants.Roles.CUSTOMER -> {
                permissions.add("PROFILE_VIEW");
                permissions.add("MY_APPLICATIONS_VIEW");
                permissions.add("MY_LOANS_VIEW");
                permissions.add("DOCUMENT_SUBMIT");
            }
        }
        return permissions;
    }
}
