package com.bank.los.bank.user.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.los.administration.master.entity.LoginDirectory;
import com.bank.los.administration.master.entity.Organization;
import com.bank.los.administration.master.repository.LoginDirectoryRepository;
import com.bank.los.administration.master.repository.OrganizationRepository;
import com.bank.los.bank.master.entity.Branch;
import com.bank.los.bank.master.entity.OrganizationRole;
import com.bank.los.bank.master.entity.OrganizationUser;
import com.bank.los.bank.master.repository.BranchRepository;
import com.bank.los.bank.master.repository.OrganizationRoleRepository;
import com.bank.los.bank.master.repository.OrganizationUserRepository;
import com.bank.los.bank.user.dto.AdminResetUserPasswordRequest;
import com.bank.los.bank.user.dto.CreateUserRequest;
import com.bank.los.bank.user.dto.UpdateUserRequest;
import com.bank.los.bank.user.dto.UserResponse;
import com.bank.los.common.constant.ApplicationConstants;
import com.bank.los.common.exception.BusinessException;
import com.bank.los.common.exception.ResourceNotFoundException;
import com.bank.los.common.validation.PasswordPolicy;
import com.bank.los.administration.master.repository.MasterLookupSubTypeRepository;
import com.bank.los.bank.master.entity.BankLookupSubType;
import com.bank.los.bank.master.repository.BankLookupSubTypeRepository;
import com.bank.los.config.BankContext;
import com.bank.los.config.OrganizationContext;
import com.bank.los.security.UserPrincipal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final OrganizationUserRepository organizationUserRepository;
    private final BranchRepository branchRepository;
    private final OrganizationRoleRepository organizationRoleRepository;
    private final LoginDirectoryRepository loginDirectoryRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.bank.los.config.BankDataSourceProvider bankDataSourceProvider;
    private final BankLookupSubTypeRepository bankLookupSubTypeRepository;
    private final MasterLookupSubTypeRepository masterLookupSubTypeRepository;


    public List<UserResponse> getAllUsers(UserPrincipal principal, Long organizationId) {
        if (principal == null) {
            throw new org.springframework.security.access.AccessDeniedException("Authentication required");
        }
        boolean isInternalAdmin = isInternalAdmin(principal);

        if (isInternalAdmin) {
            if (organizationId != null) {
                OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);
                Organization org = organizationRepository.findById(organizationId)
                        .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", organizationId));
                bankDataSourceProvider.getOrCreateBankDataSource(org.getDbName(), org.getDbHost(), org.getDbPort());
                OrganizationContext.setCurrentOrganization(org.getDbName());
                OrganizationContext.setCurrentOrgCode(org.getCode());
                return organizationUserRepository.findAll().stream()
                        .map(u -> mapToResponse(u, org))
                        .collect(Collectors.toList());
            } else {
                // Return all users across all active organizations
                OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);
                List<Organization> orgs = organizationRepository.findAll();
                List<UserResponse> allUsers = new ArrayList<>();
                for (Organization org : orgs) {
                    try {
                        bankDataSourceProvider.getOrCreateBankDataSource(org.getDbName(), org.getDbHost(), org.getDbPort());
                        OrganizationContext.setCurrentOrganization(org.getDbName());
                        OrganizationContext.setCurrentOrgCode(org.getCode());
                        List<UserResponse> orgUsers = organizationUserRepository.findAll().stream()
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
            // Organization admin / staff querying their own organization
            if (organizationId != null && !principal.belongsToOrganization(organizationId)) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "Access denied: You cannot access users of another organization"
                );
            }
            Organization org = null;
            if (principal.getOrganizationId() != null) {
                OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);
                org = organizationRepository.findById(principal.getOrganizationId()).orElse(null);
            }
            final Organization finalOrg = org;
            OrganizationContext.setCurrentOrganization(principal.getOrganizationDbName());
            OrganizationContext.setCurrentOrgCode(principal.getOrganizationCode());
            return organizationUserRepository.findAll().stream()
                    .map(u -> mapToResponse(u, finalOrg))
                    .collect(Collectors.toList());
        }
    }

    public UserResponse getUserById(UserPrincipal principal, Long id, Long organizationId) {
        if (principal == null) {
            throw new org.springframework.security.access.AccessDeniedException("Authentication required");
        }
        boolean isInternalAdmin = isInternalAdmin(principal);
        Organization targetOrg = null;

        if (isInternalAdmin) {
            if (organizationId != null) {
                OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);
                targetOrg = organizationRepository.findById(organizationId)
                        .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", organizationId));
                bankDataSourceProvider.getOrCreateBankDataSource(targetOrg.getDbName(), targetOrg.getDbHost(), targetOrg.getDbPort());
                OrganizationContext.setCurrentOrganization(targetOrg.getDbName());
                OrganizationContext.setCurrentOrgCode(targetOrg.getCode());
            } else {
                OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);
            }
        } else {
            if (organizationId != null && !principal.belongsToOrganization(organizationId)) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "Access denied: You cannot access users of another organization"
                );
            }
            if (principal.getOrganizationId() != null) {
                OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);
                targetOrg = organizationRepository.findById(principal.getOrganizationId()).orElse(null);
            }
            OrganizationContext.setCurrentOrganization(principal.getOrganizationDbName());
            OrganizationContext.setCurrentOrgCode(principal.getOrganizationCode());
        }

        final Organization finalOrg = targetOrg;
        OrganizationUser user = organizationUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return mapToResponse(user, finalOrg);
    }

    public UserResponse createUser(UserPrincipal principal, CreateUserRequest request) {
        PasswordPolicy.validate(request.getPassword());
        boolean isInternalAdmin = isInternalAdmin(principal);

        Organization org;
        OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);
        if (isInternalAdmin) {
            if (request.getOrganizationId() != null) {
                org = organizationRepository.findById(request.getOrganizationId())
                        .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", request.getOrganizationId()));
            } else if (request.getOrganizationUuid() != null) {
                org = organizationRepository.findByUuid(request.getOrganizationUuid())
                        .orElseThrow(() -> new ResourceNotFoundException("Organization", "uuid", request.getOrganizationUuid()));
            } else if (request.getOrganizationCode() != null && !request.getOrganizationCode().isBlank()) {
                org = organizationRepository.findByCode(request.getOrganizationCode().trim().toUpperCase())
                        .orElseThrow(() -> new ResourceNotFoundException("Organization", "code", request.getOrganizationCode()));
            } else {
                throw new BusinessException("ORGANIZATION_REQUIRED", "Please specify organizationId, organizationUuid, or organizationCode when creating a bank user as Super Admin");
            }
        } else {
            if (request.getOrganizationId() != null && !principal.belongsToOrganization(request.getOrganizationId())) {
                throw new org.springframework.security.access.AccessDeniedException("Access denied: You cannot create users for another organization");
            }
            if (request.getOrganizationCode() != null && !principal.belongsToOrganization(request.getOrganizationCode())) {
                throw new org.springframework.security.access.AccessDeniedException("Access denied: You cannot create users for another organization");
            }
            if (principal.getOrganizationId() != null) {
                org = organizationRepository.findById(principal.getOrganizationId())
                        .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", principal.getOrganizationId()));
            } else {
                org = organizationRepository.findByCode(principal.getOrganizationCode())
                        .orElseThrow(() -> new ResourceNotFoundException("Organization", "code", principal.getOrganizationCode()));
            }
            request.setOrganizationId(org.getId());
            request.setOrganizationCode(org.getCode());
        }

        String orgDb = org.getDbName();
        String orgCode = org.getCode();

        // 1. Check globally in Master Login Directory for unique email & empNo
        OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);
        if (loginDirectoryRepository.findByEmail(request.getEmail().trim().toLowerCase()).isPresent()) {
            throw new BusinessException("EMAIL_EXISTS", "User with email '" + request.getEmail() + "' is already registered in the platform");
        }
        if (request.getEmpNo() != null && !request.getEmpNo().isBlank()) {
            String candidateEmpNo = request.getEmpNo().trim();
            if (loginDirectoryRepository.findByUserCode(candidateEmpNo).isPresent()
                    || loginDirectoryRepository.existsByUserCode(candidateEmpNo)) {
                throw new BusinessException("EMP_NO_EXISTS", "User with employee number '" + candidateEmpNo + "' is already registered in the platform");
            }
        }

        // Pre-warm / register Bank DataSource using org's database settings
        bankDataSourceProvider.getOrCreateBankDataSource(orgDb, org.getDbHost(), org.getDbPort());

        // 2. Switch to target Organization DB
        OrganizationContext.setCurrentOrganization(orgDb);
        OrganizationContext.setCurrentOrgCode(orgCode);

        // Parse Name if full name passed
        String firstName = request.getFirstName();
        String middleName = request.getMiddleName();
        String lastName = request.getLastName();

        if ((firstName == null || firstName.isBlank()) && request.getName() != null && !request.getName().isBlank()) {
            String[] parts = request.getName().trim().split("\\s+");
            switch (parts.length) {
                case 1 -> {
                    firstName = parts[0];
                    lastName = parts[0];
                }
                case 2 -> {
                    firstName = parts[0];
                    lastName = parts[1];
                }
                default -> {
                    firstName = parts[0];
                    middleName = parts[1];
                    StringBuilder sb = new StringBuilder();
                    for (int i = 2; i < parts.length; i++) {
                        if (i > 2) {
                            sb.append(" ");
                        }
                        sb.append(parts[i]);
                    }
                    lastName = sb.toString();
                }
            }
        }

        if (firstName == null || firstName.isBlank()) {
            firstName = request.getEmail().split("@")[0];
        }
        if (lastName == null || lastName.isBlank()) {
            lastName = "Staff";
        }

        String username = request.getUsername();
        if (username == null || username.isBlank()) {
            username = request.getEmail().split("@")[0].toLowerCase();
        } else {
            username = username.trim().toLowerCase();
        }

        if (organizationUserRepository.existsByUsername(username)) {
            throw new BusinessException("USERNAME_EXISTS", "User with username '" + username + "' already exists in " + org.getName());
        }

        if (organizationUserRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new BusinessException("EMAIL_EXISTS", "User with email '" + request.getEmail() + "' already exists in " + org.getName());
        }

        // 3. Resolve and validate Role in organization DB against lookup 10003
        OrganizationRole role = validateAndResolveRole(request.getRoleName(), request.getRoleId(), isInternalAdmin);

        // 4. Validate and resolve Designation against lookup 10002
        String designationCode = validateAndResolveDesignation(request.getDesignation());

        // 5. Resolve Branch in organization DB
        Branch loginBranch = null;
        Long targetBranchId = request.getLoginBranchId();
        String branchIdentifier = request.getLoginBranch();

        if (targetBranchId == null && branchIdentifier != null && !branchIdentifier.isBlank()) {
            try {
                targetBranchId = Long.valueOf(branchIdentifier.trim());
            } catch (NumberFormatException ignored) {
            }
        }

        if (targetBranchId != null) {
            loginBranch = branchRepository.findById(targetBranchId).orElse(null);
        }
        if (loginBranch == null && branchIdentifier != null && !branchIdentifier.isBlank()) {
            loginBranch = branchRepository.findByName(branchIdentifier.trim())
                    .or(() -> branchRepository.findByCode(branchIdentifier.trim()))
                    .orElse(null);
        }

        if (loginBranch == null) {
            loginBranch = branchRepository.findAll().stream().findFirst()
                    .orElseGet(() -> branchRepository.save(Branch.builder()
                            .name(org.getName() + " Main Branch")
                            .code(org.getCode() + "-BR-01")
                            .address("Headquarters")
                            .city("Mumbai")
                            .state("Maharashtra")
                            .pincode("400001")
                            .status("ACTIVE")
                            .build()));
        }

        String empNo = request.getEmpNo();
        if (empNo == null || empNo.isBlank()) {
            do {
                empNo = "EMP-" + orgCode + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            } while (organizationUserRepository.existsByEmpNo(empNo)
                    || organizationUserRepository.existsByEmpNoIgnoreCase(empNo));
        } else {
            empNo = empNo.trim();
            if (organizationUserRepository.existsByEmpNo(empNo)
                    || organizationUserRepository.existsByEmpNoIgnoreCase(empNo)) {
                throw new BusinessException("EMP_NO_EXISTS", "User with employee number '" + empNo + "' already exists in " + org.getName());
            }
        }
        String initialStatus = request.getStatus();
        if (initialStatus == null || initialStatus.isBlank()) {
            boolean isStaffSuperAdmin = principal != null && ApplicationConstants.Roles.SUPER_ADMIN.equalsIgnoreCase(principal.getRole());
            initialStatus = (isInternalAdmin || isStaffSuperAdmin) ? ApplicationConstants.UserStatus.OPERATIVE : ApplicationConstants.UserStatus.PENDING_VERIFICATION;
        }

        Boolean twoFa = request.getTwoFaEnabled();
        if (twoFa == null) {
            twoFa = true;
        }

        Long creatorId;
        if (principal != null && principal.getId() != null) {
            creatorId = principal.getId();
        } else if (request.getCreatedBy() != null) {
            creatorId = request.getCreatedBy();
        } else {
            creatorId = 1L;
        }

        Integer timeout = request.getInactiveSessionTimeout();
        if (timeout == null) {
            timeout = 1800;
        }
        Integer badLogins = request.getNoOfBadLogins();
        if (badLogins == null) {
            badLogins = 0;
        }

        OrganizationUser user = OrganizationUser.builder()
                .empNo(empNo)
                .username(username)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .twoFaEnabled(twoFa)
                .status(initialStatus)
                .isActive(true)
                .role(role)
                .loginBranch(loginBranch)
                .multiBranchAccess(Boolean.TRUE.equals(request.getMultiBranchAccess()))
                .firstName(firstName)
                .middleName(middleName)
                .lastName(lastName)
                .dob(request.getDob())
                .email(request.getEmail().trim().toLowerCase())
                .mobile(request.getMobile())
                .gender(request.getGender())
                .designation(designationCode)
                .loginOnHolidays(Boolean.TRUE.equals(request.getLoginOnHolidays()))

                .loginTime(request.getLoginTime())
                .logoutTime(request.getLogoutTime())
                .inactiveSessionTimeout(timeout)
                .lastLoginDate(request.getLastLoginDate())
                .noOfBadLogins(badLogins)
                .verifiedBy(request.getVerifiedBy())
                .verifiedDate(request.getVerifiedDate())
                .modifiedBy(request.getModifiedBy())
                .build();
        user.setCreatedBy(creatorId);

        OrganizationUser savedUser;
        try {
            savedUser = organizationUserRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            String msg = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
            if (msg.contains("emp_no") || msg.contains("empno") || msg.contains("users_emp_no")) {
                throw new BusinessException("EMP_NO_EXISTS", "User with Employee Number '" + empNo + "' already exists in " + org.getName());
            }
            throw ex;
        }

        // 5. Register in Master Login Directory for dynamic routing
        try {
            OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);
            LoginDirectory loginDirectory = LoginDirectory.builder()
                    .userCode(savedUser.getEmpNo())
                    .email(savedUser.getEmail())
                    .phone(savedUser.getMobile())
                    .organization(org)
                    .userType(ApplicationConstants.UserTypes.STAFF)
                    .build();

            loginDirectoryRepository.save(loginDirectory);
        } catch (DataIntegrityViolationException ex) {
            String msg = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
            if (msg.contains("user_code") || msg.contains("usercode") || msg.contains("emp_no")) {
                throw new BusinessException("EMP_NO_EXISTS", "User with Employee Number '" + savedUser.getEmpNo() + "' is already registered in the platform");
            }
            throw ex;
        } finally {
            OrganizationContext.setCurrentOrganization(orgDb);
            OrganizationContext.setCurrentOrgCode(orgCode);
        }

        log.info("Successfully created staff user empNo={} username={} role={} for organization={}",
                savedUser.getEmpNo(), savedUser.getUsername(), role.getName(), orgCode);
        return mapToResponse(savedUser, org);
    }

    @Transactional
    public UserResponse verifyUser(UserPrincipal principal, Long userId) {
        String orgDb = principal.getOrganizationDbName();
        OrganizationContext.setCurrentOrganization(orgDb);

        OrganizationUser user = organizationUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (!isInternalAdmin(principal) && user.getCreatedBy() != null && user.getCreatedBy().equals(principal.getId())) {
            throw new BusinessException("MAKER_CHECKER_VIOLATION", "The creator of the user cannot verify/approve the same user record (Maker-Checker policy).");
        }

        user.setStatus(ApplicationConstants.UserStatus.OPERATIVE);
        user.setVerifiedBy(principal.getId());
        user.setVerifiedDate(LocalDateTime.now());
        user.setModifiedBy(principal.getId());
        user.setUpdatedAt(LocalDateTime.now());

        OrganizationUser updated = organizationUserRepository.save(user);
        log.info("User id={} verified and marked OPERATIVE by verifier={}", userId, principal.getId());
        Organization org = null;
        if (principal.getOrganizationId() != null) {
            OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);
            org = organizationRepository.findById(principal.getOrganizationId()).orElse(null);
            OrganizationContext.setCurrentOrganization(orgDb);
        }
        return mapToResponse(updated, org);
    }

    @Transactional
    public void adminResetPassword(UserPrincipal principal, Long userId, AdminResetUserPasswordRequest request) {
        PasswordPolicy.validate(request.getNewPassword());
        String orgDb = principal.getOrganizationDbName();
        OrganizationContext.setCurrentOrganization(orgDb);

        OrganizationUser user = organizationUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setNoOfBadLogins(0);
        user.setModifiedBy(principal.getId());
        user.setUpdatedAt(LocalDateTime.now());

        organizationUserRepository.save(user);
        log.info("Password reset by admin id={} for user id={}", principal.getId(), userId);
    }

    public UserResponse updateUser(UserPrincipal principal, Long userId, UpdateUserRequest request, Long organizationId) {
        if (principal == null) {
            throw new org.springframework.security.access.AccessDeniedException("Authentication required");
        }
        boolean isInternalAdmin = isInternalAdmin(principal);
        Organization targetOrg = null;

        if (isInternalAdmin) {
            if (organizationId != null) {
                OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);
                targetOrg = organizationRepository.findById(organizationId)
                        .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", organizationId));
                bankDataSourceProvider.getOrCreateBankDataSource(targetOrg.getDbName(), targetOrg.getDbHost(), targetOrg.getDbPort());
                OrganizationContext.setCurrentOrganization(targetOrg.getDbName());
                OrganizationContext.setCurrentOrgCode(targetOrg.getCode());
            } else {
                // If not supplied, search across organizations to locate which tenant has this user
                OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);
                List<Organization> orgs = organizationRepository.findAll();
                for (Organization candidate : orgs) {
                    try {
                        bankDataSourceProvider.getOrCreateBankDataSource(candidate.getDbName(), candidate.getDbHost(), candidate.getDbPort());
                        OrganizationContext.setCurrentOrganization(candidate.getDbName());
                        OrganizationContext.setCurrentOrgCode(candidate.getCode());
                        if (organizationUserRepository.existsById(userId)) {
                            targetOrg = candidate;
                            break;
                        }
                    } catch (Exception ignored) {
                    }
                }
                if (targetOrg == null) {
                    throw new ResourceNotFoundException("User", "id", userId);
                }
            }
        } else {
            if (organizationId != null && !principal.belongsToOrganization(organizationId)) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "Access denied: You cannot modify users of another organization"
                );
            }
            if (principal.getOrganizationId() != null) {
                OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);
                targetOrg = organizationRepository.findById(principal.getOrganizationId()).orElse(null);
            }
            OrganizationContext.setCurrentOrganization(principal.getOrganizationDbName());
            OrganizationContext.setCurrentOrgCode(principal.getOrganizationCode());
        }

        final Organization finalOrg = targetOrg;
        OrganizationUser user = organizationUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Name
        if (request.getName() != null && !request.getName().isBlank()) {
            String[] parts = request.getName().trim().split("\\s+");
            switch (parts.length) {
                case 1 -> user.setFirstName(parts[0]);
                case 2 -> {
                    user.setFirstName(parts[0]);
                    user.setLastName(parts[1]);
                }
                default -> {
                    user.setFirstName(parts[0]);
                    user.setMiddleName(parts[1]);
                    StringBuilder sb = new StringBuilder();
                    for (int i = 2; i < parts.length; i++) {
                        if (i > 2) {
                            sb.append(" ");
                        }
                        sb.append(parts[i]);
                    }
                    user.setLastName(sb.toString());
                }
            }
        }
        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            user.setFirstName(request.getFirstName().trim());
        }
        if (request.getMiddleName() != null) {
            user.setMiddleName(request.getMiddleName().trim());
        }
        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            user.setLastName(request.getLastName().trim());
        }

        // Email update
        String oldEmail = user.getEmail();
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String newEmail = request.getEmail().trim().toLowerCase();
            if (!newEmail.equalsIgnoreCase(oldEmail)) {
                if (organizationUserRepository.existsByEmail(newEmail)) {
                    throw new BusinessException("EMAIL_EXISTS", "User with email '" + newEmail + "' already exists in this organization");
                }
                user.setEmail(newEmail);
            }
        }

        // Mobile update
        String oldMobile = user.getMobile();
        if (request.getMobile() != null && !request.getMobile().isBlank()) {
            String newMobile = request.getMobile().trim();
            if (!newMobile.equalsIgnoreCase(oldMobile)) {
                user.setMobile(newMobile);
            }
        }

        if (request.getGender() != null && !request.getGender().isBlank()) {
            user.setGender(request.getGender().trim());
        }
        if (request.getDob() != null) {
            user.setDob(request.getDob());
        }
        if (request.getDesignation() != null && !request.getDesignation().isBlank()) {
            String validatedCode = validateAndResolveDesignation(request.getDesignation());
            user.setDesignation(validatedCode);
        }

        // Role update
        if (request.getRoleId() != null || (request.getRoleName() != null && !request.getRoleName().isBlank())) {
            OrganizationRole role = validateAndResolveRole(request.getRoleName(), request.getRoleId(), isInternalAdmin);
            user.setRole(role);
        }


        // Branch update
        Long targetBranchId = request.getLoginBranchId();
        String branchIdentifier = request.getLoginBranch();
        if (targetBranchId == null && branchIdentifier != null && !branchIdentifier.isBlank()) {
            try {
                targetBranchId = Long.valueOf(branchIdentifier.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        if (targetBranchId != null) {
            Branch branch = branchRepository.findById(targetBranchId).orElse(null);
            if (branch != null) {
                user.setLoginBranch(branch);
            }
        } else if (branchIdentifier != null && !branchIdentifier.isBlank()) {
            Branch branch = branchRepository.findByName(branchIdentifier.trim())
                    .or(() -> branchRepository.findByCode(branchIdentifier.trim()))
                    .orElse(null);
            if (branch != null) {
                user.setLoginBranch(branch);
            }
        }

        if (request.getTwoFaEnabled() != null) {
            user.setTwoFaEnabled(request.getTwoFaEnabled());
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            user.setStatus(request.getStatus().trim().toUpperCase());
        }
        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }
        if (request.getMultiBranchAccess() != null) {
            user.setMultiBranchAccess(request.getMultiBranchAccess());
        }
        if (request.getLoginOnHolidays() != null) {
            user.setLoginOnHolidays(request.getLoginOnHolidays());
        }
        if (request.getLoginTime() != null) {
            user.setLoginTime(request.getLoginTime());
        }
        if (request.getLogoutTime() != null) {
            user.setLogoutTime(request.getLogoutTime());
        }
        if (request.getInactiveSessionTimeout() != null) {
            user.setInactiveSessionTimeout(request.getInactiveSessionTimeout());
        }

        user.setModifiedBy(principal.getId());
        user.setUpdatedAt(LocalDateTime.now());

        OrganizationUser updatedUser = organizationUserRepository.save(user);

        // Sync with Master Login Directory if email, phone, or empNo changed
        try {
            String currentDb = OrganizationContext.getCurrentOrganization();
            String currentCode = OrganizationContext.getCurrentOrgCode();
            OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);
            loginDirectoryRepository.findByUserCode(updatedUser.getEmpNo()).ifPresent(ld -> {
                ld.setEmail(updatedUser.getEmail());
                ld.setPhone(updatedUser.getMobile());
                loginDirectoryRepository.save(ld);
            });
            OrganizationContext.setCurrentOrganization(currentDb);
            OrganizationContext.setCurrentOrgCode(currentCode);
        } catch (Exception e) {
            log.warn("Could not sync updated user with login directory: {}", e.getMessage());
        }

        log.info("Successfully updated user id={} username={} by principal={}",
                updatedUser.getId(), updatedUser.getUsername(), principal.getId());

        return mapToResponse(updatedUser, finalOrg);
    }

    public UserResponse mapToResponse(OrganizationUser user, Organization org) {
        String fullName = user.getFullName();
        String roleName = user.getRole() != null ? user.getRole().getName() : null;
        Integer roleId = user.getRole() != null ? user.getRole().getId() : null;
        String branchName = user.getLoginBranch() != null ? user.getLoginBranch().getName() : null;
        Long branchId = user.getLoginBranch() != null ? user.getLoginBranch().getId() : null;

        String rawDesignation = user.getDesignation();
        String designationCode = rawDesignation;
        String designationName = rawDesignation;

        if (rawDesignation != null && !rawDesignation.isBlank()) {
            BankLookupSubType subType = bankLookupSubTypeRepository
                    .findByLookupTypeCodeAndSubTypeCode("10002", rawDesignation.trim())
                    .orElse(null);
            if (subType != null) {
                designationCode = subType.getSubTypeCode();
                designationName = subType.getSubTypeDescription();
            } else {
                var matched = bankLookupSubTypeRepository
                        .findByLookupTypeCodeOrderByDisplayOrderAscIdAsc("10002")
                        .stream()
                        .filter(st -> st.getSubTypeDescription().equalsIgnoreCase(rawDesignation.trim()))
                        .findFirst()
                        .orElse(null);
                if (matched != null) {
                    designationCode = matched.getSubTypeCode();
                    designationName = matched.getSubTypeDescription();
                }
            }
        }

        String roleCode = resolveRoleLookupCode(roleName);

        return UserResponse.builder()
                .pkid(user.getId())
                .id(user.getId())
                .organizationId(org != null ? org.getId() : null)
                .organizationCode(org != null ? org.getCode() : null)
                .organizationName(org != null ? org.getName() : null)
                .empNo(user.getEmpNo())
                .username(user.getUsername())
                .name(fullName)
                .firstName(user.getFirstName())
                .middleName(user.getMiddleName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .gender(user.getGender())
                .dob(user.getDob())
                .designation(designationCode != null ? designationCode : rawDesignation)
                .designationCode(designationCode)
                .designationName(designationName)
                .status(user.getStatus())
                .isActive(user.getIsActive())
                .twoFa(user.getTwoFaEnabled())
                .twoFaEnabled(user.getTwoFaEnabled())
                .role(roleName)
                .roleId(roleId)
                .roleName(roleName)
                .roleCode(roleCode)
                .mBrAccess(user.getMultiBranchAccess())
                .multiBranchAccess(user.getMultiBranchAccess())
                .loginBranch(branchName)
                .loginBranchId(branchId)
                .loginBranchName(branchName)
                .holidayLogin(user.getLoginOnHolidays())
                .loginOnHolidays(user.getLoginOnHolidays())
                .loginTime(user.getLoginTime())
                .logoutTime(user.getLogoutTime())
                .inactiveSessionTimeout(user.getInactiveSessionTimeout())
                .noOfBadLogins(user.getNoOfBadLogins())
                .lastlogindate(user.getLastLoginDate())
                .lastLoginDate(user.getLastLoginDate())
                .lastLoginTime(user.getLastLoginTime())
                .createdBy(user.getCreatedBy())
                .createdDate(user.getCreatedAt())
                .createdAt(user.getCreatedAt())
                .verifiedBy(user.getVerifiedBy())
                .verifiedDate(user.getVerifiedDate())
                .modifiedBy(user.getModifiedBy())
                .modifiedDate(user.getUpdatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private String validateAndResolveDesignation(String submittedDesignation) {
        if (submittedDesignation == null || submittedDesignation.trim().isEmpty()) {
            throw new BusinessException("INVALID_DESIGNATION", "Designation is required. Must be an active option code under lookup type 10002.");
        }
        String code = submittedDesignation.trim();

        // 1. Look up in bank database by code ONLY
        BankLookupSubType subType = bankLookupSubTypeRepository
                .findByLookupTypeCodeAndSubTypeCode("10002", code)
                .orElse(null);

        // 2. If not found in bank DB, check if it exists in master to return a clear syncing error
        if (subType == null) {
            String currentBank = BankContext.getCurrentBank();
            String currentOrg = OrganizationContext.getCurrentOrganization();
            boolean existsInMaster = false;
            try {
                BankContext.setCurrentBank(BankContext.MASTER_DB_NAME);
                OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);
                existsInMaster = masterLookupSubTypeRepository.findByLookupTypeCodeAndSubTypeCode("10002", code).isPresent();
            } finally {
                BankContext.setCurrentBank(currentBank);
                OrganizationContext.setCurrentOrganization(currentOrg);
            }

            if (existsInMaster) {
                throw new BusinessException("INVALID_DESIGNATION", "Designation option code '" + code + "' is not synced in this bank's database. Master lookups need syncing.");
            }
            throw new BusinessException("INVALID_DESIGNATION", "Invalid designation option: '" + code + "'. Must be an active option code under lookup type 10002.");
        }

        if (!Boolean.TRUE.equals(subType.getIsActive())) {
            throw new BusinessException("INVALID_DESIGNATION", "Designation option code '" + code + "' is inactive. Must be an active option code under lookup type 10002.");
        }

        return subType.getSubTypeCode();
    }

    private OrganizationRole validateAndResolveRole(String roleInput, Integer roleId, boolean isInternalAdmin) {
        if (roleId != null) {
            OrganizationRole role = organizationRoleRepository.findById(roleId)
                    .orElseThrow(() -> new BusinessException("INVALID_ROLE", "Role ID " + roleId + " does not exist."));
            validateRoleNameInLookup(role.getName());
            return role;
        }

        if (roleInput == null || roleInput.trim().isEmpty()) {
            roleInput = isInternalAdmin ? "3" : "6"; // default Admin (3) or Viewer (6)
        }

        String code = roleInput.trim();
        String resolvedCode = resolveRoleLookupCode(code);
        if (resolvedCode != null) {
            code = resolvedCode;
        }

        // 1. Look up in bank database by code ONLY
        BankLookupSubType subType = bankLookupSubTypeRepository
                .findByLookupTypeCodeAndSubTypeCode("10003", code)
                .orElse(null);

        // 2. If not found in bank DB, check if it exists in master to return a clear syncing error
        if (subType == null) {
            String currentBank = BankContext.getCurrentBank();
            String currentOrg = OrganizationContext.getCurrentOrganization();
            boolean existsInMaster = false;
            try {
                BankContext.setCurrentBank(BankContext.MASTER_DB_NAME);
                OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);
                existsInMaster = masterLookupSubTypeRepository.findByLookupTypeCodeAndSubTypeCode("10003", code).isPresent();
            } finally {
                BankContext.setCurrentBank(currentBank);
                OrganizationContext.setCurrentOrganization(currentOrg);
            }

            if (existsInMaster) {
                throw new BusinessException("INVALID_ROLE", "Role option code '" + code + "' is not synced in this bank's database. Master lookups need syncing.");
            }
            throw new BusinessException("INVALID_ROLE", "Invalid role option: '" + code + "'. Must be an active option code under lookup type 10003.");
        }

        if (!Boolean.TRUE.equals(subType.getIsActive())) {
            throw new BusinessException("INVALID_ROLE", "Role option code '" + code + "' is inactive. Must be an active option code under lookup type 10003.");
        }

        return mapRoleCodeToEntity(subType.getSubTypeCode(), subType.getSubTypeDescription());
    }

    private OrganizationRole mapRoleCodeToEntity(String subTypeCode, String description) {
        String roleName = switch (subTypeCode) {
            case "1", "2" -> "SUPER_ADMIN";
            case "3" -> "ADMIN";
            case "4" -> "CHECKER";
            case "5" -> "MAKER";
            case "6" -> "VIEWER";
            default -> description.toUpperCase().replace(" ", "_");
        };

        return organizationRoleRepository.findByName(roleName)
                .orElseGet(() -> autoSeedRole(roleName));
    }

    private void validateRoleNameInLookup(String roleName) {
        String code = resolveRoleLookupCode(roleName);
        if (code == null) {
            throw new BusinessException("INVALID_ROLE", "Role '" + roleName + "' is not mapped to lookup 10003.");
        }
        boolean existsAndActive = bankLookupSubTypeRepository
                .findByLookupTypeCodeAndSubTypeCode("10003", code)
                .filter(st -> Boolean.TRUE.equals(st.getIsActive()))
                .isPresent();
        if (!existsAndActive) {
            throw new BusinessException("INVALID_ROLE", "Role option code '" + code + "' is not active under lookup type 10003.");
        }
    }

    private String resolveRoleLookupCode(String roleName) {
        if (roleName == null) return null;
        return switch (roleName.toUpperCase()) {
            case "SUPER_ADMIN" -> "2";
            case "ADMIN" -> "3";
            case "CHECKER" -> "4";
            case "MAKER" -> "5";
            case "VIEWER" -> "6";
            case "CUSTOMER" -> "7";
            default -> null;
        };
    }
    private boolean isInternalAdmin(UserPrincipal principal) {
        return principal != null && principal.isPlatformAdmin();
    }

    private OrganizationRole autoSeedRole(String roleName) {
        String name = roleName != null ? roleName.trim().toUpperCase() : "ADMIN";
        String panel = "CUSTOMER".equalsIgnoreCase(name) ? "CUSTOMER" : "BANK_NBFC";
        String description = switch (name) {
            case "SUPER_ADMIN" ->
                "Bank/NBFC Super Admin — full control within this Bank/NBFC";
            case "ADMIN" ->
                "Bank/NBFC internal admin — configures org and manages users";
            case "MAKER" ->
                "Creates and initiates loan records for Checker approval";
            case "CHECKER" ->
                "Reviews and approves Maker actions";
            case "VIEWER" ->
                "Read-only access";
            case "CUSTOMER" ->
                "Loan applicant";
            default ->
                name + " role";
        };
        return organizationRoleRepository.save(OrganizationRole.builder()
                .name(name)
                .panel(panel)
                .description(description)
                .build());
    }
}

