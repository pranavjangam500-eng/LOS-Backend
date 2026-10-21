package com.bank.los.bank.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanProductCsvImportResult {
    private int totalRows;
    private int successCount;
    private int failureCount;
    @Builder.Default
    private List<LoanProductResponse> importedProducts = new ArrayList<>();
    @Builder.Default
    private List<String> errors = new ArrayList<>();
    private String message;
}
