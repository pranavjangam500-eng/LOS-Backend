package com.bank.los.administration.organization.controller;

import com.bank.los.administration.organization.dto.CreateOrganizationRequest;
import com.bank.los.administration.organization.dto.OrganizationResponse;
import com.bank.los.administration.organization.dto.UpdateOrganizationRequest;
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
@RequestMapping({"/api/v1/administration/organizations", "/api/v1/organizations"})
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

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('INTERNAL_ADMIN')")
    @Operation(summary = "Update an existing Bank/NBFC organization by ID, UUID, or Bank Code")
    public ResponseEntity<ApiResponse<OrganizationResponse>> updateOrganization(
            @PathVariable String id,
            @RequestBody UpdateOrganizationRequest request) {
        OrganizationResponse updated = organizationService.updateOrganization(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Organization updated successfully", updated));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('INTERNAL_ADMIN')")
    @Operation(summary = "Partially update an existing Bank/NBFC organization by ID, UUID, or Bank Code")
    public ResponseEntity<ApiResponse<OrganizationResponse>> patchOrganization(
            @PathVariable String id,
            @RequestBody UpdateOrganizationRequest request) {
        OrganizationResponse updated = organizationService.updateOrganization(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Organization updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('INTERNAL_ADMIN')")
    @Operation(summary = "Delete a Bank/NBFC organization (soft-delete by default, or hard delete with ?hardDelete=true)")
    public ResponseEntity<ApiResponse<Void>> deleteOrganization(
            @PathVariable String id,
            @RequestParam(defaultValue = "false") boolean hardDelete) {
        organizationService.deleteOrganization(id, hardDelete);
        String msg = hardDelete ? "Organization permanently deleted successfully" : "Organization deactivated/deleted successfully";
        return ResponseEntity.ok(ApiResponse.ok(msg, null));
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
