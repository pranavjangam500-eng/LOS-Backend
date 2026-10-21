package com.bank.los.bank.product;

import com.bank.los.bank.product.dto.LoanProductRequest;
import com.bank.los.config.OrganizationContext;
import com.bank.los.security.JwtTokenProvider;
import com.bank.los.security.UserPrincipal;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class LoanProductIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;

    @BeforeEach
    void setUp() {
        OrganizationContext.setCurrentOrganization("los_hdfc01_db");

        UserPrincipal adminPrincipal = UserPrincipal.builder()
                .id(1L)
                .email("admin@hdfcbank.com")
                .fullName("Bank Admin")
                .role("ADMIN")
                .userType("STAFF")
                .organizationId(1L)
                .organizationCode("HDFC01")
                .organizationDbName("los_hdfc01_db")
                .branchId(1L)
                .active(true)
                .build();
        adminToken = jwtTokenProvider.generateAccessToken(adminPrincipal);
    }

    @Test
    @DisplayName("Should successfully create, get, edit, list, and export loan products with all fields")
    void testProductManagementLifecycle() throws Exception {
        String uniqueCode = "PROD-TEST-" + System.currentTimeMillis();

        // 1. ADD / CREATE Product
        LoanProductRequest createReq = LoanProductRequest.builder()
                .code(uniqueCode)
                .name("Super Home Loan")
                .description("Affordable home loan with low interest")
                .minAmount(100000.0)
                .maxAmount(5000000.0)
                .interestRatePercent(9.5)
                .minTenureMonths(12)
                .maxTenureMonths(240)
                .emi(35000.0)
                .emiType("REDUCING_BALANCE")
                .penalty(500.0)
                .penaltyPercent(2.5)
                .interestCalculationFrequency("MONTHLY")
                .interestCalculationMode("REDUCING")
                .processingFeePercent(0.75)
                .status("ACTIVE")
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/v1/loan-products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.code").value(uniqueCode))
                .andExpect(jsonPath("$.data.name").value("Super Home Loan"))
                .andExpect(jsonPath("$.data.emi").value(35000.0))
                .andExpect(jsonPath("$.data.emiType").value("REDUCING_BALANCE"))
                .andExpect(jsonPath("$.data.penalty").value(500.0))
                .andExpect(jsonPath("$.data.penaltyPercent").value(2.5))
                .andExpect(jsonPath("$.data.interestCalculationFrequency").value("MONTHLY"))
                .andExpect(jsonPath("$.data.interestCalculationMode").value("REDUCING"))
                .andReturn();

        JsonNode root = objectMapper.readTree(createResult.getResponse().getContentAsString());
        Long productId = root.path("data").path("id").asLong();
        assertTrue(productId > 0);

        // 2. GET by ID
        mockMvc.perform(get("/api/v1/loan-products/" + productId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(productId))
                .andExpect(jsonPath("$.data.code").value(uniqueCode))
                .andExpect(jsonPath("$.data.penaltyPercent").value(2.5));

        // 3. GET by Code
        mockMvc.perform(get("/api/v1/loan-products/code/" + uniqueCode)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.code").value(uniqueCode));

        // 4. EDIT / UPDATE Product
        LoanProductRequest updateReq = LoanProductRequest.builder()
                .code(uniqueCode)
                .name("Super Home Loan Platinum")
                .description("Updated description with lower penalty")
                .minAmount(200000.0)
                .maxAmount(7500000.0)
                .interestRatePercent(8.9)
                .minTenureMonths(24)
                .maxTenureMonths(360)
                .emi(42000.0)
                .emiType("REDUCING_BALANCE")
                .penalty(250.0)
                .penaltyPercent(1.5)
                .interestCalculationFrequency("MONTHLY")
                .interestCalculationMode("REDUCING")
                .processingFeePercent(0.5)
                .status("ACTIVE")
                .build();

        mockMvc.perform(put("/api/v1/loan-products/" + productId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Super Home Loan Platinum"))
                .andExpect(jsonPath("$.data.minAmount").value(200000.0))
                .andExpect(jsonPath("$.data.maxAmount").value(7500000.0))
                .andExpect(jsonPath("$.data.penalty").value(250.0))
                .andExpect(jsonPath("$.data.penaltyPercent").value(1.5));

        // 5. LIST Products
        mockMvc.perform(get("/api/v1/loan-products")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        // 6. EXPORT to CSV
        MvcResult exportResult = mockMvc.perform(get("/api/v1/loan-products/export")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", org.hamcrest.Matchers.containsString("text/csv")))
                .andReturn();

        String csvContent = exportResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertTrue(csvContent.contains("Product Code,Product Name"));
        assertTrue(csvContent.contains(uniqueCode));

        // 7. SAMPLE CSV
        MvcResult sampleResult = mockMvc.perform(get("/api/v1/loan-products/sample-csv")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();
        String sampleCsv = sampleResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertTrue(sampleCsv.contains("HOME_LOAN"));
        assertTrue(sampleCsv.contains("PERSONAL_LOAN"));
    }

    @Test
    @DisplayName("Should bulk import loan products from CSV")
    void testImportLoanProductsCsv() throws Exception {
        String importCode = "IMP-TEST-" + System.currentTimeMillis();
        String csvData = "Product Code,Product Name,Description,EMI,EMI Type,Min Amount,Max Amount,Interest Rate %,Min Tenure Months,Max Tenure Months,Penalty,Penalty %,Interest Calculation Frequency,Interest Calculation Mode,Processing Fee %,Status\r\n"
                + importCode + ",Imported Auto Loan,Vehicle financing,15000.0,REDUCING_BALANCE,50000.0,1500000.0,10.5,12,60,300.0,2.0,MONTHLY,REDUCING,1.0,ACTIVE\r\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "products.csv", "text/csv", csvData.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/v1/loan-products/import")
                        .file(file)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.successCount").value(1));

        // Verify product now exists
        mockMvc.perform(get("/api/v1/loan-products/code/" + importCode)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value(importCode))
                .andExpect(jsonPath("$.data.name").value("Imported Auto Loan"));
    }

    @Test
    @DisplayName("Should reject invalid loan product with minAmount > maxAmount")
    void testProductValidationMinMaxAmount() throws Exception {
        LoanProductRequest invalidReq = LoanProductRequest.builder()
                .code("INVALID-AMT-" + System.currentTimeMillis())
                .name("Invalid Amount Product")
                .minAmount(500000.0)
                .maxAmount(100000.0) // Invalid: min > max
                .interestRatePercent(10.0)
                .minTenureMonths(12)
                .maxTenureMonths(60)
                .build();

        mockMvc.perform(post("/api/v1/loan-products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().is4xxClientError());
    }
}
