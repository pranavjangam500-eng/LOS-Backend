package com.bank.los.administration.organization.service;

import com.bank.los.administration.master.entity.Organization;
import com.bank.los.administration.master.repository.OrganizationRepository;
import com.bank.los.administration.organization.dto.CreateOrganizationRequest;
import com.bank.los.administration.organization.dto.OrganizationResponse;
import com.bank.los.bank.branch.dto.BranchResponse;
import com.bank.los.bank.master.entity.Branch;
import com.bank.los.bank.master.entity.OrganizationRole;
import com.bank.los.bank.master.repository.BranchRepository;
import com.bank.los.bank.master.repository.OrganizationRoleRepository;
import com.bank.los.bank.user.dto.RoleResponse;
import com.bank.los.common.exception.BusinessException;
import com.bank.los.common.exception.ResourceNotFoundException;
import com.bank.los.config.OrganizationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationProvisioningService organizationProvisioningService;
    private final OrganizationRoleRepository organizationRoleRepository;
    private final BranchRepository branchRepository;

    public List<OrganizationResponse> getAllOrganizations() {
        OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);
        return organizationRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public OrganizationResponse getOrganizationById(Long id) {
        OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);
        Organization org = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", id));
        return mapToResponse(org);
    }

    public OrganizationResponse createOrganization(CreateOrganizationRequest request) {
        OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);

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

        // Auto-provision organization DB schema, default roles (ADMIN, MAKER, etc.), permissions, and primary branch
        organizationProvisioningService.provisionOrganization(
                saved.getDbName(),
                saved.getCode(),
                saved.getName(),
                saved.getDbHost(),
                saved.getDbPort()
        );

        return mapToResponse(saved);
    }

    public List<RoleResponse> getOrganizationRoles(Long orgId) {
        OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", orgId));

        OrganizationContext.setCurrentOrganization(org.getDbName());
        OrganizationContext.setCurrentOrgCode(org.getCode());
        try {
            return organizationRoleRepository.findAll().stream()
                    .map(this::mapRoleToResponse)
                    .collect(Collectors.toList());
        } finally {
            OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);
        }
    }

    public List<BranchResponse> getOrganizationBranches(Long orgId) {
        OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", orgId));

        OrganizationContext.setCurrentOrganization(org.getDbName());
        OrganizationContext.setCurrentOrgCode(org.getCode());
        try {
            return branchRepository.findAll().stream()
                    .map(this::mapBranchToResponse)
                    .collect(Collectors.toList());
        } finally {
            OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);
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

    private RoleResponse mapRoleToResponse(OrganizationRole role) {
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
