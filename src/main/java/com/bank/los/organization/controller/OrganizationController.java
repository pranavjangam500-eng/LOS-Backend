package com.bank.los.organization.controller;

import com.bank.los.common.response.ApiResponse;
import com.bank.los.organization.dto.CreateOrganizationRequest;
import com.bank.los.organization.dto.OrganizationResponse;
import com.bank.los.organization.service.OrganizationService;
import com.bank.los.user.dto.BranchResponse;
import com.bank.los.user.dto.RoleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
@Tag(name = "Organizations", description = "Central registry management for Banks and NBFCs (Internal Platform Admin only)")
@SecurityRequirement(name = "BearerAuth")
public class OrganizationController {

    private final OrganizationService organizationService;

    @GetMapping
    @PreAuthorize("hasRole('INTERNAL_ADMIN')")
    @Operation(summary = "List all registered Banks and NBFCs")
    public ResponseEntity<ApiResponse<List<OrganizationResponse>>> getAllOrganizations() {
        List<OrganizationResponse> response = organizationService.getAllOrganizations();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('INTERNAL_ADMIN')")
    @Operation(summary = "Get organization details by ID")
    public ResponseEntity<ApiResponse<OrganizationResponse>> getOrganizationById(@PathVariable Long id) {
        OrganizationResponse response = organizationService.getOrganizationById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping
    @PreAuthorize("hasRole('INTERNAL_ADMIN')")
    @Operation(summary = "Register a new Bank/NBFC tenant organization (without admin credentials; auto-provisions schema and roles)")
    public ResponseEntity<ApiResponse<OrganizationResponse>> createOrganization(@Valid @RequestBody CreateOrganizationRequest request) {
        OrganizationResponse response = organizationService.createOrganization(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Organization registered successfully", response));
    }

    @GetMapping("/{id}/roles")
    @PreAuthorize("hasAnyRole('INTERNAL_ADMIN', 'ADMIN')")
    @Operation(summary = "Get all available staff roles in a bank (e.g. ADMIN, MAKER, CHECKER, VIEWER)")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getOrganizationRoles(@PathVariable Long id) {
        List<RoleResponse> response = organizationService.getOrganizationRoles(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}/branches")
    @PreAuthorize("hasAnyRole('INTERNAL_ADMIN', 'ADMIN')")
    @Operation(summary = "Get all branches configured for a bank")
    public ResponseEntity<ApiResponse<List<BranchResponse>>> getOrganizationBranches(@PathVariable Long id) {
        List<BranchResponse> response = organizationService.getOrganizationBranches(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
