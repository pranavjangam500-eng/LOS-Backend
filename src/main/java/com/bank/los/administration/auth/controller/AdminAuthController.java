package com.bank.los.administration.auth.controller;

import com.bank.los.bank.auth.dto.request.LoginRequest;
import com.bank.los.bank.auth.dto.response.LoginResponse;
import com.bank.los.bank.auth.service.AuthenticationService;
import com.bank.los.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/admin/auth", "/api/v1/administration/auth"})
@RequiredArgsConstructor
@Tag(name = "Allianza Admin Authentication", description = "Endpoints for Master internal admin (Allianza panel) login")
public class AdminAuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    @Operation(summary = "Authenticate Master internal admin (Allianza panel) via email / username / empNo and password")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authenticationService.internalAdminLogin(request);
        return ResponseEntity.ok(ApiResponse.ok("Internal admin login successful", response));
    }
}
