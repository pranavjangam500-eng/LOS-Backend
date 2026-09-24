package com.bank.los.bank.lead.service;

import com.bank.los.bank.lead.dto.CreateLeadRequest;
import com.bank.los.bank.lead.dto.LeadResponse;
import com.bank.los.bank.lead.entity.Lead;
import com.bank.los.bank.lead.repository.LeadRepository;
import com.bank.los.common.exception.ResourceNotFoundException;
import com.bank.los.common.response.PageResponse;
import com.bank.los.config.OrganizationContext;
import com.bank.los.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeadService {

    private final LeadRepository leadRepository;

    public PageResponse<LeadResponse> getLeads(UserPrincipal principal, Pageable pageable) {
        OrganizationContext.setCurrentOrganization(principal.getOrganizationDbName());
        Page<Lead> page = leadRepository.findAll(pageable);
        return PageResponse.of(page.map(this::mapToResponse));
    }

    public LeadResponse getLeadById(UserPrincipal principal, Long id) {
        OrganizationContext.setCurrentOrganization(principal.getOrganizationDbName());
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead", "id", id));
        return mapToResponse(lead);
    }

    @Transactional
    public LeadResponse createLead(UserPrincipal principal, CreateLeadRequest request) {
        OrganizationContext.setCurrentOrganization(principal.getOrganizationDbName());

        String leadNumber = "LEAD-" + principal.getOrganizationCode() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        Lead lead = Lead.builder()
                .leadNumber(leadNumber)
                .customerName(request.getCustomerName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .loanProductType(request.getLoanProductType())
                .requestedAmount(request.getRequestedAmount())
                .branchId(request.getBranchId() != null ? request.getBranchId() : principal.getBranchId())
                .assignedToUserId(principal.getId())
                .status("NEW")
                .remarks(request.getRemarks())
                .build();

        Lead saved = leadRepository.save(lead);
        log.info("Lead created: leadNumber={}, org={}", leadNumber, principal.getOrganizationCode());
        return mapToResponse(saved);
    }

    private LeadResponse mapToResponse(Lead lead) {
        return LeadResponse.builder()
                .id(lead.getId())
                .leadNumber(lead.getLeadNumber())
                .customerName(lead.getCustomerName())
                .email(lead.getEmail())
                .phone(lead.getPhone())
                .loanProductType(lead.getLoanProductType())
                .requestedAmount(lead.getRequestedAmount())
                .assignedToUserId(lead.getAssignedToUserId())
                .branchId(lead.getBranchId())
                .status(lead.getStatus())
                .remarks(lead.getRemarks())
                .createdAt(lead.getCreatedAt())
                .build();
    }
}
