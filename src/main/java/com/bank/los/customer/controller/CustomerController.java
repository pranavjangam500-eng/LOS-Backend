package com.bank.los.customer.controller;

import com.bank.los.common.response.ApiResponse;
import com.bank.los.customer.dto.request.CreateCustomerRequest;
import com.bank.los.customer.dto.request.UpdateCustomerRequest;
import com.bank.los.customer.dto.response.CustomerResponse;
import com.bank.los.customer.service.CustomerService;
import com.bank.los.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Customer Management", description = "Customer / Borrower profile management endpoints")
@SecurityRequirement(name = "BearerAuth")
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MAKER', 'CHECKER', 'VIEWER')")
    @Operation(summary = "List all customers in the current bank/NBFC")
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> getAllCustomers(@AuthenticationPrincipal UserPrincipal principal) {
        List<CustomerResponse> customers = customerService.getAllCustomers(principal);
        return ResponseEntity.ok(ApiResponse.ok(customers));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MAKER', 'CHECKER', 'VIEWER', 'CUSTOMER')")
    @Operation(summary = "Get customer profile by ID")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        CustomerResponse customer = customerService.getCustomerById(principal, id);
        return ResponseEntity.ok(ApiResponse.ok(customer));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MAKER')")
    @Operation(summary = "Create a new customer profile (Maker / Super Admin)")
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateCustomerRequest request) {
        CustomerResponse customer = customerService.createCustomer(principal, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Customer created successfully", customer));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MAKER')")
    @Operation(summary = "Update customer profile details")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomer(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody UpdateCustomerRequest request) {
        CustomerResponse customer = customerService.updateCustomer(principal, id, request);
        return ResponseEntity.ok(ApiResponse.ok("Customer updated successfully", customer));
    }
}
