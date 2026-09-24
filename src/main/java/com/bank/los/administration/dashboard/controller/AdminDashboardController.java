package com.bank.los.administration.dashboard.controller;

import com.bank.los.administration.dashboard.dto.InternalAdminDashboardDto;
import com.bank.los.administration.dashboard.service.AdminDashboardService;
import com.bank.los.common.response.ApiResponse;
import com.bank.los.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/administration/dashboard", "/api/v1/dashboard/internal-admin"})
@RequiredArgsConstructor
@Tag(name = "Administration Dashboard", description = "Endpoints for overall system & platform administrator dashboard")
@SecurityRequirement(name = "BearerAuth")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping
    @PreAuthorize("hasRole('INTERNAL_ADMIN')")
    @Operation(summary = "Platform Master Administration Dashboard")
    public ResponseEntity<ApiResponse<InternalAdminDashboardDto>> getDashboard(
            @AuthenticationPrincipal UserPrincipal principal) {
        InternalAdminDashboardDto data = adminDashboardService.getInternalAdminDashboard(principal);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }
}
