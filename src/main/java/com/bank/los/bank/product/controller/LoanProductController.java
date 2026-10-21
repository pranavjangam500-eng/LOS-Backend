package com.bank.los.bank.product.controller;

import com.bank.los.bank.product.dto.LoanProductCsvImportResult;
import com.bank.los.bank.product.dto.LoanProductRequest;
import com.bank.los.bank.product.dto.LoanProductResponse;
import com.bank.los.bank.product.service.LoanProductService;
import com.bank.los.common.exception.BusinessException;
import com.bank.los.common.response.ApiResponse;
import com.bank.los.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/api/v1/loan-products")
@RequiredArgsConstructor
@Tag(name = "Loan Products", description = "Endpoints for configuring, querying, updating, and importing/exporting bank loan products")
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

    @GetMapping("/code/{code}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MAKER', 'CHECKER', 'VIEWER', 'CUSTOMER')")
    @Operation(summary = "Get loan product by unique product code")
    public ResponseEntity<ApiResponse<LoanProductResponse>> getProductByCode(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String code) {
        LoanProductResponse product = loanProductService.getProductByCode(principal, code);
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

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Update an existing loan product")
    public ResponseEntity<ApiResponse<LoanProductResponse>> updateProduct(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody LoanProductRequest request) {
        LoanProductResponse product = loanProductService.updateProduct(principal, id, request);
        return ResponseEntity.ok(ApiResponse.ok("Loan product updated successfully", product));
    }

    @PostMapping(value = "/import", consumes = {
            MediaType.MULTIPART_FORM_DATA_VALUE,
            "text/csv",
            MediaType.APPLICATION_OCTET_STREAM_VALUE
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Bulk import loan products from CSV file (multipart or stream)")
    public ResponseEntity<ApiResponse<LoanProductCsvImportResult>> importProducts(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(value = "file", required = false) MultipartFile file,
            HttpServletRequest request) throws IOException {
        InputStream inputStream;
        if (file != null) {
            if (file.isEmpty()) {
                throw new BusinessException("EMPTY_FILE", "Uploaded CSV file is empty");
            }
            inputStream = file.getInputStream();
        } else {
            inputStream = request.getInputStream();
        }
        LoanProductCsvImportResult result = loanProductService.importProductsFromCsv(principal, inputStream);
        return ResponseEntity.ok(ApiResponse.ok("Bulk CSV import completed", result));
    }

    @GetMapping(value = "/export", produces = "text/csv")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MAKER', 'CHECKER', 'VIEWER')")
    @Operation(summary = "Export all loan products to CSV attachment")
    public ResponseEntity<byte[]> exportProducts(@AuthenticationPrincipal UserPrincipal principal) {
        byte[] csvData = loanProductService.exportProductsToCsv(principal);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"loan_products.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvData);
    }

    @GetMapping(value = "/sample-csv", produces = "text/csv")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MAKER', 'CHECKER', 'VIEWER')")
    @Operation(summary = "Download pre-configured sample CSV template for loan products bulk import")
    public ResponseEntity<byte[]> getSampleCsv() {
        byte[] csvData = loanProductService.getSampleCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"sample_loan_products.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvData);
    }
}
