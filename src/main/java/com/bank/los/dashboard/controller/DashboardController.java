package com.bank.los.dashboard.controller;

import com.bank.los.common.response.ApiResponse;
import com.bank.los.dashboard.dto.*;
import com.bank.los.dashboard.service.DashboardService;
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
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Role-Based Dashboards", description = "Role-restricted dashboard data APIs for each organization role")
@SecurityRequirement(name = "BearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/internal-admin")
    @PreAuthorize("hasRole('INTERNAL_ADMIN')")
    @Operation(summary = "Platform Master Dashboard — INTERNAL_ADMIN only (Platform team; manages tenants and features)")
    public ResponseEntity<ApiResponse<InternalAdminDashboardDto>> getInternalAdminDashboard(
            @AuthenticationPrincipal UserPrincipal principal) {
        InternalAdminDashboardDto data = dashboardService.getInternalAdminDashboard(principal);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @GetMapping("/tenant-admin")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Bank/NBFC Tenant Admin Dashboard — SUPER_ADMIN only (Full control within this NBFC/Bank)")
    public ResponseEntity<ApiResponse<TenantAdminDashboardDto>> getTenantAdminDashboard(
            @AuthenticationPrincipal UserPrincipal principal) {
        TenantAdminDashboardDto data = dashboardService.getTenantAdminDashboard(principal);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @GetMapping("/maker")
    @PreAuthorize("hasRole('MAKER')")
    @Operation(summary = "Maker Dashboard — MAKER only (Creates/initiates loan applications for Checker approval)")
    public ResponseEntity<ApiResponse<MakerDashboardDto>> getMakerDashboard(
            @AuthenticationPrincipal UserPrincipal principal) {
        MakerDashboardDto data = dashboardService.getMakerDashboard(principal);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @GetMapping("/checker")
    @PreAuthorize("hasRole('CHECKER')")
    @Operation(summary = "Checker Dashboard — CHECKER only (Reviews and approves/rejects Maker loan actions)")
    public ResponseEntity<ApiResponse<CheckerDashboardDto>> getCheckerDashboard(
            @AuthenticationPrincipal UserPrincipal principal) {
        CheckerDashboardDto data = dashboardService.getCheckerDashboard(principal);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @GetMapping("/viewer")
    @PreAuthorize("hasRole('VIEWER')")
    @Operation(summary = "Viewer Dashboard — VIEWER only (Read-only analytics and reports)")
    public ResponseEntity<ApiResponse<ViewerDashboardDto>> getViewerDashboard(
            @AuthenticationPrincipal UserPrincipal principal) {
        ViewerDashboardDto data = dashboardService.getViewerDashboard(principal);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @GetMapping("/customer")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Customer Portal Dashboard — CUSTOMER only (Loan applicant portal)")
    public ResponseEntity<ApiResponse<CustomerDashboardDto>> getCustomerDashboard(
            @AuthenticationPrincipal UserPrincipal principal) {
        CustomerDashboardDto data = dashboardService.getCustomerDashboard(principal);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }
}
