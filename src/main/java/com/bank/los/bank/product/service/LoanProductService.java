package com.bank.los.bank.product.service;

import com.bank.los.bank.lead.service.CsvHelper;
import com.bank.los.bank.product.dto.LoanProductCsvImportResult;
import com.bank.los.bank.product.dto.LoanProductRequest;
import com.bank.los.bank.product.dto.LoanProductResponse;
import com.bank.los.bank.product.entity.LoanProduct;
import com.bank.los.bank.product.repository.LoanProductRepository;
import com.bank.los.common.exception.BusinessException;
import com.bank.los.common.exception.ResourceNotFoundException;
import com.bank.los.config.OrganizationContext;
import com.bank.los.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanProductService {

    private final LoanProductRepository loanProductRepository;

    public static final String[] PRODUCT_CSV_HEADERS = {
            "Product Code",
            "Product Name",
            "Description",
            "EMI",
            "EMI Type",
            "Min Amount",
            "Max Amount",
            "Interest Rate %",
            "Min Tenure Months",
            "Max Tenure Months",
            "Penalty",
            "Penalty %",
            "Interest Calculation Frequency",
            "Interest Calculation Mode",
            "Processing Fee %",
            "Status"
    };

    public List<LoanProductResponse> getAllProducts(UserPrincipal principal) {
        OrganizationContext.setCurrentOrganization(principal.getOrganizationDbName());
        return loanProductRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public LoanProductResponse getProductById(UserPrincipal principal, Long id) {
        OrganizationContext.setCurrentOrganization(principal.getOrganizationDbName());
        LoanProduct product = loanProductRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LoanProduct", "id", id));
        return mapToResponse(product);
    }

    public LoanProductResponse getProductByCode(UserPrincipal principal, String code) {
        OrganizationContext.setCurrentOrganization(principal.getOrganizationDbName());
        LoanProduct product = loanProductRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("LoanProduct", "code", code));
        return mapToResponse(product);
    }

    @Transactional
    public LoanProductResponse createProduct(UserPrincipal principal, LoanProductRequest request) {
        OrganizationContext.setCurrentOrganization(principal.getOrganizationDbName());

        validateProductRequest(request);

        String codeUpper = request.getCode().trim().toUpperCase();
        if (loanProductRepository.existsByCode(codeUpper)) {
            throw new BusinessException("PRODUCT_EXISTS", "Loan product code " + codeUpper + " already exists");
        }

        Double calculatedEmi = resolveEmi(request.getEmi(), request.getMinAmount(), request.getMaxAmount(),
                request.getInterestRatePercent(), request.getMaxTenureMonths(), request.getInterestCalculationMode());

        Double penalty = request.getPenalty();
        if (penalty == null) {
            penalty = 0.0;
        }
        Double penaltyPercent = request.getPenaltyPercent();
        if (penaltyPercent == null) {
            penaltyPercent = 2.0;
        }
        Double processingFee = request.getProcessingFeePercent();
        if (processingFee == null) {
            processingFee = 1.0;
        }

        LoanProduct product = LoanProduct.builder()
                .code(codeUpper)
                .name(request.getName().trim())
                .description(request.getDescription())
                .emi(calculatedEmi)
                .emiType(request.getEmiType() != null && !request.getEmiType().isBlank() ? request.getEmiType().trim().toUpperCase() : "REDUCING_BALANCE")
                .minAmount(request.getMinAmount())
                .maxAmount(request.getMaxAmount())
                .interestRatePercent(request.getInterestRatePercent())
                .minTenureMonths(request.getMinTenureMonths())
                .maxTenureMonths(request.getMaxTenureMonths())
                .penalty(penalty)
                .penaltyPercent(penaltyPercent)
                .interestCalculationFrequency(request.getInterestCalculationFrequency() != null && !request.getInterestCalculationFrequency().isBlank()
                        ? request.getInterestCalculationFrequency().trim().toUpperCase() : "MONTHLY")
                .interestCalculationMode(request.getInterestCalculationMode() != null && !request.getInterestCalculationMode().isBlank()
                        ? request.getInterestCalculationMode().trim().toUpperCase() : "REDUCING")
                .processingFeePercent(processingFee)
                .status(request.getStatus() != null && !request.getStatus().isBlank() ? request.getStatus().trim().toUpperCase() : "ACTIVE")
                .build();

        LoanProduct saved = loanProductRepository.save(product);
        log.info("Loan product created: code={}, org={}", saved.getCode(), principal.getOrganizationCode());
        return mapToResponse(saved);
    }

    @Transactional
    public LoanProductResponse updateProduct(UserPrincipal principal, Long id, LoanProductRequest request) {
        OrganizationContext.setCurrentOrganization(principal.getOrganizationDbName());

        LoanProduct existing = loanProductRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LoanProduct", "id", id));

        validateProductRequest(request);

        String codeUpper = request.getCode().trim().toUpperCase();
        if (loanProductRepository.existsByCodeAndIdNot(codeUpper, id)) {
            throw new BusinessException("PRODUCT_EXISTS", "Loan product code " + codeUpper + " already exists on another product");
        }

        Double calculatedEmi = resolveEmi(request.getEmi(), request.getMinAmount(), request.getMaxAmount(),
                request.getInterestRatePercent(), request.getMaxTenureMonths(), request.getInterestCalculationMode());

        existing.setCode(codeUpper);
        existing.setName(request.getName().trim());
        existing.setDescription(request.getDescription());
        existing.setEmi(calculatedEmi);
        existing.setEmiType(request.getEmiType() != null && !request.getEmiType().isBlank() ? request.getEmiType().trim().toUpperCase() : existing.getEmiType());
        existing.setMinAmount(request.getMinAmount());
        existing.setMaxAmount(request.getMaxAmount());
        existing.setInterestRatePercent(request.getInterestRatePercent());
        existing.setMinTenureMonths(request.getMinTenureMonths());
        existing.setMaxTenureMonths(request.getMaxTenureMonths());
        if (request.getPenalty() != null) existing.setPenalty(request.getPenalty());
        if (request.getPenaltyPercent() != null) existing.setPenaltyPercent(request.getPenaltyPercent());
        if (request.getInterestCalculationFrequency() != null && !request.getInterestCalculationFrequency().isBlank()) {
            existing.setInterestCalculationFrequency(request.getInterestCalculationFrequency().trim().toUpperCase());
        }
        if (request.getInterestCalculationMode() != null && !request.getInterestCalculationMode().isBlank()) {
            existing.setInterestCalculationMode(request.getInterestCalculationMode().trim().toUpperCase());
        }
        if (request.getProcessingFeePercent() != null) existing.setProcessingFeePercent(request.getProcessingFeePercent());
        if (request.getStatus() != null && !request.getStatus().isBlank()) existing.setStatus(request.getStatus().trim().toUpperCase());

        LoanProduct updated = loanProductRepository.save(existing);
        log.info("Loan product updated: id={}, code={}, org={}", updated.getId(), updated.getCode(), principal.getOrganizationCode());
        return mapToResponse(updated);
    }

    public byte[] exportProductsToCsv(UserPrincipal principal) {
        OrganizationContext.setCurrentOrganization(principal.getOrganizationDbName());
        List<LoanProduct> products = loanProductRepository.findAll();

        StringBuilder sb = new StringBuilder();
        sb.append(CsvHelper.toCsvLine(List.of(PRODUCT_CSV_HEADERS)));

        for (LoanProduct p : products) {
            List<String> row = List.of(
                    p.getCode() != null ? p.getCode() : "",
                    p.getName() != null ? p.getName() : "",
                    p.getDescription() != null ? p.getDescription() : "",
                    p.getEmi() != null ? String.format("%.2f", p.getEmi()) : "",
                    p.getEmiType() != null ? p.getEmiType() : "",
                    p.getMinAmount() != null ? String.valueOf(p.getMinAmount()) : "",
                    p.getMaxAmount() != null ? String.valueOf(p.getMaxAmount()) : "",
                    p.getInterestRatePercent() != null ? String.valueOf(p.getInterestRatePercent()) : "",
                    p.getMinTenureMonths() != null ? String.valueOf(p.getMinTenureMonths()) : "",
                    p.getMaxTenureMonths() != null ? String.valueOf(p.getMaxTenureMonths()) : "",
                    p.getPenalty() != null ? String.valueOf(p.getPenalty()) : "",
                    p.getPenaltyPercent() != null ? String.valueOf(p.getPenaltyPercent()) : "",
                    p.getInterestCalculationFrequency() != null ? p.getInterestCalculationFrequency() : "",
                    p.getInterestCalculationMode() != null ? p.getInterestCalculationMode() : "",
                    p.getProcessingFeePercent() != null ? String.valueOf(p.getProcessingFeePercent()) : "",
                    p.getStatus() != null ? p.getStatus() : ""
            );
            sb.append(CsvHelper.toCsvLine(row));
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] getSampleCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append(CsvHelper.toCsvLine(List.of(PRODUCT_CSV_HEADERS)));

        List<String> sampleRow1 = List.of(
                "HOME_LOAN",
                "Prime Housing Loan",
                "Standard retail home loan with flexible tenure",
                "43391.13",
                "REDUCING_BALANCE",
                "500000.0",
                "10000000.0",
                "8.5",
                "12",
                "360",
                "500.0",
                "2.0",
                "MONTHLY",
                "REDUCING",
                "0.5",
                "ACTIVE"
        );
        sb.append(CsvHelper.toCsvLine(sampleRow1));

        List<String> sampleRow2 = List.of(
                "PERSONAL_LOAN",
                "Express Personal Loan",
                "Unsecured instant personal loan for salaried applicants",
                "26333.84",
                "FLAT_RATE",
                "50000.0",
                "2000000.0",
                "12.0",
                "6",
                "60",
                "750.0",
                "2.5",
                "MONTHLY",
                "FLAT",
                "1.5",
                "ACTIVE"
        );
        sb.append(CsvHelper.toCsvLine(sampleRow2));

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Transactional
    public LoanProductCsvImportResult importProductsFromCsv(UserPrincipal principal, InputStream inputStream) {
        OrganizationContext.setCurrentOrganization(principal.getOrganizationDbName());

        List<List<String>> records;
        try {
            records = CsvHelper.parseCsv(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new BusinessException("CSV_PARSE_ERROR", "Failed to parse CSV file: " + e.getMessage());
        }

        if (records.isEmpty()) {
            throw new BusinessException("EMPTY_CSV", "CSV file contains no rows");
        }

        List<String> header = records.get(0);
        int codeCol = -1, nameCol = -1, descCol = -1, emiCol = -1, emiTypeCol = -1;
        int minAmtCol = -1, maxAmtCol = -1, rateCol = -1, minTenureCol = -1, maxTenureCol = -1;
        int penaltyCol = -1, penaltyPctCol = -1, freqCol = -1, modeCol = -1, feeCol = -1, statusCol = -1;

        for (int i = 0; i < header.size(); i++) {
            String col = header.get(i).toLowerCase().replaceAll("[^a-z0-9]", "");
            if (col.contains("code")) codeCol = i;
            else if (col.contains("name")) nameCol = i;
            else if (col.contains("desc")) descCol = i;
            else if (col.equals("emi")) emiCol = i;
            else if (col.contains("emitype")) emiTypeCol = i;
            else if (col.contains("minamt") || col.contains("minamount")) minAmtCol = i;
            else if (col.contains("maxamt") || col.contains("maxamount")) maxAmtCol = i;
            else if (col.contains("interest") || col.contains("rate")) rateCol = i;
            else if (col.contains("mintenure")) minTenureCol = i;
            else if (col.contains("maxtenure")) maxTenureCol = i;
            else if (col.equals("penalty")) penaltyCol = i;
            else if (col.contains("penaltypct") || col.contains("penaltypercent")) penaltyPctCol = i;
            else if (col.contains("frequency")) freqCol = i;
            else if (col.contains("mode")) modeCol = i;
            else if (col.contains("fee") || col.contains("processing")) feeCol = i;
            else if (col.contains("status")) statusCol = i;
        }

        if (codeCol < 0 || nameCol < 0) {
            throw new BusinessException("INVALID_CSV_HEADER", "CSV must contain at least 'Product Code' and 'Product Name' columns");
        }

        int successCount = 0;
        int failureCount = 0;
        List<LoanProductResponse> importedProducts = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (int r = 1; r < records.size(); r++) {
            List<String> row = records.get(r);
            int rowNum = r + 1;
            try {
                String code = getColValue(row, codeCol);
                String name = getColValue(row, nameCol);
                if (code == null || code.isBlank()) throw new IllegalArgumentException("Product code is missing");
                if (name == null || name.isBlank()) throw new IllegalArgumentException("Product name is missing");

                Double minAmt = parseDouble(getColValue(row, minAmtCol), 10000.0);
                Double maxAmt = parseDouble(getColValue(row, maxAmtCol), 1000000.0);
                Double rate = parseDouble(getColValue(row, rateCol), 12.0);
                Integer minTenure = parseInt(getColValue(row, minTenureCol), 12);
                Integer maxTenure = parseInt(getColValue(row, maxTenureCol), 60);
                Double emi = parseDoubleNullable(getColValue(row, emiCol));
                String emiType = getColValue(row, emiTypeCol);
                Double penalty = parseDouble(getColValue(row, penaltyCol), 0.0);
                Double penaltyPct = parseDouble(getColValue(row, penaltyPctCol), 2.0);
                String freq = getColValue(row, freqCol);
                String mode = getColValue(row, modeCol);
                Double fee = parseDouble(getColValue(row, feeCol), 1.0);
                String status = getColValue(row, statusCol);

                LoanProductRequest req = LoanProductRequest.builder()
                        .code(code)
                        .name(name)
                        .description(getColValue(row, descCol))
                        .emi(emi)
                        .emiType(emiType != null && !emiType.isBlank() ? emiType : "REDUCING_BALANCE")
                        .minAmount(minAmt)
                        .maxAmount(maxAmt)
                        .interestRatePercent(rate)
                        .minTenureMonths(minTenure)
                        .maxTenureMonths(maxTenure)
                        .penalty(penalty)
                        .penaltyPercent(penaltyPct)
                        .interestCalculationFrequency(freq != null && !freq.isBlank() ? freq : "MONTHLY")
                        .interestCalculationMode(mode != null && !mode.isBlank() ? mode : "REDUCING")
                        .processingFeePercent(fee)
                        .status(status != null && !status.isBlank() ? status : "ACTIVE")
                        .build();

                validateProductRequest(req);

                Optional<LoanProduct> existingOpt = loanProductRepository.findByCode(code.toUpperCase());
                LoanProduct product;
                if (existingOpt.isPresent()) {
                    product = existingOpt.get();
                    product.setName(req.getName());
                    product.setDescription(req.getDescription());
                    product.setMinAmount(req.getMinAmount());
                    product.setMaxAmount(req.getMaxAmount());
                    product.setInterestRatePercent(req.getInterestRatePercent());
                    product.setMinTenureMonths(req.getMinTenureMonths());
                    product.setMaxTenureMonths(req.getMaxTenureMonths());
                    product.setEmi(resolveEmi(req.getEmi(), req.getMinAmount(), req.getMaxAmount(),
                            req.getInterestRatePercent(), req.getMaxTenureMonths(), req.getInterestCalculationMode()));
                    product.setEmiType(req.getEmiType());
                    product.setPenalty(req.getPenalty());
                    product.setPenaltyPercent(req.getPenaltyPercent());
                    product.setInterestCalculationFrequency(req.getInterestCalculationFrequency());
                    product.setInterestCalculationMode(req.getInterestCalculationMode());
                    product.setProcessingFeePercent(req.getProcessingFeePercent());
                    product.setStatus(req.getStatus());
                } else {
                    Double calculatedEmi = resolveEmi(req.getEmi(), req.getMinAmount(), req.getMaxAmount(),
                            req.getInterestRatePercent(), req.getMaxTenureMonths(), req.getInterestCalculationMode());
                    product = LoanProduct.builder()
                            .code(code.toUpperCase())
                            .name(req.getName())
                            .description(req.getDescription())
                            .emi(calculatedEmi)
                            .emiType(req.getEmiType())
                            .minAmount(req.getMinAmount())
                            .maxAmount(req.getMaxAmount())
                            .interestRatePercent(req.getInterestRatePercent())
                            .minTenureMonths(req.getMinTenureMonths())
                            .maxTenureMonths(req.getMaxTenureMonths())
                            .penalty(req.getPenalty())
                            .penaltyPercent(req.getPenaltyPercent())
                            .interestCalculationFrequency(req.getInterestCalculationFrequency())
                            .interestCalculationMode(req.getInterestCalculationMode())
                            .processingFeePercent(req.getProcessingFeePercent())
                            .status(req.getStatus())
                            .build();
                }

                LoanProduct saved = loanProductRepository.save(product);
                importedProducts.add(mapToResponse(saved));
                successCount++;
            } catch (BusinessException | IllegalArgumentException ex) {
                failureCount++;
                errors.add("Row " + rowNum + ": " + ex.getMessage());
            } catch (RuntimeException ex) {
                failureCount++;
                errors.add("Row " + rowNum + ": " + ex.getMessage());
            }
        }

        return LoanProductCsvImportResult.builder()
                .totalRows(records.size() - 1)
                .successCount(successCount)
                .failureCount(failureCount)
                .importedProducts(importedProducts)
                .errors(errors)
                .message(String.format("Imported %d loan products successfully (%d failed)", successCount, failureCount))
                .build();
    }

    private void validateProductRequest(LoanProductRequest request) {
        if (request.getMinAmount() != null && request.getMaxAmount() != null) {
            if (request.getMinAmount() > request.getMaxAmount()) {
                throw new BusinessException("INVALID_AMOUNT_RANGE", "Minimum amount (" + request.getMinAmount() + ") cannot be greater than maximum amount (" + request.getMaxAmount() + ")");
            }
        }
        if (request.getMinTenureMonths() != null && request.getMaxTenureMonths() != null) {
            if (request.getMinTenureMonths() > request.getMaxTenureMonths()) {
                throw new BusinessException("INVALID_TENURE_RANGE", "Minimum tenure (" + request.getMinTenureMonths() + " months) cannot be greater than maximum tenure (" + request.getMaxTenureMonths() + " months)");
            }
        }
        if (request.getPenalty() != null && request.getPenalty() < 0) {
            throw new BusinessException("INVALID_PENALTY", "Penalty amount cannot be negative");
        }
        if (request.getPenaltyPercent() != null && request.getPenaltyPercent() < 0) {
            throw new BusinessException("INVALID_PENALTY_PERCENT", "Penalty percentage cannot be negative");
        }
    }

    private Double resolveEmi(Double requestedEmi, Double minAmount, Double maxAmount, Double interestRate, Integer tenureMonths, String mode) {
        if (requestedEmi != null && requestedEmi > 0) {
            return Math.round(requestedEmi * 100.0) / 100.0;
        }

        if (interestRate == null || tenureMonths == null || tenureMonths <= 0) {
            return null;
        }

        double principal = (minAmount != null && maxAmount != null) ? (minAmount + maxAmount) / 2.0 : (minAmount != null ? minAmount : 100000.0);
        if ("FLAT".equalsIgnoreCase(mode) || "FLAT_RATE".equalsIgnoreCase(mode)) {
            // Flat EMI = (P + (P * r * t)) / (t * 12)
            double years = tenureMonths / 12.0;
            double totalInterest = principal * (interestRate / 100.0) * years;
            return Math.round(((principal + totalInterest) / tenureMonths) * 100.0) / 100.0;
        } else {
            // Reducing balance EMI: P * r * (1+r)^n / ((1+r)^n - 1)
            double monthlyRate = (interestRate / 100.0) / 12.0;
            if (monthlyRate == 0) {
                return Math.round((principal / tenureMonths) * 100.0) / 100.0;
            }
            double factor = Math.pow(1.0 + monthlyRate, tenureMonths);
            double emi = (principal * monthlyRate * factor) / (factor - 1.0);
            return Math.round(emi * 100.0) / 100.0;
        }
    }

    private String getColValue(List<String> row, int colIndex) {
        if (colIndex >= 0 && colIndex < row.size()) {
            return row.get(colIndex).trim();
        }
        return null;
    }

    private Double parseDouble(String val, Double defaultVal) {
        if (val == null || val.isBlank()) return defaultVal;
        try {
            return Double.valueOf(val.replaceAll("[^0.0-9.]", ""));
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    private Double parseDoubleNullable(String val) {
        if (val == null || val.isBlank()) return null;
        try {
            return Double.valueOf(val.replaceAll("[^0.0-9.]", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseInt(String val, Integer defaultVal) {
        if (val == null || val.isBlank()) return defaultVal;
        try {
            return Integer.valueOf(val.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    public LoanProductResponse mapToResponse(LoanProduct p) {
        return LoanProductResponse.builder()
                .id(p.getId())
                .code(p.getCode())
                .name(p.getName())
                .description(p.getDescription())
                .emi(p.getEmi())
                .emiType(p.getEmiType())
                .minAmount(p.getMinAmount())
                .maxAmount(p.getMaxAmount())
                .interestRatePercent(p.getInterestRatePercent())
                .minTenureMonths(p.getMinTenureMonths())
                .maxTenureMonths(p.getMaxTenureMonths())
                .penalty(p.getPenalty())
                .penaltyPercent(p.getPenaltyPercent())
                .interestCalculationFrequency(p.getInterestCalculationFrequency())
                .interestCalculationMode(p.getInterestCalculationMode())
                .processingFeePercent(p.getProcessingFeePercent())
                .status(p.getStatus())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
