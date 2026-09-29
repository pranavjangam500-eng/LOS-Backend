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
@Tag(name = "Administration Lookups", description = "Central Platform Lookup Management (System Admin Master Database)")
@SecurityRequirement(name = "BearerAuth")
public class AdministrationLookupController {

    private final AdministrationLookupService administrationLookupService;

    @GetMapping("/types")
    @PreAuthorize("hasRole('INTERNAL_ADMIN')")
    @Operation(summary = "List all central master lookup types")
    public ResponseEntity<ApiResponse<List<LookupTypeResponse>>> getAllLookupTypes() {
        List<LookupTypeResponse> types = administrationLookupService.getAllLookupTypes();
        return ResponseEntity.ok(ApiResponse.ok(types));
    }

    @GetMapping("/types/{code}")
    @PreAuthorize("hasRole('INTERNAL_ADMIN')")
    @Operation(summary = "Get central master lookup type with its options by code")
    public ResponseEntity<ApiResponse<LookupTypeResponse>> getLookupTypeByCode(@PathVariable String code) {
        LookupTypeResponse response = administrationLookupService.getLookupTypeByCode(code);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/types")
    @PreAuthorize("hasRole('INTERNAL_ADMIN')")
    @Operation(summary = "Create a new central master lookup type")
    public ResponseEntity<ApiResponse<LookupTypeResponse>> createLookupType(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateLookupTypeRequest request) {
        LookupTypeResponse created = administrationLookupService.createLookupType(request, principal);
        return ResponseEntity.ok(ApiResponse.ok("Lookup type created successfully in Master DB", created));
    }

    @PutMapping("/types/{code}")
    @PreAuthorize("hasRole('INTERNAL_ADMIN')")
    @Operation(summary = "Update central master lookup type")
    public ResponseEntity<ApiResponse<LookupTypeResponse>> updateLookupType(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String code,
            @Valid @RequestBody UpdateLookupTypeRequest request) {
        LookupTypeResponse updated = administrationLookupService.updateLookupType(code, request, principal);
        return ResponseEntity.ok(ApiResponse.ok("Lookup type updated successfully in Master DB", updated));
    }

    @DeleteMapping("/types/{code}")
    @PreAuthorize("hasRole('INTERNAL_ADMIN')")
    @Operation(summary = "Delete central master lookup type (if not system-fixed)")
    public ResponseEntity<ApiResponse<Void>> deleteLookupType(@PathVariable String code) {
        administrationLookupService.deleteLookupType(code);
        return ResponseEntity.ok(ApiResponse.ok("Lookup type deleted successfully from Master DB", null));
    }

    @PostMapping("/types/{typeCode}/sub-types")
    @PreAuthorize("hasRole('INTERNAL_ADMIN')")
    @Operation(summary = "Add an option / sub-type to a master lookup type")
    public ResponseEntity<ApiResponse<LookupSubTypeResponse>> addSubType(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String typeCode,
            @Valid @RequestBody CreateLookupSubTypeRequest request) {
        LookupSubTypeResponse created = administrationLookupService.addSubType(typeCode, request, principal);
        return ResponseEntity.ok(ApiResponse.ok("Lookup sub-type created successfully in Master DB", created));
    }

    @PutMapping("/sub-types/{id}")
    @PreAuthorize("hasRole('INTERNAL_ADMIN')")
    @Operation(summary = "Update an option / sub-type in master DB")
    public ResponseEntity<ApiResponse<LookupSubTypeResponse>> updateSubType(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody UpdateLookupSubTypeRequest request) {
        LookupSubTypeResponse updated = administrationLookupService.updateSubType(id, request, principal);
        return ResponseEntity.ok(ApiResponse.ok("Lookup sub-type updated successfully in Master DB", updated));
    }

    @DeleteMapping("/sub-types/{id}")
    @PreAuthorize("hasRole('INTERNAL_ADMIN')")
    @Operation(summary = "Delete an option / sub-type from master DB")
    public ResponseEntity<ApiResponse<Void>> deleteSubType(@PathVariable Long id) {
        administrationLookupService.deleteSubType(id);
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
    @Operation(summary = "Configure allowed permissions (e.g. VIEW, ADD, EDIT, DELETE, ADD_FROM_MASTER, ACTIVATE, DEACTIVATE) for a specific lookup type in a bank")
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
