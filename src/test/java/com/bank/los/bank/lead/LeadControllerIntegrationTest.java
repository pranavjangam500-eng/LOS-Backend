package com.bank.los.bank.lead;

import com.bank.los.bank.lead.dto.LeadAssignRequest;
import com.bank.los.bank.lead.dto.LeadRequest;
import com.bank.los.bank.lead.entity.Lead;
import com.bank.los.bank.lead.repository.LeadRepository;
import com.bank.los.security.JwtTokenProvider;
import com.bank.los.security.UserPrincipal;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class LeadControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String internalAdminToken;
    private String makerToken;

    @BeforeEach
    void setUp() {
        // 1. Bank Admin token (ADMIN)
        UserPrincipal adminPrincipal = UserPrincipal.builder()
                .id(1L)
                .email("admin@hdfcbank.com")
                .fullName("Bank Admin")
                .role("ADMIN")
                .userType("STAFF")
                .organizationId(1L)
                .organizationCode("HDFC01")
                .branchId(1L)
                .active(true)
                .build();
        adminToken = jwtTokenProvider.generateAccessToken(adminPrincipal);

        // 2. Internal Admin token (INTERNAL_ADMIN)
        UserPrincipal internalAdminPrincipal = UserPrincipal.builder()
                .id(2L)
                .email("admin@losplatform.com")
                .fullName("Super Admin")
                .role("INTERNAL_ADMIN")
                .userType("STAFF")
                .organizationId(1L)
                .organizationCode("HDFC01")
                .active(true)
                .build();
        internalAdminToken = jwtTokenProvider.generateAccessToken(internalAdminPrincipal);

        // 3. Maker token (MAKER)
        UserPrincipal makerPrincipal = UserPrincipal.builder()
                .id(3L)
                .email("maker@hdfcbank.com")
                .fullName("Maker User")
                .role("MAKER")
                .userType("STAFF")
                .organizationId(1L)
                .organizationCode("HDFC01")
                .branchId(1L)
                .active(true)
                .build();
        makerToken = jwtTokenProvider.generateAccessToken(makerPrincipal);
    }

    private LeadRequest createSampleLeadRequest() {
        LeadRequest req = new LeadRequest();
        req.setCustomerName("Rohan Verma");
        req.setDob("1992-08-15");
        req.setAge(34);
        req.setCustomerType("Individual");
        req.setPassportNumber("P1234567");
        req.setPassportExpiryDate("2033-08-14");
        req.setPanNumber("ABCDE1234F");
        req.setAadhaarNumber("123456789012");
        req.setLoanProductType("Personal Loan");
        req.setLoanAmount(500000.0);
        req.setInterestRate(10.5);
        req.setTotalInterest(80000.0);
        req.setTenure(24);
        req.setNumberOfInstalments(24);
        req.setEmi(24166.0);
        req.setAnnualIncome(1200000.0);
        req.setLeadAcquisitionChannel("Website");
        req.setDate("2026-10-10");
        req.setSourcingAgentPartnerId("AGT-101");
        req.setAgentPartnerName("FinCorp Partners");
        req.setEmployeeId("EMP-001");
        req.setEmployeeName("Sonia Roy");
        return req;
    }

    @Test
    @DisplayName("Unauthenticated request to /api/leads should return 403 Forbidden")
    void testUnauthenticatedAccessFails() throws Exception {
        mockMvc.perform(get("/api/leads"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("MAKER role cannot access Lead endpoints (403 Forbidden)")
    void testMakerAccessForbidden() throws Exception {
        mockMvc.perform(get("/api/leads")
                        .header("Authorization", "Bearer " + makerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Complete Lead Lifecycle: Preview ID -> Create -> Get -> Update -> Assign -> Export")
    void testCompleteLeadLifecycleFlow() throws Exception {
        // 1. Preview Next Lead ID
        MvcResult nextIdRes = mockMvc.perform(get("/api/leads/next-id")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();
        String previewId = nextIdRes.getResponse().getContentAsString();
        assertNotNull(previewId);
        assertTrue(previewId.startsWith("LD"));

        // 2. Create Lead
        LeadRequest createReq = createSampleLeadRequest();
        MvcResult createRes = mockMvc.perform(post("/api/leads")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.leadId").isNotEmpty())
                .andExpect(jsonPath("$.leadStatus").value("NEW"))
                .andExpect(jsonPath("$.customerName").value("Rohan Verma"))
                .andReturn();

        Lead createdLead = objectMapper.readValue(createRes.getResponse().getContentAsString(), Lead.class);
        String leadId = createdLead.getLeadId();

        // 3. Get Lead by ID
        mockMvc.perform(get("/api/leads/" + leadId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.leadId").value(leadId))
                .andExpect(jsonPath("$.leadStatus").value("NEW"))
                .andExpect(jsonPath("$.annualIncome").value(1200000.0));

        // 4. Update Lead
        LeadRequest updateReq = new LeadRequest();
        updateReq.setLoanAmount(600000.0);
        updateReq.setInterestRate(11.0);
        updateReq.setAnnualIncome(1400000.0);
        mockMvc.perform(put("/api/leads/" + leadId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.leadId").value(leadId))
                .andExpect(jsonPath("$.loanAmount").value(600000.0))
                .andExpect(jsonPath("$.interestRate").value(11.0));

        // 5. Assign Lead
        LeadAssignRequest assignReq = new LeadAssignRequest("EMP-HDFC-002", "EMP-HDFC-001");
        mockMvc.perform(put("/api/leads/" + leadId + "/assign")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.leadId").value(leadId))
                .andExpect(jsonPath("$.leadStatus").value("ASSIGNED"))
                .andExpect(jsonPath("$.assignedEmployeeId").value("EMP-HDFC-002"));

        // 6. Get All Leads
        mockMvc.perform(get("/api/leads")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // 7. Export Leads
        mockMvc.perform(get("/api/leads/export")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv;charset=UTF-8"));

        // 8. Sample CSV
        mockMvc.perform(get("/api/leads/sample-csv")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv;charset=UTF-8"));
    }

    @Test
    @DisplayName("INTERNAL_ADMIN can also access Lead endpoints")
    void testInternalAdminAccess() throws Exception {
        mockMvc.perform(get("/api/leads")
                        .header("Authorization", "Bearer " + internalAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
