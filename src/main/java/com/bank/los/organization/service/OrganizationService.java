package com.bank.los.organization.service;

import com.bank.los.common.exception.BusinessException;
import com.bank.los.common.exception.ResourceNotFoundException;
import com.bank.los.config.TenantContext;
import com.bank.los.master.entity.Organization;
import com.bank.los.master.repository.OrganizationRepository;
import com.bank.los.organization.dto.CreateOrganizationRequest;
import com.bank.los.organization.dto.OrganizationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

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
        return mapToResponse(saved);
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
}
