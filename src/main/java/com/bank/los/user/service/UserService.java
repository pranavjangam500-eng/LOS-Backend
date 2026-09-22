package com.bank.los.user.service;

import com.bank.los.common.exception.BusinessException;
import com.bank.los.common.exception.ResourceNotFoundException;
import com.bank.los.common.validation.PasswordPolicy;
import com.bank.los.config.TenantContext;
import com.bank.los.master.entity.LoginDirectory;
import com.bank.los.master.entity.Organization;
import com.bank.los.master.repository.LoginDirectoryRepository;
import com.bank.los.master.repository.OrganizationRepository;
import com.bank.los.security.UserPrincipal;
import com.bank.los.tenant.entity.Branch;
import com.bank.los.tenant.entity.TenantRole;
import com.bank.los.tenant.entity.TenantUser;
import com.bank.los.tenant.repository.BranchRepository;
import com.bank.los.tenant.repository.TenantRoleRepository;
import com.bank.los.tenant.repository.TenantUserRepository;
import com.bank.los.user.dto.AdminResetUserPasswordRequest;
import com.bank.los.user.dto.CreateUserRequest;
import com.bank.los.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final TenantUserRepository tenantUserRepository;
    private final BranchRepository branchRepository;
    private final TenantRoleRepository tenantRoleRepository;
    private final LoginDirectoryRepository loginDirectoryRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserResponse> getAllUsers(UserPrincipal principal) {
        TenantContext.setCurrentTenant(principal.getTenantDbName());
        return tenantUserRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(UserPrincipal principal, Long id) {
        TenantContext.setCurrentTenant(principal.getTenantDbName());
        TenantUser user = tenantUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return mapToResponse(user);
    }

    @Transactional
    public UserResponse createUser(UserPrincipal principal, CreateUserRequest request) {
        PasswordPolicy.validate(request.getPassword());
        String tenantDb = principal.getTenantDbName();
        String orgCode = principal.getOrganizationCode();

        TenantContext.setCurrentTenant(tenantDb);

        if (tenantUserRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("USERNAME_EXISTS", "User with username '" + request.getUsername() + "' already exists");
        }

        if (tenantUserRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("EMAIL_EXISTS", "User with email '" + request.getEmail() + "' already exists");
        }

        Branch loginBranch = null;
        if (Boolean.TRUE.equals(request.getMultiBranchAccess())) {
            if (request.getLoginBranchId() != null) {
                loginBranch = branchRepository.findById(request.getLoginBranchId()).orElse(null);
            }
        } else {
            if (request.getLoginBranchId() == null) {
                throw new BusinessException("BRANCH_REQUIRED", "Login Branch is mandatory when Multi-Branch Access is not enabled");
            }
            loginBranch = branchRepository.findById(request.getLoginBranchId())
                    .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", request.getLoginBranchId()));
        }

        TenantRole role = tenantRoleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", request.getRoleId()));

        String empNo = request.getEmpNo();
        if (empNo == null || empNo.isBlank()) {
            empNo = "EMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }

        TenantUser user = TenantUser.builder()
                .empNo(empNo)
                .username(request.getUsername().trim().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .twoFaEnabled(request.getTwoFaEnabled() != null ? request.getTwoFaEnabled() : true)
                .status("PENDING_VERIFICATION")
                .isActive(true)
                .role(role)
                .loginBranch(loginBranch)
                .multiBranchAccess(Boolean.TRUE.equals(request.getMultiBranchAccess()))
                .firstName(request.getFirstName())
                .middleName(request.getMiddleName())
                .lastName(request.getLastName())
                .dob(request.getDob())
                .email(request.getEmail().trim().toLowerCase())
                .mobile(request.getMobile())
                .gender(request.getGender())
                .designation(request.getDesignation())
                .loginOnHolidays(Boolean.TRUE.equals(request.getLoginOnHolidays()))
                .loginTime(request.getLoginTime())
                .logoutTime(request.getLogoutTime())
                .inactiveSessionTimeout(request.getInactiveSessionTimeout() != null ? request.getInactiveSessionTimeout() : 1800)
                .noOfBadLogins(0)
                .createdBy(principal.getId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        TenantUser savedUser = tenantUserRepository.save(user);

        // Register in Master Login Directory for routing
        try {
            TenantContext.setCurrentTenant(TenantContext.MASTER_TENANT_ID);
            Organization org = organizationRepository.findByCode(orgCode)
                    .orElseThrow(() -> new ResourceNotFoundException("Organization", "code", orgCode));

            LoginDirectory loginDirectory = LoginDirectory.builder()
                    .userCode(savedUser.getEmpNo())
                    .email(savedUser.getEmail())
                    .phone(savedUser.getMobile())
                    .organization(org)
                    .userType("STAFF")
                    .build();

            loginDirectoryRepository.save(loginDirectory);
        } finally {
            TenantContext.setCurrentTenant(tenantDb);
        }

        log.info("Created staff user empNo={} username={} for tenant={}", savedUser.getEmpNo(), savedUser.getUsername(), tenantDb);
        return mapToResponse(savedUser);
    }

    @Transactional
    public UserResponse verifyUser(UserPrincipal principal, Long userId) {
        String tenantDb = principal.getTenantDbName();
        TenantContext.setCurrentTenant(tenantDb);

        TenantUser user = tenantUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (user.getCreatedBy() != null && user.getCreatedBy().equals(principal.getId())) {
            throw new BusinessException("MAKER_CHECKER_VIOLATION", "The creator of the user cannot verify/approve the same user record (Maker-Checker policy).");
        }

        user.setStatus("OPERATIVE");
        user.setVerifiedBy(principal.getId());
        user.setVerifiedDate(LocalDateTime.now());
        user.setModifiedBy(principal.getId());
        user.setUpdatedAt(LocalDateTime.now());

        TenantUser updated = tenantUserRepository.save(user);
        log.info("User id={} verified and marked OPERATIVE by verifier={}", userId, principal.getId());
        return mapToResponse(updated);
    }

    @Transactional
    public void adminResetPassword(UserPrincipal principal, Long userId, AdminResetUserPasswordRequest request) {
        PasswordPolicy.validate(request.getNewPassword());
        String tenantDb = principal.getTenantDbName();
        TenantContext.setCurrentTenant(tenantDb);

        TenantUser user = tenantUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setNoOfBadLogins(0);
        user.setModifiedBy(principal.getId());
        user.setUpdatedAt(LocalDateTime.now());

        tenantUserRepository.save(user);
        log.info("Password reset by admin id={} for user id={}", principal.getId(), userId);
    }

    public UserResponse mapToResponse(TenantUser user) {
        return UserResponse.builder()
                .id(user.getId())
                .empNo(user.getEmpNo())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .middleName(user.getMiddleName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .gender(user.getGender())
                .dob(user.getDob())
                .designation(user.getDesignation())
                .status(user.getStatus())
                .isActive(user.getIsActive())
                .twoFaEnabled(user.getTwoFaEnabled())
                .roleId(user.getRole() != null ? user.getRole().getId() : null)
                .roleName(user.getRole() != null ? user.getRole().getName() : null)
                .multiBranchAccess(user.getMultiBranchAccess())
                .loginBranchId(user.getLoginBranch() != null ? user.getLoginBranch().getId() : null)
                .loginBranchName(user.getLoginBranch() != null ? user.getLoginBranch().getName() : null)
                .loginOnHolidays(user.getLoginOnHolidays())
                .loginTime(user.getLoginTime())
                .logoutTime(user.getLogoutTime())
                .inactiveSessionTimeout(user.getInactiveSessionTimeout())
                .noOfBadLogins(user.getNoOfBadLogins())
                .lastLoginDate(user.getLastLoginDate())
                .lastLoginTime(user.getLastLoginTime())
                .createdBy(user.getCreatedBy())
                .createdAt(user.getCreatedAt())
                .verifiedBy(user.getVerifiedBy())
                .verifiedDate(user.getVerifiedDate())
                .modifiedBy(user.getModifiedBy())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
