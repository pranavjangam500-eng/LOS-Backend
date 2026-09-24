package com.bank.los.bank.dashboard.controller;

import com.bank.los.bank.dashboard.dto.*;
import com.bank.los.bank.dashboard.service.BankDashboardService;
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
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Bank Dashboards", description = "Role-restricted dashboard data APIs for Bank panel users")
@SecurityRequirement(name = "BearerAuth")
public class BankDashboardController {

    private final BankDashboardService bankDashboardService;

    @GetMapping({"/admin", "/tenant-admin"})
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Bank/NBFC Admin Dashboard — ADMIN or SUPER_ADMIN (Full control within this Bank/NBFC)")
    public ResponseEntity<ApiResponse<BankAdminDashboardDto>> getBankAdminDashboard(
            @AuthenticationPrincipal UserPrincipal principal) {
        BankAdminDashboardDto data = bankDashboardService.getBankAdminDashboard(principal);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @GetMapping("/maker")
    @PreAuthorize("hasRole('MAKER')")
    @Operation(summary = "Maker Dashboard — MAKER only (Creates/initiates loan applications for Checker approval)")
    public ResponseEntity<ApiResponse<MakerDashboardDto>> getMakerDashboard(
            @AuthenticationPrincipal UserPrincipal principal) {
        MakerDashboardDto data = bankDashboardService.getMakerDashboard(principal);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @GetMapping("/checker")
    @PreAuthorize("hasRole('CHECKER')")
    @Operation(summary = "Checker Dashboard — CHECKER only (Reviews and approves/rejects Maker loan actions)")
    public ResponseEntity<ApiResponse<CheckerDashboardDto>> getCheckerDashboard(
            @AuthenticationPrincipal UserPrincipal principal) {
        CheckerDashboardDto data = bankDashboardService.getCheckerDashboard(principal);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @GetMapping("/viewer")
    @PreAuthorize("hasRole('VIEWER')")
    @Operation(summary = "Viewer Dashboard — VIEWER only (Read-only analytics and reports)")
    public ResponseEntity<ApiResponse<ViewerDashboardDto>> getViewerDashboard(
            @AuthenticationPrincipal UserPrincipal principal) {
        ViewerDashboardDto data = bankDashboardService.getViewerDashboard(principal);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @GetMapping("/customer")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Customer Portal Dashboard — CUSTOMER only (Loan applicant portal)")
    public ResponseEntity<ApiResponse<CustomerDashboardDto>> getCustomerDashboard(
            @AuthenticationPrincipal UserPrincipal principal) {
        CustomerDashboardDto data = bankDashboardService.getCustomerDashboard(principal);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }
}
