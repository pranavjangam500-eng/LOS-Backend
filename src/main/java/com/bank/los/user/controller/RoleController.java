package com.bank.los.user.controller;

import com.bank.los.common.response.ApiResponse;
import com.bank.los.organization.service.OrganizationService;
import com.bank.los.security.UserPrincipal;
import com.bank.los.user.dto.RoleResponse;
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
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@Tag(name = "Roles", description = "Query available roles for bank staff assignment")
@SecurityRequirement(name = "BearerAuth")
public class RoleController {

    private final OrganizationService organizationService;

    @GetMapping
    @PreAuthorize("hasAnyRole('INTERNAL_ADMIN', 'ADMIN')")
    @Operation(summary = "Get available roles for a bank/organization (e.g. ADMIN, MAKER, CHECKER, VIEWER)")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getRoles(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "Bank/Organization ID (required for Super Admin, defaults to current bank for Bank Admin)")
            @RequestParam(required = false) Long organizationId) {

        Long targetOrgId = organizationId;
        if (targetOrgId == null) {
            // Default to 1 (HDFC) or first org if not specified
            targetOrgId = 1L;
        }

        List<RoleResponse> roles = organizationService.getOrganizationRoles(targetOrgId);
        return ResponseEntity.ok(ApiResponse.ok(roles));
    }
}
