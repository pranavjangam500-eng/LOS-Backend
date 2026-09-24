package com.bank.los.bank.branch.controller;

import com.bank.los.bank.branch.dto.BranchResponse;
import com.bank.los.bank.branch.dto.CreateBranchRequest;
import com.bank.los.bank.branch.service.BranchService;
import com.bank.los.common.response.ApiResponse;
import com.bank.los.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/branches")
@RequiredArgsConstructor
@Tag(name = "Bank Branches", description = "Endpoints for managing branches within an organization")
@SecurityRequirement(name = "BearerAuth")
public class BranchController {

    private final BranchService branchService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MAKER', 'CHECKER', 'VIEWER')")
    @Operation(summary = "List all active branches in the current organization")
    public ResponseEntity<ApiResponse<List<BranchResponse>>> getAllBranches(@AuthenticationPrincipal UserPrincipal principal) {
        List<BranchResponse> branches = branchService.getAllBranches(principal);
        return ResponseEntity.ok(ApiResponse.ok(branches));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MAKER', 'CHECKER', 'VIEWER')")
    @Operation(summary = "Get branch details by ID")
    public ResponseEntity<ApiResponse<BranchResponse>> getBranchById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        BranchResponse branch = branchService.getBranchById(principal, id);
        return ResponseEntity.ok(ApiResponse.ok(branch));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Create a new branch within this bank/NBFC")
    public ResponseEntity<ApiResponse<BranchResponse>> createBranch(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateBranchRequest request) {
        BranchResponse branch = branchService.createBranch(principal, request);
        return ResponseEntity.ok(ApiResponse.ok("Branch created successfully", branch));
    }
}
