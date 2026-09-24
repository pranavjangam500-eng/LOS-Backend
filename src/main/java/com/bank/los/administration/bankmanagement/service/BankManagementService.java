package com.bank.los.administration.bankmanagement.service;

import com.bank.los.administration.bankmanagement.dto.BankStatusUpdateRequest;
import com.bank.los.administration.master.entity.Organization;
import com.bank.los.administration.master.repository.OrganizationRepository;
import com.bank.los.administration.organization.dto.OrganizationResponse;
import com.bank.los.common.exception.ResourceNotFoundException;
import com.bank.los.config.OrganizationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankManagementService {

    private final OrganizationRepository organizationRepository;

    @Transactional
    public OrganizationResponse updateBankStatus(Long organizationId, BankStatusUpdateRequest request) {
        OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);

        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", organizationId));

        org.setStatus(request.getStatus().toUpperCase());
        Organization updated = organizationRepository.save(org);
        log.info("Organization id={} code={} status changed to {} by administrator",
                org.getId(), org.getCode(), org.getStatus());

        return OrganizationResponse.builder()
                .id(updated.getId())
                .name(updated.getName())
                .code(updated.getCode())
                .type(updated.getType())
                .status(updated.getStatus())
                .contactEmail(updated.getContactEmail())
                .contactPhone(updated.getContactPhone())
                .dbName(updated.getDbName())
                .dbHost(updated.getDbHost())
                .dbPort(updated.getDbPort())
                .createdAt(updated.getCreatedAt())
                .build();
    }
}
