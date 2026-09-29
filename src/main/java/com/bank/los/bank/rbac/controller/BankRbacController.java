package com.bank.los.bank.rbac.controller;

import com.bank.los.bank.master.entity.Permission;
import com.bank.los.bank.rbac.dto.*;
import com.bank.los.bank.rbac.service.BankRbacService;
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
@RequestMapping("/api/v1/bank/rbac")
@RequiredArgsConstructor
@Tag(name = "Bank RBAC & Permissions", description = "Endpoints for Bank/NBFC Role, Designation Mapping, and Custom Permission Overrides (ALLOW/DENY)")
@SecurityRequirement(name = "BearerAuth")
public class BankRbacController {

    private final BankRbacService bankRbacService;

    @GetMapping("/permissions")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') or hasAuthority('ROLE_PERMISSION_MANAGE')")
    @Operation(summary = "List all permissions available in the current bank database")
    public ResponseEntity<ApiResponse<List<Permission>>> getAllPermissions(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<Permission> perms = bankRbacService.getAllPermissions(principal);
        return ResponseEntity.ok(ApiResponse.ok(perms));
    }

    @GetMapping("/roles")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') or hasAuthority('ROLE_PERMISSION_MANAGE')")
    @Operation(summary = "List all roles with base permissions, overrides, and effective permissions")
    public ResponseEntity<ApiResponse<List<BankRolePermissionResponse>>> getAllRoles(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<BankRolePermissionResponse> roles = bankRbacService.getAllRolesWithPermissions(principal.getOrganizationDbName());
        return ResponseEntity.ok(ApiResponse.ok(roles));
    }

    @PutMapping("/roles/{roleName}/permissions")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') or hasAuthority('ROLE_PERMISSION_MANAGE')")
    @Operation(summary = "Assign or update permissions for a specific role (accepts access permission codes like 201, 202 or names)")
    public ResponseEntity<ApiResponse<BankRolePermissionResponse>> updateRolePermissions(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String roleName,
            @Valid @RequestBody UpdateRolePermissionsRequest request) {
        BankRolePermissionResponse response = bankRbacService.updateRolePermissions(roleName, request.getPermissions(), principal);
        return ResponseEntity.ok(ApiResponse.ok("Role permissions updated successfully", response));
    }

    @GetMapping("/designations")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') or hasAuthority('ROLE_PERMISSION_MANAGE')")
    @Operation(summary = "List all designations with mapped roles and inherited/effective permissions")
    public ResponseEntity<ApiResponse<List<DesignationRoleMappingResponse>>> getAllDesignations(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<DesignationRoleMappingResponse> designations = bankRbacService.getAllDesignationMappings(principal.getOrganizationDbName());
        return ResponseEntity.ok(ApiResponse.ok(designations));
    }

    @PostMapping("/designations")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') or hasAuthority('ROLE_PERMISSION_MANAGE')")
    @Operation(summary = "Map a bank designation to a role (e.g. General Manager -> ADMIN to inherit ADMIN permissions)")
    public ResponseEntity<ApiResponse<DesignationRoleMappingResponse>> mapDesignation(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody DesignationRoleMappingRequest request) {
        DesignationRoleMappingResponse mapped = bankRbacService.createOrUpdateDesignationMapping(request, principal);
        return ResponseEntity.ok(ApiResponse.ok("Designation mapped to role successfully", mapped));
    }

    @DeleteMapping("/designations/{designation}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') or hasAuthority('ROLE_PERMISSION_MANAGE')")
    @Operation(summary = "Delete designation to role mapping")
    public ResponseEntity<ApiResponse<Void>> deleteDesignationMapping(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String designation) {
        bankRbacService.deleteDesignationMapping(designation, principal);
        return ResponseEntity.ok(ApiResponse.ok("Designation mapping deleted successfully", null));
    }

    @GetMapping("/overrides")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') or hasAuthority('ROLE_PERMISSION_MANAGE')")
    @Operation(summary = "List all permission customization overrides (ALLOW / DENY)")
    public ResponseEntity<ApiResponse<List<PermissionOverrideResponse>>> getAllOverrides(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<PermissionOverrideResponse> overrides = bankRbacService.getAllPermissionOverrides(principal.getOrganizationDbName());
        return ResponseEntity.ok(ApiResponse.ok(overrides));
    }

    @PostMapping("/overrides")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') or hasAuthority('ROLE_PERMISSION_MANAGE')")
    @Operation(summary = "Set explicit permission override (ALLOW or DENY) on a Role or Designation")
    public ResponseEntity<ApiResponse<PermissionOverrideResponse>> createOrUpdateOverride(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody PermissionOverrideRequest request) {
        PermissionOverrideResponse response = bankRbacService.createOrUpdatePermissionOverride(request, principal);
        return ResponseEntity.ok(ApiResponse.ok("Permission override saved successfully", response));
    }

    @DeleteMapping("/overrides/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') or hasAuthority('ROLE_PERMISSION_MANAGE')")
    @Operation(summary = "Delete a permission override")
    public ResponseEntity<ApiResponse<Void>> deleteOverride(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        bankRbacService.deletePermissionOverride(id, principal);
        return ResponseEntity.ok(ApiResponse.ok("Permission override deleted successfully", null));
    }

    @GetMapping("/effective-permissions")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MAKER', 'CHECKER', 'VIEWER', 'CUSTOMER')")
    @Operation(summary = "Get effective permissions for current user or for a specific Role/Designation")
    public ResponseEntity<ApiResponse<EffectivePermissionResponse>> getEffectivePermissions(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) String targetName) {

        String type = (targetType != null && !targetType.isBlank()) ? targetType : "ROLE";
        String name = (targetName != null && !targetName.isBlank()) ? targetName : principal.getRole();

        EffectivePermissionResponse response = bankRbacService.getEffectivePermissionsForTarget(type, name, principal);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
