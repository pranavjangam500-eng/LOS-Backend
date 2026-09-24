package com.bank.los.administration.audit.controller;

import com.bank.los.administration.audit.entity.AdminAuditLog;
import com.bank.los.administration.audit.service.AdminAuditService;
import com.bank.los.common.response.ApiResponse;
import com.bank.los.common.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/administration/audit")
@RequiredArgsConstructor
@Tag(name = "Administration Audit", description = "Endpoints for platform administration audit trails and logs")
@SecurityRequirement(name = "BearerAuth")
public class AdminAuditController {

    private final AdminAuditService adminAuditService;

    @GetMapping
    @PreAuthorize("hasRole('INTERNAL_ADMIN')")
    @Operation(summary = "Get platform administration audit logs (paginated)")
    public ResponseEntity<ApiResponse<PageResponse<AdminAuditLog>>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<AdminAuditLog> logs = adminAuditService.getAuditLogs(page, size);
        return ResponseEntity.ok(ApiResponse.ok(logs));
    }
}
