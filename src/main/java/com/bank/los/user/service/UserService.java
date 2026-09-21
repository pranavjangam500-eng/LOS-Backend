package com.bank.los.user.service;

import com.bank.los.common.exception.BusinessException;
import com.bank.los.common.exception.ResourceNotFoundException;
import com.bank.los.common.util.StringUtil;
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
import com.bank.los.user.dto.CreateUserRequest;
import com.bank.los.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

        if (tenantUserRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("EMAIL_EXISTS", "User with email " + request.getEmail() + " already exists in this organization");
        }

        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", request.getBranchId()));

        TenantRole role = tenantRoleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", request.getRoleId()));

        TenantUser user = TenantUser.builder()
                .userCode(request.getUserCode())
                .branch(branch)
                .role(role)
                .firstName(request.getFirstName())
                .middleName(request.getMiddleName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .isActive(true)
                .failedLoginAttempts(0)
                .build();

        TenantUser savedUser = tenantUserRepository.save(user);

        // Register in Master Login Directory
        try {
            TenantContext.setCurrentTenant(TenantContext.MASTER_TENANT_ID);
            Organization org = organizationRepository.findByCode(orgCode)
                    .orElseThrow(() -> new ResourceNotFoundException("Organization", "code", orgCode));

            LoginDirectory loginDirectory = LoginDirectory.builder()
                    .userCode(savedUser.getUserCode())
                    .email(savedUser.getEmail())
                    .phone(savedUser.getPhone())
                    .organization(org)
                    .userType("STAFF")
                    .build();

            loginDirectoryRepository.save(loginDirectory);
        } finally {
            TenantContext.setCurrentTenant(tenantDb);
        }

        return mapToResponse(savedUser);
    }

    private UserResponse mapToResponse(TenantUser user) {
        return UserResponse.builder()
                .id(user.getId())
                .userCode(user.getUserCode())
                .branchId(user.getBranch() != null ? user.getBranch().getId() : null)
                .branchName(user.getBranch() != null ? user.getBranch().getName() : null)
                .roleId(user.getRole() != null ? user.getRole().getId() : null)
                .roleName(user.getRole() != null ? user.getRole().getName() : null)
                .firstName(user.getFirstName())
                .middleName(user.getMiddleName())
                .lastName(user.getLastName())
                .fullName(StringUtil.getFullName(user.getFirstName(), user.getMiddleName(), user.getLastName()))
                .email(user.getEmail())
                .phone(user.getPhone())
                .isActive(user.getIsActive())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
