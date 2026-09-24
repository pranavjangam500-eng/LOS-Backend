package com.bank.los.administration.administrator.service;

import com.bank.los.administration.administrator.dto.AdministratorResponse;
import com.bank.los.administration.administrator.dto.CreateAdministratorRequest;
import com.bank.los.administration.master.entity.InternalUser;
import com.bank.los.administration.master.entity.LoginDirectory;
import com.bank.los.administration.master.entity.MasterRole;
import com.bank.los.administration.master.entity.Organization;
import com.bank.los.administration.master.repository.InternalUserRepository;
import com.bank.los.administration.master.repository.LoginDirectoryRepository;
import com.bank.los.administration.master.repository.MasterRoleRepository;
import com.bank.los.administration.master.repository.OrganizationRepository;
import com.bank.los.common.constant.ApplicationConstants;
import com.bank.los.common.exception.BusinessException;
import com.bank.los.common.exception.ResourceNotFoundException;
import com.bank.los.common.validation.PasswordPolicy;
import com.bank.los.config.OrganizationContext;
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
public class AdministratorService {

    private final InternalUserRepository internalUserRepository;
    private final MasterRoleRepository masterRoleRepository;
    private final LoginDirectoryRepository loginDirectoryRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;

    public List<AdministratorResponse> getAllAdministrators() {
        OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);
        return internalUserRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public AdministratorResponse getAdministratorById(Long id) {
        OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);
        InternalUser user = internalUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Administrator", "id", id));
        return mapToResponse(user);
    }

    @Transactional
    public AdministratorResponse createAdministrator(CreateAdministratorRequest request) {
        OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);
        PasswordPolicy.validate(request.getPassword());

        if (internalUserRepository.existsByUsername(request.getUsername().trim().toLowerCase())) {
            throw new BusinessException("USERNAME_EXISTS", "Username is already taken");
        }
        if (internalUserRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new BusinessException("EMAIL_EXISTS", "Email is already registered");
        }

        MasterRole role = masterRoleRepository.findByName(ApplicationConstants.Roles.INTERNAL_ADMIN)
                .orElseGet(() -> masterRoleRepository.save(MasterRole.builder()
                        .name(ApplicationConstants.Roles.INTERNAL_ADMIN)
                        .panel(ApplicationConstants.Panels.INTERNAL)
                        .description("Platform master administrator")
                        .build()));

        String empNo = request.getEmpNo();
        if (empNo == null || empNo.isBlank()) {
            empNo = "EMP-MST-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        }

        InternalUser user = InternalUser.builder()
                .empNo(empNo)
                .username(request.getUsername().trim().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .firstName(request.getFirstName())
                .middleName(request.getMiddleName())
                .lastName(request.getLastName())
                .email(request.getEmail().trim().toLowerCase())
                .mobile(request.getMobile())
                .designation(request.getDesignation() != null ? request.getDesignation() : "Platform Administrator")
                .isActive(true)
                .status(ApplicationConstants.UserStatus.OPERATIVE)
                .loginOnHolidays(true)
                .inactiveSessionTimeout(3600)
                .noOfBadLogins(0)
                .build();

        InternalUser saved = internalUserRepository.save(user);

        // Link in master login directory
        Organization defaultOrg = organizationRepository.findAll().stream().findFirst().orElse(null);
        if (defaultOrg != null) {
            loginDirectoryRepository.save(LoginDirectory.builder()
                    .userCode(saved.getEmpNo())
                    .email(saved.getEmail())
                    .phone(saved.getMobile())
                    .organization(defaultOrg)
                    .userType(ApplicationConstants.UserTypes.INTERNAL)
                    .build());
        }

        return mapToResponse(saved);
    }

    private AdministratorResponse mapToResponse(InternalUser user) {
        return AdministratorResponse.builder()
                .id(user.getId())
                .empNo(user.getEmpNo())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .middleName(user.getMiddleName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .designation(user.getDesignation())
                .roleName(user.getRole() != null ? user.getRole().getName() : null)
                .status(user.getStatus())
                .isActive(user.getIsActive())
                .lastLoginDate(user.getLastLoginDate())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
