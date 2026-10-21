package com.bank.los.bank.loan;

import com.bank.los.bank.loan.dto.CreateLoanApplicationRequest;
import com.bank.los.bank.loan.dto.IncomeExpenseItemDto;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class LoanEligibilityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private String makerToken;
    private String adminToken;
    private String testProductCode;

    @BeforeEach
    void setUp() throws Exception {
        OrganizationContext.setCurrentOrganization("los_hdfc01_db");

        UserPrincipal makerPrincipal = UserPrincipal.builder()
                .id(2L)
                .email("maker@hdfcbank.com")
                .fullName("Loan Maker")
                .role("MAKER")
                .userType("STAFF")
                .organizationId(1L)
                .organizationCode("HDFC01")
                .organizationDbName("los_hdfc01_db")
                .branchId(1L)
                .active(true)
                .build();
        makerToken = jwtTokenProvider.generateAccessToken(makerPrincipal);

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

        testProductCode = "ELIG-PROD-" + System.currentTimeMillis();
        LoanProductRequest productReq = LoanProductRequest.builder()
                .code(testProductCode)
                .name("Eligibility Test Product")
                .minAmount(10000.0)
                .maxAmount(10000000.0)
                .interestRatePercent(11.5)
                .minTenureMonths(12)
                .maxTenureMonths(120)
                .status("ACTIVE")
                .build();

        mockMvc.perform(post("/api/v1/loan-products")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productReq)));
    }

    @Test
    @DisplayName("Should assess eligibility across Section 1, 2, 3 and perform CRUD on both routes")
    void testLoanEligibilityFullWorkflow() throws Exception {
        // Section 2: Repeatable income and expense items
        List<IncomeExpenseItemDto> cashflows = List.of(
                IncomeExpenseItemDto.builder().type("INCOME").source("Primary Salary").monthlyAmount(85000.0).build(),
                IncomeExpenseItemDto.builder().type("INCOME").source("Consulting").monthlyAmount(15000.0).build(),
                IncomeExpenseItemDto.builder().type("EXPENSE").source("House Rent").monthlyAmount(25000.0).build(),
                IncomeExpenseItemDto.builder().type("EXPENSE").source("Living Expenses").monthlyAmount(15000.0).build()
        );

        // Section 1 & Section 3
        CreateLoanApplicationRequest request = CreateLoanApplicationRequest.builder()
                .customerNumber("CUST-10099")
                .applicantName("Suresh Nair")
                .loanProduct(testProductCode)
                .requestedAmount(1000000.0)
                .annualInterestRate(11.5)
                .loanTermMonths(60)
                .incomeExpenses(cashflows)
                .existingMonthlyDebt(10000.0)
                .cibilScore(760)
                .collateralValue(2000000.0)
                .build();

        // 1. CREATE application on dual route /api/v1/loan-eligibility
        MvcResult createResult = mockMvc.perform(post("/api/v1/loan-eligibility")
                        .header("Authorization", "Bearer " + makerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.customerNumber").value("CUST-10099"))
                .andExpect(jsonPath("$.data.applicantName").value("Suresh Nair"))
                .andExpect(jsonPath("$.data.productCode").value(testProductCode))
                .andExpect(jsonPath("$.data.requestedAmount").value(1000000.0))
                .andExpect(jsonPath("$.data.annualInterestRate").value(11.5))
                .andExpect(jsonPath("$.data.loanTermMonths").value(60))
                .andExpect(jsonPath("$.data.totalMonthlyIncome").value(100000.0))
                .andExpect(jsonPath("$.data.totalMonthlyExpenses").value(40000.0))
                .andExpect(jsonPath("$.data.netMonthlyIncome").value(60000.0))
                .andExpect(jsonPath("$.data.existingMonthlyDebt").value(10000.0))
                .andExpect(jsonPath("$.data.cibilScore").value(760))
                .andExpect(jsonPath("$.data.collateralValue").value(2000000.0))
                .andExpect(jsonPath("$.data.emi").isNumber())
                .andExpect(jsonPath("$.data.dtiRatio").isNumber())
                .andExpect(jsonPath("$.data.maxEligibleAmount").isNumber())
                .andExpect(jsonPath("$.data.eligibilityStatus").isNotEmpty())
                .andExpect(jsonPath("$.data.incomeExpenses").isArray())
                .andReturn();

        JsonNode root = objectMapper.readTree(createResult.getResponse().getContentAsString());
        Long appId = root.path("data").path("id").asLong();
        String appNumber = root.path("data").path("applicationNumber").asText();

        assertNotNull(appId);
        assertTrue(appNumber.startsWith("APP-HDFC01-"));

        // 2. GET by ID via alternate route /api/v1/loan-applications
        mockMvc.perform(get("/api/v1/loan-applications/" + appId)
                        .header("Authorization", "Bearer " + makerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(appId))
                .andExpect(jsonPath("$.data.applicationNumber").value(appNumber))
                .andExpect(jsonPath("$.data.incomeExpenses.length()").value(4));

        // 3. GET by Application Number
        mockMvc.perform(get("/api/v1/loan-eligibility/number/" + appNumber)
                        .header("Authorization", "Bearer " + makerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(appId));

        // 4. UPDATE application with revised income / requested amount
        List<IncomeExpenseItemDto> updatedCashflows = List.of(
                IncomeExpenseItemDto.builder().type("INCOME").source("Primary Salary").monthlyAmount(120000.0).build(),
                IncomeExpenseItemDto.builder().type("EXPENSE").source("Total Expenses").monthlyAmount(20000.0).build()
        );

        CreateLoanApplicationRequest updateReq = CreateLoanApplicationRequest.builder()
                .customerNumber("CUST-10099")
                .applicantName("Suresh Nair (Updated)")
                .loanProduct(testProductCode)
                .requestedAmount(1500000.0)
                .annualInterestRate(11.0)
                .loanTermMonths(72)
                .incomeExpenses(updatedCashflows)
                .existingMonthlyDebt(5000.0)
                .cibilScore(800)
                .collateralValue(2500000.0)
                .build();

        mockMvc.perform(put("/api/v1/loan-eligibility/" + appId)
                        .header("Authorization", "Bearer " + makerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.applicantName").value("Suresh Nair (Updated)"))
                .andExpect(jsonPath("$.data.requestedAmount").value(1500000.0))
                .andExpect(jsonPath("$.data.totalMonthlyIncome").value(120000.0))
                .andExpect(jsonPath("$.data.totalMonthlyExpenses").value(20000.0))
                .andExpect(jsonPath("$.data.netMonthlyIncome").value(100000.0))
                .andExpect(jsonPath("$.data.cibilScore").value(800));

        // 5. LIST Applications
        mockMvc.perform(get("/api/v1/loan-eligibility")
                        .header("Authorization", "Bearer " + makerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("Should reject application with invalid CIBIL score (< 300 or > 900)")
    void testInvalidCibilScoreValidation() throws Exception {
        CreateLoanApplicationRequest invalidCibil = CreateLoanApplicationRequest.builder()
                .customerNumber("CUST-INVALID")
                .applicantName("Invalid Applicant")
                .loanProduct(testProductCode)
                .requestedAmount(500000.0)
                .cibilScore(250) // Invalid: < 300
                .build();

        mockMvc.perform(post("/api/v1/loan-eligibility")
                        .header("Authorization", "Bearer " + makerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCibil)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Should reject application with negative existing monthly debt")
    void testNegativeMonthlyDebtValidation() throws Exception {
        CreateLoanApplicationRequest negativeDebt = CreateLoanApplicationRequest.builder()
                .customerNumber("CUST-INVALID-DEBT")
                .applicantName("Invalid Debt")
                .loanProduct(testProductCode)
                .requestedAmount(500000.0)
                .existingMonthlyDebt(-1000.0) // Invalid
                .cibilScore(700)
                .build();

        mockMvc.perform(post("/api/v1/loan-eligibility")
                        .header("Authorization", "Bearer " + makerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(negativeDebt)))
                .andExpect(status().is4xxClientError());
    }
}
