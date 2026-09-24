package com.bank.los.user.service;

import com.bank.los.common.constant.ApplicationConstants;
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
import java.util.ArrayList;
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

    public List<UserResponse> getAllUsers(UserPrincipal principal, Long organizationId) {
        boolean isInternalAdmin = isInternalAdmin(principal);

        if (isInternalAdmin) {
            if (organizationId != null) {
                TenantContext.setCurrentTenant(TenantContext.MASTER_TENANT_ID);
                Organization org = organizationRepository.findById(organizationId)
                        .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", organizationId));
                TenantContext.setCurrentTenant(org.getDbName());
                TenantContext.setCurrentOrgCode(org.getCode());
                return tenantUserRepository.findAll().stream()
                        .map(u -> mapToResponse(u, org))
                        .collect(Collectors.toList());
            } else {
                // Return all users across all active organizations
                TenantContext.setCurrentTenant(TenantContext.MASTER_TENANT_ID);
                List<Organization> orgs = organizationRepository.findAll();
                List<UserResponse> allUsers = new ArrayList<>();
                for (Organization org : orgs) {
                    try {
                        TenantContext.setCurrentTenant(org.getDbName());
                        TenantContext.setCurrentOrgCode(org.getCode());
                        List<UserResponse> orgUsers = tenantUserRepository.findAll().stream()
                                .map(u -> mapToResponse(u, org))
                                .collect(Collectors.toList());
                        allUsers.addAll(orgUsers);
                    } catch (Exception ex) {
                        log.debug("Notice listing users for org {}: {}", org.getCode(), ex.getMessage());
                    }
                }
                return allUsers;
            }
        } else {
            // Tenant admin / staff querying their own organization
            TenantContext.setCurrentTenant(principal.getTenantDbName());
            TenantContext.setCurrentOrgCode(principal.getOrganizationCode());
            return tenantUserRepository.findAll().stream()
                    .map(u -> mapToResponse(u, null))
                    .collect(Collectors.toList());
        }
    }

    public UserResponse getUserById(UserPrincipal principal, Long id, Long organizationId) {
        boolean isInternalAdmin = isInternalAdmin(principal);
        Organization targetOrg = null;

        if (isInternalAdmin) {
            if (organizationId != null) {
                TenantContext.setCurrentTenant(TenantContext.MASTER_TENANT_ID);
                targetOrg = organizationRepository.findById(organizationId)
                        .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", organizationId));
                TenantContext.setCurrentTenant(targetOrg.getDbName());
                TenantContext.setCurrentOrgCode(targetOrg.getCode());
            } else {
                // Look up in login directory
                TenantContext.setCurrentTenant(TenantContext.MASTER_TENANT_ID);
                // Search in current context or master
            }
        } else {
            TenantContext.setCurrentTenant(principal.getTenantDbName());
            TenantContext.setCurrentOrgCode(principal.getOrganizationCode());
        }

        final Organization finalOrg = targetOrg;
        TenantUser user = tenantUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return mapToResponse(user, finalOrg);
    }

    @Transactional
    public UserResponse createUser(UserPrincipal principal, CreateUserRequest request) {
        PasswordPolicy.validate(request.getPassword());
        boolean isInternalAdmin = isInternalAdmin(principal);

        Organization org;
        if (isInternalAdmin) {
            TenantContext.setCurrentTenant(TenantContext.MASTER_TENANT_ID);
            if (request.getOrganizationId() != null) {
                org = organizationRepository.findById(request.getOrganizationId())
                        .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", request.getOrganizationId()));
            } else if (request.getOrganizationCode() != null && !request.getOrganizationCode().isBlank()) {
                org = organizationRepository.findByCode(request.getOrganizationCode().trim().toUpperCase())
                        .orElseThrow(() -> new ResourceNotFoundException("Organization", "code", request.getOrganizationCode()));
            } else {
                throw new BusinessException("ORGANIZATION_REQUIRED", "Please specify organizationId or organizationCode when creating a bank user as Super Admin");
            }
        } else {
            TenantContext.setCurrentTenant(TenantContext.MASTER_TENANT_ID);
            org = organizationRepository.findByCode(principal.getOrganizationCode())
                    .orElseThrow(() -> new ResourceNotFoundException("Organization", "code", principal.getOrganizationCode()));
        }

        String tenantDb = org.getDbName();
        String orgCode = org.getCode();

        // 1. Check globally in Master Login Directory for unique email
        TenantContext.setCurrentTenant(TenantContext.MASTER_TENANT_ID);
        if (loginDirectoryRepository.findByEmail(request.getEmail().trim().toLowerCase()).isPresent()) {
            throw new BusinessException("EMAIL_EXISTS", "User with email '" + request.getEmail() + "' is already registered in the platform");
        }

        // 2. Switch to target Tenant DB
        TenantContext.setCurrentTenant(tenantDb);
        TenantContext.setCurrentOrgCode(orgCode);

        if (tenantUserRepository.existsByUsername(request.getUsername().trim().toLowerCase())) {
            throw new BusinessException("USERNAME_EXISTS", "User with username '" + request.getUsername() + "' already exists in " + org.getName());
        }

        if (tenantUserRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new BusinessException("EMAIL_EXISTS", "User with email '" + request.getEmail() + "' already exists in " + org.getName());
        }

        // 3. Resolve Role in tenant DB
        TenantRole role = null;
        if (request.getRoleId() != null) {
            role = tenantRoleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "id", request.getRoleId()));
        } else if (request.getRoleName() != null && !request.getRoleName().isBlank()) {
            String roleName = request.getRoleName().trim().toUpperCase();
            role = tenantRoleRepository.findByName(roleName)
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleName));
        } else {
            // Default to ADMIN if created by Super Admin, or VIEWER
            role = tenantRoleRepository.findByName(isInternalAdmin ? "ADMIN" : "VIEWER")
                    .orElseThrow(() -> new BusinessException("ROLE_REQUIRED", "Could not resolve default role in tenant database"));
        }

        // 4. Resolve Branch in tenant DB
        Branch loginBranch = null;
        if (Boolean.TRUE.equals(request.getMultiBranchAccess())) {
            if (request.getLoginBranchId() != null) {
                loginBranch = branchRepository.findById(request.getLoginBranchId()).orElse(null);
            }
        } else {
            if (request.getLoginBranchId() != null) {
                loginBranch = branchRepository.findById(request.getLoginBranchId())
                        .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", request.getLoginBranchId()));
            } else {
                // Pick primary/first active branch in this tenant if exists
                loginBranch = branchRepository.findAll().stream().findFirst().orElse(null);
            }
        }

        String empNo = request.getEmpNo();
        if (empNo == null || empNo.isBlank()) {
            empNo = "EMP-" + orgCode + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        }

        // Initial status: OPERATIVE when created by Super Admin (so bank admin can login immediately)
        String initialStatus = request.getStatus();
        if (initialStatus == null || initialStatus.isBlank()) {
            initialStatus = isInternalAdmin ? ApplicationConstants.UserStatus.OPERATIVE : ApplicationConstants.UserStatus.PENDING_VERIFICATION;
        }

        TenantUser user = TenantUser.builder()
                .empNo(empNo)
                .username(request.getUsername().trim().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .twoFaEnabled(request.getTwoFaEnabled() != null ? request.getTwoFaEnabled() : true)
                .status(initialStatus)
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
                .designation(request.getDesignation() != null ? request.getDesignation() : (role.getName() + " - " + org.getName()))
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

        // 5. Register in Master Login Directory for dynamic routing
        try {
            TenantContext.setCurrentTenant(TenantContext.MASTER_TENANT_ID);
            LoginDirectory loginDirectory = LoginDirectory.builder()
                    .userCode(savedUser.getEmpNo())
                    .email(savedUser.getEmail())
                    .phone(savedUser.getMobile())
                    .organization(org)
                    .userType(ApplicationConstants.UserTypes.STAFF)
                    .build();

            loginDirectoryRepository.save(loginDirectory);
        } finally {
            TenantContext.setCurrentTenant(tenantDb);
            TenantContext.setCurrentOrgCode(orgCode);
        }

        log.info("Successfully created staff user empNo={} username={} role={} for organization={}",
                savedUser.getEmpNo(), savedUser.getUsername(), role.getName(), orgCode);
        return mapToResponse(savedUser, org);
    }

    @Transactional
    public UserResponse verifyUser(UserPrincipal principal, Long userId) {
        String tenantDb = principal.getTenantDbName();
        TenantContext.setCurrentTenant(tenantDb);

        TenantUser user = tenantUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (!isInternalAdmin(principal) && user.getCreatedBy() != null && user.getCreatedBy().equals(principal.getId())) {
            throw new BusinessException("MAKER_CHECKER_VIOLATION", "The creator of the user cannot verify/approve the same user record (Maker-Checker policy).");
        }

        user.setStatus(ApplicationConstants.UserStatus.OPERATIVE);
        user.setVerifiedBy(principal.getId());
        user.setVerifiedDate(LocalDateTime.now());
        user.setModifiedBy(principal.getId());
        user.setUpdatedAt(LocalDateTime.now());

        TenantUser updated = tenantUserRepository.save(user);
        log.info("User id={} verified and marked OPERATIVE by verifier={}", userId, principal.getId());
        return mapToResponse(updated, null);
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

    public UserResponse mapToResponse(TenantUser user, Organization org) {
        return UserResponse.builder()
                .id(user.getId())
                .organizationId(org != null ? org.getId() : null)
                .organizationCode(org != null ? org.getCode() : null)
                .organizationName(org != null ? org.getName() : null)
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

    private boolean isInternalAdmin(UserPrincipal principal) {
        return principal != null && (
                ApplicationConstants.Roles.INTERNAL_ADMIN.equalsIgnoreCase(principal.getRole()) ||
                ApplicationConstants.Roles.SUPER_ADMIN.equalsIgnoreCase(principal.getRole()) ||
                ApplicationConstants.UserTypes.INTERNAL.equalsIgnoreCase(principal.getUserType())
        );
    }
}
