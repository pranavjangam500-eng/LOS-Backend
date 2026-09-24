package com.bank.los.bank.customer.controller;

import com.bank.los.bank.customer.dto.request.CreateCustomerRequest;
import com.bank.los.bank.customer.dto.request.UpdateCustomerRequest;
import com.bank.los.bank.customer.dto.response.CustomerResponse;
import com.bank.los.bank.customer.service.CustomerService;
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
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Bank Customers", description = "Endpoints for bank staff to register and manage customers")
@SecurityRequirement(name = "BearerAuth")
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MAKER', 'CHECKER', 'VIEWER')")
    @Operation(summary = "Get paginated list of customers for current organization")
    public ResponseEntity<ApiResponse<PageResponse<CustomerResponse>>> getCustomers(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20) Pageable pageable) {
        PageResponse<CustomerResponse> response = customerService.getCustomers(principal, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MAKER', 'CHECKER', 'VIEWER')")
    @Operation(summary = "Get customer details by ID")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        CustomerResponse response = customerService.getCustomerById(principal, id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MAKER')")
    @Operation(summary = "Register a new customer within this bank organization")
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateCustomerRequest request) {
        CustomerResponse response = customerService.createCustomer(principal, request);
        return ResponseEntity.ok(ApiResponse.ok("Customer registered successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MAKER')")
    @Operation(summary = "Update customer details")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomer(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody UpdateCustomerRequest request) {
        CustomerResponse response = customerService.updateCustomer(principal, id, request);
        return ResponseEntity.ok(ApiResponse.ok("Customer updated successfully", response));
    }
}
