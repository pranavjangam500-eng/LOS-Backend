package com.bank.los.bank.audit.service;

import com.bank.los.bank.audit.dto.BankAuditLogResponse;
import com.bank.los.bank.audit.entity.BankAuditLog;
import com.bank.los.bank.audit.repository.BankAuditLogRepository;
import com.bank.los.common.response.PageResponse;
import com.bank.los.config.OrganizationContext;
import com.bank.los.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankAuditService {

    private final BankAuditLogRepository bankAuditLogRepository;

    public void logAction(UserPrincipal principal, String action, String resource, String details, String ip) {
        OrganizationContext.setCurrentOrganization(principal.getOrganizationDbName());
        try {
            bankAuditLogRepository.save(BankAuditLog.builder()
                    .userId(principal.getId())
                    .userCode(principal.getUserCode())
                    .roleName(principal.getRole())
                    .action(action)
                    .resource(resource)
                    .details(details)
                    .ipAddress(ip)
                    .createdAt(LocalDateTime.now())
                    .build());
        } catch (Exception ex) {
            log.debug("Bank audit notice: {}", ex.getMessage());
        }
    }

    public PageResponse<BankAuditLogResponse> getAuditLogs(UserPrincipal principal, Pageable pageable) {
        OrganizationContext.setCurrentOrganization(principal.getOrganizationDbName());
        Page<BankAuditLog> page = bankAuditLogRepository.findAll(pageable);
        return PageResponse.of(page.map(this::mapToResponse));
    }

    private BankAuditLogResponse mapToResponse(BankAuditLog log) {
        return BankAuditLogResponse.builder()
                .id(log.getId())
                .userId(log.getUserId())
                .userCode(log.getUserCode())
                .roleName(log.getRoleName())
                .action(log.getAction())
                .resource(log.getResource())
                .details(log.getDetails())
                .ipAddress(log.getIpAddress())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
