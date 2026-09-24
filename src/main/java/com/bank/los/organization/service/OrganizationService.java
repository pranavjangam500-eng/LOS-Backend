package com.bank.los.organization.service;

import com.bank.los.common.exception.BusinessException;
import com.bank.los.common.exception.ResourceNotFoundException;
import com.bank.los.config.TenantContext;
import com.bank.los.master.entity.Organization;
import com.bank.los.master.repository.OrganizationRepository;
import com.bank.los.organization.dto.CreateOrganizationRequest;
import com.bank.los.organization.dto.OrganizationResponse;
import com.bank.los.tenant.entity.Branch;
import com.bank.los.tenant.entity.TenantRole;
import com.bank.los.tenant.repository.BranchRepository;
import com.bank.los.tenant.repository.TenantRoleRepository;
import com.bank.los.user.dto.BranchResponse;
import com.bank.los.user.dto.RoleResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final TenantProvisioningService tenantProvisioningService;
    private final TenantRoleRepository tenantRoleRepository;
    private final BranchRepository branchRepository;

    public List<OrganizationResponse> getAllOrganizations() {
        TenantContext.setCurrentTenant(TenantContext.MASTER_TENANT_ID);
        return organizationRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public OrganizationResponse getOrganizationById(Long id) {
        TenantContext.setCurrentTenant(TenantContext.MASTER_TENANT_ID);
        Organization org = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", id));
        return mapToResponse(org);
    }

    @Transactional
    public OrganizationResponse createOrganization(CreateOrganizationRequest request) {
        TenantContext.setCurrentTenant(TenantContext.MASTER_TENANT_ID);

        if (organizationRepository.existsByCode(request.getCode())) {
            throw new BusinessException("ORGANIZATION_EXISTS", "Organization with code " + request.getCode() + " already exists");
        }
        if (organizationRepository.existsByDbName(request.getDbName())) {
            throw new BusinessException("DATABASE_NAME_IN_USE", "Database name " + request.getDbName() + " is already assigned");
        }

        Organization org = Organization.builder()
                .name(request.getName())
                .code(request.getCode().toUpperCase())
                .type(request.getType().toUpperCase())
                .status("ACTIVE")
                .contactEmail(request.getContactEmail())
                .contactPhone(request.getContactPhone())
                .dbName(request.getDbName())
                .dbHost(request.getDbHost())
                .dbPort(request.getDbPort())
                .build();

        Organization saved = organizationRepository.save(org);

        // Auto-provision tenant DB schema, default roles (ADMIN, MAKER, etc.), permissions, and primary branch
        tenantProvisioningService.provisionTenant(
                saved.getDbName(),
                saved.getCode(),
                saved.getName(),
                saved.getDbHost(),
                saved.getDbPort()
        );

        return mapToResponse(saved);
    }

    public List<RoleResponse> getOrganizationRoles(Long orgId) {
        TenantContext.setCurrentTenant(TenantContext.MASTER_TENANT_ID);
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", orgId));

        TenantContext.setCurrentTenant(org.getDbName());
        TenantContext.setCurrentOrgCode(org.getCode());
        try {
            return tenantRoleRepository.findAll().stream()
                    .map(this::mapRoleToResponse)
                    .collect(Collectors.toList());
        } finally {
            TenantContext.clear();
        }
    }

    public List<BranchResponse> getOrganizationBranches(Long orgId) {
        TenantContext.setCurrentTenant(TenantContext.MASTER_TENANT_ID);
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", orgId));

        TenantContext.setCurrentTenant(org.getDbName());
        TenantContext.setCurrentOrgCode(org.getCode());
        try {
            return branchRepository.findAll().stream()
                    .map(this::mapBranchToResponse)
                    .collect(Collectors.toList());
        } finally {
            TenantContext.clear();
        }
    }

    private OrganizationResponse mapToResponse(Organization org) {
        return OrganizationResponse.builder()
                .id(org.getId())
                .name(org.getName())
                .code(org.getCode())
                .type(org.getType())
                .status(org.getStatus())
                .contactEmail(org.getContactEmail())
                .contactPhone(org.getContactPhone())
                .dbName(org.getDbName())
                .dbHost(org.getDbHost())
                .dbPort(org.getDbPort())
                .createdAt(org.getCreatedAt())
                .build();
    }

    private RoleResponse mapRoleToResponse(TenantRole role) {
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .panel(role.getPanel())
                .description(role.getDescription())
                .build();
    }

    private BranchResponse mapBranchToResponse(Branch branch) {
        return BranchResponse.builder()
                .id(branch.getId())
                .name(branch.getName())
                .code(branch.getCode())
                .address(branch.getAddress())
                .city(branch.getCity())
                .state(branch.getState())
                .pincode(branch.getPincode())
                .status(branch.getStatus())
                .build();
    }
}
