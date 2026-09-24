package com.bank.los.bank.user.controller;

import com.bank.los.bank.master.entity.OrganizationRole;
import com.bank.los.bank.master.repository.OrganizationRoleRepository;
import com.bank.los.bank.user.dto.RoleResponse;
import com.bank.los.common.response.ApiResponse;
import com.bank.los.config.OrganizationContext;
import com.bank.los.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@Tag(name = "Roles", description = "Endpoints for retrieving organization-specific roles")
@SecurityRequirement(name = "BearerAuth")
public class RoleController {

    private final OrganizationRoleRepository organizationRoleRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('INTERNAL_ADMIN', 'SUPER_ADMIN', 'ADMIN', 'MAKER', 'CHECKER', 'VIEWER')")
    @Operation(summary = "List all roles available in the current bank organization")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getRoles(@AuthenticationPrincipal UserPrincipal principal) {
        OrganizationContext.setCurrentOrganization(principal.getOrganizationDbName());
        List<RoleResponse> roles = organizationRoleRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(roles));
    }

    private RoleResponse mapToResponse(OrganizationRole role) {
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .panel(role.getPanel())
                .description(role.getDescription())
                .build();
    }
}
