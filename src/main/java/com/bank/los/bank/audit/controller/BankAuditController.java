package com.bank.los.bank.audit.controller;

import com.bank.los.bank.audit.dto.BankAuditLogResponse;
import com.bank.los.bank.audit.service.BankAuditService;
import com.bank.los.common.response.ApiResponse;
import com.bank.los.common.response.PageResponse;
import com.bank.los.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Bank Audit Logs", description = "Endpoints for bank activity and compliance audit logs")
@SecurityRequirement(name = "BearerAuth")
public class BankAuditController {

    private final BankAuditService bankAuditService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'VIEWER')")
    @Operation(summary = "Get paginated activity audit logs for current bank")
    public ResponseEntity<ApiResponse<PageResponse<BankAuditLogResponse>>> getAuditLogs(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20) Pageable pageable) {
        PageResponse<BankAuditLogResponse> logs = bankAuditService.getAuditLogs(principal, pageable);
        return ResponseEntity.ok(ApiResponse.ok(logs));
    }
}
