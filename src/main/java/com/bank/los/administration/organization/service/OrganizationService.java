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
import java.util.UUID;
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

    public OrganizationResponse getOrganizationByUuid(UUID uuid) {
        OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);
        Organization org = organizationRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "uuid", uuid));
        return mapToResponse(org);
    }

    public OrganizationResponse createOrganization(CreateOrganizationRequest request) {
        OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);

        String code = request.getBankCode();
        if (code == null || code.isBlank()) {
            String baseName = request.getBankName() != null ? request.getBankName() : 
                    (request.getLegalName() != null ? request.getLegalName() : "BANK");
            String clean = baseName.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
            if (clean.length() > 6) {
                clean = clean.substring(0, 6);
            }
            if (clean.isBlank()) {
                clean = "BANK";
            }
            int seq = 1;
            code = clean + String.format("%02d", seq);
            while (organizationRepository.existsByCode(code) || organizationRepository.existsByBankCode(code)) {
                seq++;
                code = clean + String.format("%02d", seq);
            }
        } else {
            code = code.trim().toUpperCase();
            if (organizationRepository.existsByCode(code) || organizationRepository.existsByBankCode(code)) {
                throw new BusinessException("BANK_CODE_IN_USE", "Bank code " + code + " is already assigned");
            }
        }

        String name = request.getBankName() != null ? request.getBankName() : request.getLegalName();
        String shortName = name != null ? name.split("\\s+")[0] : code;

        String dbName = request.getDbName();
        if (dbName == null || dbName.isBlank()) {
            dbName = "los_" + code.toLowerCase().replaceAll("[^a-z0-9]", "") + "_db";
        }

        if (organizationRepository.existsByDbName(dbName)) {
            throw new BusinessException("DATABASE_NAME_IN_USE", "Database name " + dbName + " is already assigned");
        }

        UUID orgUuid = request.getId() != null ? request.getId() : UUID.randomUUID();

        Organization org = Organization.builder()
                .uuid(orgUuid)
                .bankCode(code)
                .bankName(request.getBankName())
                .legalName(request.getLegalName())
                .shortName(shortName)
                .bankType(request.getBankType() != null ? request.getBankType().toUpperCase() : "BANK")
                .registrationNumber(request.getRegistrationNumber())
                .pan(request.getPan() != null ? request.getPan().toUpperCase() : null)
                .cin(request.getCin() != null ? request.getCin().toUpperCase() : null)
                .website(request.getWebsite())
                .logo(request.getLogo())
                .regulatoryAuthorityId(request.getRegulatoryAuthorityId())
                .regulatoryStatus(request.getRegulatoryStatus() != null ? request.getRegulatoryStatus().toUpperCase() : "ACTIVE")
                .country(request.getCountry() != null ? request.getCountry() : "India")
                .status("ACTIVE")
                .contactEmail(request.getContactEmail())
                .contactPhone(request.getContactPhone())
                .dbName(dbName)
                .dbHost(request.getDbHost() != null ? request.getDbHost() : "localhost")
                .dbPort(request.getDbPort() != null ? request.getDbPort() : 5432)
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

    public OrganizationResponse mapToResponse(Organization org) {
        return OrganizationResponse.builder()
                .id(org.getUuid() != null ? org.getUuid() : UUID.nameUUIDFromBytes(String.valueOf(org.getId()).getBytes()))
                .pkid(org.getId())
                .bankCode(org.getBankCode())
                .bankName(org.getBankName() != null ? org.getBankName() : org.getName())
                .legalName(org.getLegalName())
                .bankType(org.getBankType() != null ? org.getBankType() : org.getType())
                .registrationNumber(org.getRegistrationNumber())
                .pan(org.getPan())
                .cin(org.getCin())
                .website(org.getWebsite())
                .logo(org.getLogo())
                .regulatoryAuthorityId(org.getRegulatoryAuthorityId())
                .regulatoryStatus(org.getRegulatoryStatus())
                .country(org.getCountry())
                .status(org.getStatus())
                .contactEmail(org.getContactEmail())
                .contactPhone(org.getContactPhone())
                .dbName(org.getDbName())
                .dbHost(org.getDbHost())
                .dbPort(org.getDbPort())
                .createdAt(org.getCreatedAt())
                .updatedAt(org.getUpdatedAt())
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

