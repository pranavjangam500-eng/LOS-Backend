package com.bank.los.administration.organization.controller;

import com.bank.los.administration.organization.dto.CreateOrganizationRequest;
import com.bank.los.administration.organization.dto.OrganizationResponse;
import com.bank.los.administration.organization.service.OrganizationService;
import com.bank.los.bank.branch.dto.BranchResponse;
import com.bank.los.bank.user.dto.RoleResponse;
import com.bank.los.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
@Tag(name = "Organization Management", description = "Endpoints for onboarding and managing Bank/NBFC organizations (Internal Admin only)")
@SecurityRequirement(name = "BearerAuth")
public class OrganizationController {

    private final OrganizationService organizationService;

    @GetMapping
    @PreAuthorize("hasRole('INTERNAL_ADMIN')")
    @Operation(summary = "List all onboarded organizations / financial institutions")
    public ResponseEntity<ApiResponse<List<OrganizationResponse>>> getAllOrganizations() {
        List<OrganizationResponse> orgs = organizationService.getAllOrganizations();
        return ResponseEntity.ok(ApiResponse.ok(orgs));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('INTERNAL_ADMIN')")
    @Operation(summary = "Get organization details by ID")
    public ResponseEntity<ApiResponse<OrganizationResponse>> getOrganizationById(@PathVariable Long id) {
        OrganizationResponse org = organizationService.getOrganizationById(id);
        return ResponseEntity.ok(ApiResponse.ok(org));
    }

    @PostMapping
    @PreAuthorize("hasRole('INTERNAL_ADMIN')")
    @Operation(summary = "Onboard a new Bank/NBFC organization with dedicated database provisioning")
    public ResponseEntity<ApiResponse<OrganizationResponse>> createOrganization(
            @Valid @RequestBody CreateOrganizationRequest request) {
        OrganizationResponse org = organizationService.createOrganization(request);
        return ResponseEntity.ok(ApiResponse.ok("Organization onboarded and database provisioned successfully", org));
    }

    @GetMapping("/{id}/roles")
    @PreAuthorize("hasRole('INTERNAL_ADMIN')")
    @Operation(summary = "Get roles configured within a specific bank/NBFC organization")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getOrganizationRoles(@PathVariable Long id) {
        List<RoleResponse> roles = organizationService.getOrganizationRoles(id);
        return ResponseEntity.ok(ApiResponse.ok(roles));
    }

    @GetMapping("/{id}/branches")
    @PreAuthorize("hasRole('INTERNAL_ADMIN')")
    @Operation(summary = "Get branches of a specific bank/NBFC organization")
    public ResponseEntity<ApiResponse<List<BranchResponse>>> getOrganizationBranches(@PathVariable Long id) {
        List<BranchResponse> branches = organizationService.getOrganizationBranches(id);
        return ResponseEntity.ok(ApiResponse.ok(branches));
    }
}
