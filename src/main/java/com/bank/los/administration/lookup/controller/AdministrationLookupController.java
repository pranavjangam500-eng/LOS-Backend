package com.bank.los.administration.lookup.controller;

import com.bank.los.administration.lookup.dto.*;
import com.bank.los.administration.lookup.service.AdministrationLookupService;
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
@RequestMapping({"/api/v1/administration/lookups", "/api/v1/admin/lookups"})
@RequiredArgsConstructor
@Tag(name = "Administration Lookups", description = "Central Platform Lookup Management (System Admin Master Database & Bank Access)")
@SecurityRequirement(name = "BearerAuth")
public class AdministrationLookupController {

    private final AdministrationLookupService administrationLookupService;

    @GetMapping("/types")
    @PreAuthorize("hasAnyRole('INTERNAL_ADMIN', 'SUPER_ADMIN', 'ADMIN', 'MAKER', 'CHECKER', 'VIEWER')")
    @Operation(summary = "List all central master lookup types (supports optional search, status, and pagination)")
    public ResponseEntity<?> getLookupTypes(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (search == null && status == null && page == null && size == null) {
            List<LookupTypeResponse> types = administrationLookupService.getAllLookupTypes(principal);
            return ResponseEntity.ok(ApiResponse.ok(types));
        }

        int pageNum = (page != null && page >= 0) ? page : 0;
        int pageSize = (size != null && size > 0) ? Math.min(size, 100) : 10;
        PagedLookupResponse<LookupTypeResponse> paged = administrationLookupService.searchLookupTypes(search, status, pageNum, pageSize, principal);
        return ResponseEntity.ok(ApiResponse.ok(paged));
    }

    @GetMapping("/types/{code}")
    @PreAuthorize("hasAnyRole('INTERNAL_ADMIN', 'SUPER_ADMIN', 'ADMIN', 'MAKER', 'CHECKER', 'VIEWER')")
    @Operation(summary = "Get central master lookup type with its options by code (detail view)")
    public ResponseEntity<ApiResponse<LookupTypeResponse>> getLookupTypeByCode(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String code) {
        LookupTypeResponse response = administrationLookupService.getLookupTypeByCode(code, principal);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/types/{code}/sub-types")
    @PreAuthorize("hasAnyRole('INTERNAL_ADMIN', 'SUPER_ADMIN', 'ADMIN', 'MAKER', 'CHECKER', 'VIEWER')")
    @Operation(summary = "List or search options/sub-types under a master lookup type")
    public ResponseEntity<?> getLookupSubTypes(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String code,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (search == null && status == null && page == null && size == null) {
            List<LookupSubTypeResponse> subTypes = administrationLookupService.getSubTypesByLookupTypeCode(code, principal);
            return ResponseEntity.ok(ApiResponse.ok(subTypes));
        }

        int pageNum = (page != null && page >= 0) ? page : 0;
        int pageSize = (size != null && size > 0) ? Math.min(size, 100) : 10;
        PagedLookupResponse<LookupSubTypeResponse> paged = administrationLookupService.searchLookupSubTypes(code, search, status, pageNum, pageSize, principal);
        return ResponseEntity.ok(ApiResponse.ok(paged));
    }

    @GetMapping("/{code}/options")
    @PreAuthorize("hasAnyRole('INTERNAL_ADMIN', 'SUPER_ADMIN', 'ADMIN', 'MAKER', 'CHECKER', 'VIEWER')")
    @Operation(summary = "Get active dropdown options for a master lookup code (returns [{ code, description }])")
    public ResponseEntity<ApiResponse<List<LookupOptionResponse>>> getMasterLookupOptions(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String code) {
        List<LookupOptionResponse> options = administrationLookupService.getActiveOptionsByLookupCode(code, principal);
        return ResponseEntity.ok(ApiResponse.ok(options));
    }

    @PostMapping("/types")
    @PreAuthorize("hasAnyRole('INTERNAL_ADMIN', 'SUPER_ADMIN', 'ADMIN', 'MAKER')")
    @Operation(summary = "Create a new central master lookup type (server generates unique code)")
    public ResponseEntity<ApiResponse<LookupTypeResponse>> createLookupType(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateLookupTypeRequest request) {
        LookupTypeResponse created = administrationLookupService.createLookupType(request, principal);
        return ResponseEntity.ok(ApiResponse.ok("Lookup type created successfully in Master DB", created));
    }

    @PutMapping("/types/{code}")
    @PreAuthorize("hasAnyRole('INTERNAL_ADMIN', 'SUPER_ADMIN', 'ADMIN', 'MAKER')")
    @Operation(summary = "Update central master lookup type (name and status only; code immutable)")
    public ResponseEntity<ApiResponse<LookupTypeResponse>> updateLookupType(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String code,
            @Valid @RequestBody UpdateLookupTypeRequest request) {
        LookupTypeResponse updated = administrationLookupService.updateLookupType(code, request, principal);
        return ResponseEntity.ok(ApiResponse.ok("Lookup type updated successfully in Master DB", updated));
    }

    @PatchMapping("/types/{code}/activate")
    @PreAuthorize("hasAnyRole('INTERNAL_ADMIN', 'SUPER_ADMIN', 'ADMIN', 'MAKER')")
    @Operation(summary = "Activate a central master lookup type")
    public ResponseEntity<ApiResponse<LookupTypeResponse>> activateLookupType(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String code) {
        LookupTypeResponse updated = administrationLookupService.activateLookupType(code, principal);
        return ResponseEntity.ok(ApiResponse.ok("Lookup type activated successfully", updated));
    }

    @PatchMapping("/types/{code}/deactivate")
    @PreAuthorize("hasAnyRole('INTERNAL_ADMIN', 'SUPER_ADMIN', 'ADMIN', 'MAKER')")
    @Operation(summary = "Deactivate a central master lookup type (system-fixed types cannot be deactivated)")
    public ResponseEntity<ApiResponse<LookupTypeResponse>> deactivateLookupType(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String code) {
        LookupTypeResponse updated = administrationLookupService.deactivateLookupType(code, principal);
        return ResponseEntity.ok(ApiResponse.ok("Lookup type deactivated successfully", updated));
    }

    @DeleteMapping("/types/{code}")
    @PreAuthorize("hasAnyRole('INTERNAL_ADMIN', 'SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Delete central master lookup type (if not system-fixed)")
    public ResponseEntity<ApiResponse<Void>> deleteLookupType(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String code) {
        administrationLookupService.deleteLookupType(code, principal);
        return ResponseEntity.ok(ApiResponse.ok("Lookup type deleted successfully from Master DB", null));
    }

    @PostMapping("/types/{typeCode}/sub-types")
    @PreAuthorize("hasAnyRole('INTERNAL_ADMIN', 'SUPER_ADMIN', 'ADMIN', 'MAKER')")
    @Operation(summary = "Add an option / sub-type to a master lookup type (server generates incremental code)")
    public ResponseEntity<ApiResponse<LookupSubTypeResponse>> addSubType(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String typeCode,
            @Valid @RequestBody CreateLookupSubTypeRequest request) {
        LookupSubTypeResponse created = administrationLookupService.addSubType(typeCode, request, principal);
        return ResponseEntity.ok(ApiResponse.ok("Lookup sub-type created successfully in Master DB", created));
    }

    @PutMapping("/sub-types/{id}")
    @PreAuthorize("hasAnyRole('INTERNAL_ADMIN', 'SUPER_ADMIN', 'ADMIN', 'MAKER')")
    @Operation(summary = "Update an option / sub-type in master DB (name and status only; code immutable)")
    public ResponseEntity<ApiResponse<LookupSubTypeResponse>> updateSubType(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody UpdateLookupSubTypeRequest request) {
        LookupSubTypeResponse updated = administrationLookupService.updateSubType(id, request, principal);
        return ResponseEntity.ok(ApiResponse.ok("Lookup sub-type updated successfully in Master DB", updated));
    }

    @PatchMapping("/sub-types/{id}/activate")
    @PreAuthorize("hasAnyRole('INTERNAL_ADMIN', 'SUPER_ADMIN', 'ADMIN', 'MAKER')")
    @Operation(summary = "Activate a master lookup sub-type / option")
    public ResponseEntity<ApiResponse<LookupSubTypeResponse>> activateSubType(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        LookupSubTypeResponse updated = administrationLookupService.activateSubType(id, principal);
        return ResponseEntity.ok(ApiResponse.ok("Lookup sub-type activated successfully", updated));
    }

    @PatchMapping("/sub-types/{id}/deactivate")
    @PreAuthorize("hasAnyRole('INTERNAL_ADMIN', 'SUPER_ADMIN', 'ADMIN', 'MAKER')")
    @Operation(summary = "Deactivate a master lookup sub-type / option (system-fixed options cannot be deactivated)")
    public ResponseEntity<ApiResponse<LookupSubTypeResponse>> deactivateSubType(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        LookupSubTypeResponse updated = administrationLookupService.deactivateSubType(id, principal);
        return ResponseEntity.ok(ApiResponse.ok("Lookup sub-type deactivated successfully", updated));
    }

    @DeleteMapping("/sub-types/{id}")
    @PreAuthorize("hasAnyRole('INTERNAL_ADMIN', 'SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Delete an option / sub-type from master DB (system-fixed options cannot be deleted)")
    public ResponseEntity<ApiResponse<Void>> deleteSubType(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        administrationLookupService.deleteSubType(id, principal);
        return ResponseEntity.ok(ApiResponse.ok("Lookup sub-type deleted successfully from Master DB", null));
    }

    @GetMapping("/banks/{bankCode}")
    @PreAuthorize("hasRole('INTERNAL_ADMIN')")
    @Operation(summary = "Inspect lookups and bank-customized options for a specific Bank/NBFC")
    public ResponseEntity<ApiResponse<List<LookupTypeResponse>>> getBankLookups(@PathVariable String bankCode) {
        List<LookupTypeResponse> bankLookups = administrationLookupService.getBankLookups(bankCode);
        return ResponseEntity.ok(ApiResponse.ok(bankLookups));
    }

    @PutMapping("/banks/{bankCode}/types/{code}/permissions")
    @PreAuthorize("hasRole('INTERNAL_ADMIN')")
    @Operation(summary = "Configure allowed permissions for a specific lookup type in a bank")
    public ResponseEntity<ApiResponse<LookupTypeResponse>> updateBankLookupPermissions(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String bankCode,
            @PathVariable String code,
            @Valid @RequestBody UpdateBankLookupPermissionsRequest request) {
        LookupTypeResponse updated = administrationLookupService.updateBankLookupPermissions(bankCode, code, request, principal);
        return ResponseEntity.ok(ApiResponse.ok("Lookup permissions for bank " + bankCode + " updated successfully", updated));
    }

    @PostMapping("/sync/{bankCode}")
    @PreAuthorize("hasRole('INTERNAL_ADMIN')")
    @Operation(summary = "Push/Sync master lookups into a specific Bank/NBFC database")
    public ResponseEntity<ApiResponse<Void>> syncMasterLookupsToBank(@PathVariable String bankCode) {
        administrationLookupService.syncMasterLookupsToBank(bankCode);
        return ResponseEntity.ok(ApiResponse.ok("Master lookups synced to bank " + bankCode + " successfully", null));
    }

    @PostMapping("/sync-all")
    @PreAuthorize("hasRole('INTERNAL_ADMIN')")
    @Operation(summary = "Push/Sync master lookups into ALL active Bank/NBFC databases")
    public ResponseEntity<ApiResponse<Void>> syncMasterLookupsToAllBanks() {
        administrationLookupService.syncMasterLookupsToAllBanks();
        return ResponseEntity.ok(ApiResponse.ok("Master lookups synced to all banks successfully", null));
    }
}
