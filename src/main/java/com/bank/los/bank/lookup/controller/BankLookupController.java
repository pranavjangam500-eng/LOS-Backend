package com.bank.los.bank.lookup.controller;

import com.bank.los.administration.lookup.dto.LookupSubTypeResponse;
import com.bank.los.administration.lookup.dto.LookupTypeResponse;
import com.bank.los.bank.lookup.dto.BankCreateLookupSubTypeRequest;
import com.bank.los.bank.lookup.dto.BankUpdateLookupSubTypeRequest;
import com.bank.los.bank.lookup.service.BankLookupService;
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
@RequestMapping("/api/v1/bank/lookups")
@RequiredArgsConstructor
@Tag(name = "Bank Lookups", description = "Endpoints for bank-specific lookup queries, options retrieval, and custom extensions")
@SecurityRequirement(name = "BearerAuth")
public class BankLookupController {

    private final BankLookupService bankLookupService;

    @GetMapping("/types")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MAKER', 'CHECKER', 'VIEWER', 'CUSTOMER') or hasAuthority('LOOKUP_BANK_VIEW')")
    @Operation(summary = "List all active lookup types available in current bank")
    public ResponseEntity<ApiResponse<List<LookupTypeResponse>>> getAllLookupTypes(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<LookupTypeResponse> types = bankLookupService.getAllLookupTypes(principal);
        return ResponseEntity.ok(ApiResponse.ok(types));
    }

    @GetMapping("/types/{code}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MAKER', 'CHECKER', 'VIEWER', 'CUSTOMER') or hasAuthority('LOOKUP_BANK_VIEW')")
    @Operation(summary = "Get lookup type details and options by lookup code")
    public ResponseEntity<ApiResponse<LookupTypeResponse>> getLookupTypeByCode(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String code) {
        LookupTypeResponse response = bankLookupService.getLookupTypeByCode(principal, code);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{code}/options")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MAKER', 'CHECKER', 'VIEWER', 'CUSTOMER') or hasAuthority('LOOKUP_BANK_VIEW')")
    @Operation(summary = "Get list of active options for a lookup code (e.g. 10001 Status, 10002 Designation, 10004 Loan Type, 10005 Customer Type)")
    public ResponseEntity<ApiResponse<List<LookupSubTypeResponse>>> getOptionsByLookupCode(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String code) {
        List<LookupSubTypeResponse> options = bankLookupService.getOptionsByLookupCode(principal, code);
        return ResponseEntity.ok(ApiResponse.ok(options));
    }

    @PostMapping("/types/{typeCode}/sub-types")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MAKER') or hasAuthority('LOOKUP_BANK_ADD')")
    @Operation(summary = "Add a new custom option/sub-type into the bank database (allowed for non-fixed lookups like 10002 Designation, 10004 Loan Type)")
    public ResponseEntity<ApiResponse<LookupSubTypeResponse>> addBankSubType(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String typeCode,
            @Valid @RequestBody BankCreateLookupSubTypeRequest request) {
        LookupSubTypeResponse created = bankLookupService.addBankSubType(principal, typeCode, request);
        return ResponseEntity.ok(ApiResponse.ok("Lookup option added to bank database successfully", created));
    }

    @PostMapping("/types/{typeCode}/import-master/{subTypeCode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MAKER') or hasAuthority('LOOKUP_BANK_ADD_FROM_MASTER')")
    @Operation(summary = "Import/Add an existing option from Master DB into the bank database")
    public ResponseEntity<ApiResponse<LookupSubTypeResponse>> importOptionFromMaster(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String typeCode,
            @PathVariable String subTypeCode) {
        LookupSubTypeResponse created = bankLookupService.importOptionFromMaster(principal, typeCode, subTypeCode);
        return ResponseEntity.ok(ApiResponse.ok("Master lookup option imported into bank database successfully", created));
    }

    @PutMapping("/sub-types/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MAKER') or hasAuthority('LOOKUP_BANK_EDIT')")
    @Operation(summary = "Update a bank-custom lookup option")
    public ResponseEntity<ApiResponse<LookupSubTypeResponse>> updateBankSubType(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody BankUpdateLookupSubTypeRequest request) {
        LookupSubTypeResponse updated = bankLookupService.updateBankSubType(principal, id, request);
        return ResponseEntity.ok(ApiResponse.ok("Lookup option updated in bank database successfully", updated));
    }

    @DeleteMapping("/sub-types/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') or hasAuthority('LOOKUP_BANK_DELETE')")
    @Operation(summary = "Delete a bank-custom lookup option (e.g. remove bank-specific designation or loan type)")
    public ResponseEntity<ApiResponse<Void>> deleteBankSubType(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        bankLookupService.deleteBankSubType(principal, id);
        return ResponseEntity.ok(ApiResponse.ok("Lookup option deleted from bank database successfully", null));
    }

    @PatchMapping("/sub-types/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'CHECKER') or hasAuthority('LOOKUP_BANK_ACTIVATE')")
    @Operation(summary = "Activate a bank lookup option")
    public ResponseEntity<ApiResponse<LookupSubTypeResponse>> activateBankSubType(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        LookupSubTypeResponse updated = bankLookupService.activateBankSubType(principal, id);
        return ResponseEntity.ok(ApiResponse.ok("Lookup option activated successfully", updated));
    }

    @PatchMapping("/sub-types/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'CHECKER') or hasAuthority('LOOKUP_BANK_DEACTIVATE')")
    @Operation(summary = "Deactivate a bank lookup option")
    public ResponseEntity<ApiResponse<LookupSubTypeResponse>> deactivateBankSubType(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        LookupSubTypeResponse updated = bankLookupService.deactivateBankSubType(principal, id);
        return ResponseEntity.ok(ApiResponse.ok("Lookup option deactivated successfully", updated));
    }
}
