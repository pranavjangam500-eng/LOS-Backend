package com.bank.los.administration.administrator.controller;

import com.bank.los.administration.administrator.dto.AdministratorResponse;
import com.bank.los.administration.administrator.dto.CreateAdministratorRequest;
import com.bank.los.administration.administrator.service.AdministratorService;
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
@RequestMapping("/api/v1/administration/administrators")
@RequiredArgsConstructor
@Tag(name = "Administrator Management", description = "Endpoints for managing Platform/Allianza Administrators")
@SecurityRequirement(name = "BearerAuth")
public class AdministratorController {

    private final AdministratorService administratorService;

    @GetMapping
    @PreAuthorize("hasRole('INTERNAL_ADMIN')")
    @Operation(summary = "List all platform administrators")
    public ResponseEntity<ApiResponse<List<AdministratorResponse>>> getAllAdministrators() {
        List<AdministratorResponse> admins = administratorService.getAllAdministrators();
        return ResponseEntity.ok(ApiResponse.ok(admins));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('INTERNAL_ADMIN')")
    @Operation(summary = "Get administrator profile by ID")
    public ResponseEntity<ApiResponse<AdministratorResponse>> getAdministratorById(@PathVariable Long id) {
        AdministratorResponse admin = administratorService.getAdministratorById(id);
        return ResponseEntity.ok(ApiResponse.ok(admin));
    }

    @PostMapping
    @PreAuthorize("hasRole('INTERNAL_ADMIN')")
    @Operation(summary = "Create a new platform administrator")
    public ResponseEntity<ApiResponse<AdministratorResponse>> createAdministrator(
            @Valid @RequestBody CreateAdministratorRequest request) {
        AdministratorResponse admin = administratorService.createAdministrator(request);
        return ResponseEntity.ok(ApiResponse.ok("Administrator created successfully", admin));
    }
}
