package com.bank.los.user.controller;

import com.bank.los.common.response.ApiResponse;
import com.bank.los.organization.service.OrganizationService;
import com.bank.los.security.UserPrincipal;
import com.bank.los.user.dto.BranchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/branches")
@RequiredArgsConstructor
@Tag(name = "Branches", description = "Query available branches for bank staff assignment")
@SecurityRequirement(name = "BearerAuth")
public class BranchController {

    private final OrganizationService organizationService;

    @GetMapping
    @PreAuthorize("hasAnyRole('INTERNAL_ADMIN', 'ADMIN')")
    @Operation(summary = "Get available branches for a bank/organization")
    public ResponseEntity<ApiResponse<List<BranchResponse>>> getBranches(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "Bank/Organization ID (required for Super Admin, defaults to current bank for Bank Admin)")
            @RequestParam(required = false) Long organizationId) {

        Long targetOrgId = organizationId;
        if (targetOrgId == null) {
            targetOrgId = 1L;
        }

        List<BranchResponse> branches = organizationService.getOrganizationBranches(targetOrgId);
        return ResponseEntity.ok(ApiResponse.ok(branches));
    }
}
