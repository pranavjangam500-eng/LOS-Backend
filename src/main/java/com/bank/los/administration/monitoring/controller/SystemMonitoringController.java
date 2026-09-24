package com.bank.los.administration.monitoring.controller;

import com.bank.los.administration.monitoring.dto.SystemMetricsResponse;
import com.bank.los.administration.monitoring.service.SystemMonitoringService;
import com.bank.los.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/administration/monitoring")
@RequiredArgsConstructor
@Tag(name = "System Monitoring", description = "Administration metrics and system monitoring")
@SecurityRequirement(name = "BearerAuth")
public class SystemMonitoringController {

    private final SystemMonitoringService systemMonitoringService;

    @GetMapping("/metrics")
    @PreAuthorize("hasRole('INTERNAL_ADMIN')")
    @Operation(summary = "Get system health and multi-organization routing cluster metrics")
    public ResponseEntity<ApiResponse<SystemMetricsResponse>> getMetrics() {
        SystemMetricsResponse metrics = systemMonitoringService.getMetrics();
        return ResponseEntity.ok(ApiResponse.ok(metrics));
    }
}
