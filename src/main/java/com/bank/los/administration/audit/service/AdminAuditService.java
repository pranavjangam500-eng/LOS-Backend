package com.bank.los.administration.audit.service;

import com.bank.los.administration.audit.entity.AdminAuditLog;
import com.bank.los.administration.audit.repository.AdminAuditLogRepository;
import com.bank.los.common.response.PageResponse;
import com.bank.los.config.OrganizationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAuditService {

    private final AdminAuditLogRepository adminAuditLogRepository;

    public void logAdminAction(Long adminId, String username, String action, String resource, String orgCode, String details, String ip) {
        OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);
        try {
            adminAuditLogRepository.save(AdminAuditLog.builder()
                    .adminId(adminId)
                    .adminUsername(username)
                    .action(action)
                    .resource(resource)
                    .organizationCode(orgCode)
                    .details(details)
                    .ipAddress(ip)
                    .createdAt(LocalDateTime.now())
                    .build());
        } catch (Exception ex) {
            log.debug("Audit logging notice: {}", ex.getMessage());
        }
    }

    public PageResponse<AdminAuditLog> getAuditLogs(int page, int size) {
        OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);
        Page<AdminAuditLog> p = adminAuditLogRepository.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return PageResponse.of(p.getContent(), page, size, p.getTotalElements());
    }
}
