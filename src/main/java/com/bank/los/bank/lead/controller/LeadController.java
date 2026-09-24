package com.bank.los.bank.lead.controller;

import com.bank.los.bank.lead.dto.CreateLeadRequest;
import com.bank.los.bank.lead.dto.LeadResponse;
import com.bank.los.bank.lead.service.LeadService;
import com.bank.los.common.response.ApiResponse;
import com.bank.los.common.response.PageResponse;
import com.bank.los.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/leads")
@RequiredArgsConstructor
@Tag(name = "Loan Leads", description = "Endpoints for loan lead capture and routing")
@SecurityRequirement(name = "BearerAuth")
public class LeadController {

    private final LeadService leadService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MAKER', 'CHECKER', 'VIEWER')")
    @Operation(summary = "Get paginated list of leads for the bank organization")
    public ResponseEntity<ApiResponse<PageResponse<LeadResponse>>> getLeads(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20) Pageable pageable) {
        PageResponse<LeadResponse> leads = leadService.getLeads(principal, pageable);
        return ResponseEntity.ok(ApiResponse.ok(leads));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MAKER', 'CHECKER', 'VIEWER')")
    @Operation(summary = "Get lead details by ID")
    public ResponseEntity<ApiResponse<LeadResponse>> getLeadById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        LeadResponse lead = leadService.getLeadById(principal, id);
        return ResponseEntity.ok(ApiResponse.ok(lead));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MAKER')")
    @Operation(summary = "Capture / create a new loan lead")
    public ResponseEntity<ApiResponse<LeadResponse>> createLead(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateLeadRequest request) {
        LeadResponse lead = leadService.createLead(principal, request);
        return ResponseEntity.ok(ApiResponse.ok("Lead created successfully", lead));
    }
}
