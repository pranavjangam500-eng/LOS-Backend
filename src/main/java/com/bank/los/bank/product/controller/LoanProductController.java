package com.bank.los.bank.product.controller;

import com.bank.los.bank.product.dto.LoanProductRequest;
import com.bank.los.bank.product.dto.LoanProductResponse;
import com.bank.los.bank.product.service.LoanProductService;
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
@RequestMapping("/api/v1/loan-products")
@RequiredArgsConstructor
@Tag(name = "Loan Products", description = "Endpoints for configuring and querying bank loan products")
@SecurityRequirement(name = "BearerAuth")
public class LoanProductController {

    private final LoanProductService loanProductService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MAKER', 'CHECKER', 'VIEWER', 'CUSTOMER')")
    @Operation(summary = "List all configured loan products for current organization")
    public ResponseEntity<ApiResponse<List<LoanProductResponse>>> getAllProducts(@AuthenticationPrincipal UserPrincipal principal) {
        List<LoanProductResponse> products = loanProductService.getAllProducts(principal);
        return ResponseEntity.ok(ApiResponse.ok(products));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MAKER', 'CHECKER', 'VIEWER', 'CUSTOMER')")
    @Operation(summary = "Get loan product by ID")
    public ResponseEntity<ApiResponse<LoanProductResponse>> getProductById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        LoanProductResponse product = loanProductService.getProductById(principal, id);
        return ResponseEntity.ok(ApiResponse.ok(product));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Create / configure a new loan product")
    public ResponseEntity<ApiResponse<LoanProductResponse>> createProduct(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody LoanProductRequest request) {
        LoanProductResponse product = loanProductService.createProduct(principal, request);
        return ResponseEntity.ok(ApiResponse.ok("Loan product created successfully", product));
    }
}
