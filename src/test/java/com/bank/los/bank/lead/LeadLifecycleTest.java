package com.bank.los.bank.lead;

import com.bank.los.bank.lead.dto.LeadRequest;
import com.bank.los.bank.lead.entity.Lead;
import com.bank.los.bank.lead.exception.ValidationException;
import com.bank.los.bank.lead.mapper.LeadMapper;
import com.bank.los.bank.lead.model.CsvImportResult;
import com.bank.los.bank.lead.repository.LeadRepository;
import com.bank.los.bank.lead.service.CsvHelper;
import com.bank.los.bank.lead.service.LeadService;
import com.bank.los.bank.lead.service.LeadUserService;
import com.bank.los.bank.lead.service.LeadValidator;
import com.bank.los.bank.master.entity.OrganizationUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unused")
class LeadLifecycleTest {

    @Mock
    private LeadRepository leadRepository;

    @Mock
    private LeadUserService leadUserService;

    @Spy
    private final LeadValidator leadValidator = new LeadValidator();

    @Spy
    private final LeadMapper leadMapper = new LeadMapper();

    @InjectMocks
    private LeadService leadService;

    @BeforeEach
    void setUp() {
        lenient().when(leadRepository.save(any(Lead.class))).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(leadRepository.findByLeadIdStartingWithOrderByLeadIdDesc(anyString())).thenReturn(Collections.emptyList());
    }

    private LeadRequest createBaseLeadRequest() {
        LeadRequest req = new LeadRequest();
        req.setCustomerName("Siddharth Rao");
        req.setDob("1990-05-20");
        req.setAge(36);
        req.setCustomerType("Individual");
        req.setPassportNumber("P9876543");
        req.setPassportExpiryDate("2032-05-19");
        req.setPanNumber("ABCDE1234F");
        req.setAadhaarNumber("123456789012");
        req.setLoanProductType("Personal Loan");
        req.setLoanAmount(1200000.0);
        req.setInterestRate(11.5);
        req.setTotalInterest(244428.0);
        req.setTenure(36);
        req.setNumberOfInstalments(36);
        req.setEmi(40123.0);
        req.setAnnualIncome(1800000.0);

        // Step 4: Referral Details
        req.setLeadAcquisitionChannel("Website");
        req.setDate("2026-10-07");
        req.setSourcingAgentPartnerId("AGT-555");
        req.setAgentPartnerName("Apex Partners");
        req.setEmployeeId("EMP101");
        req.setEmployeeName("Anita Roy");

        return req;
    }

    @Test
    @DisplayName("Create Lead should succeed with passportExpiryDate, interestRate, totalInterest and all Step 4 Referral Details")
    void testCreateLeadSuccess() {
        LeadRequest request = createBaseLeadRequest();

        Lead created = leadService.createLead(request);

        assertNotNull(created);
        assertNotNull(created.getLeadId());
        assertTrue(created.getLeadId().startsWith("LD"));
        assertEquals("NEW", created.getLeadStatus());
        assertEquals("Siddharth Rao", created.getCustomerName());
        assertEquals("2032-05-19", created.getPassportExpiryDate());
        assertEquals(11.5, created.getInterestRate());
        assertEquals(244428.0, created.getTotalInterest());

        // Step 4 Referral Details verification
        assertEquals("Website", created.getLeadAcquisitionChannel());
        assertEquals("2026-10-07", created.getDate());
        assertEquals("AGT-555", created.getSourcingAgentPartnerId());
        assertEquals("Apex Partners", created.getAgentPartnerName());
        assertEquals("EMP101", created.getEmployeeId());
        assertEquals("Anita Roy", created.getEmployeeName());

        // Verify aliases backward compatibility
        assertEquals("Website", created.getSourcingChannel());
        assertEquals("2026-10-07", created.getReferralDate());
        assertEquals("AGT-555", created.getLspPartnerCode());
        assertEquals("EMP101", created.getSourcingEmployeeId());
        assertEquals("Anita Roy", created.getSourcingEmployeeName());
    }

    @Test
    @DisplayName("Create Lead should enrich employeeId and employeeName using existing bank User table/entity")
    void testCreateLeadWithBankUserEnrichment() {
        OrganizationUser bankUser = OrganizationUser.builder()
                .empNo("EMP999")
                .firstName("Rohan")
                .lastName("Sharma")
                .build();
        lenient().when(leadUserService.lookupBankUser("EMP999", null)).thenReturn(Optional.of(bankUser));

        LeadRequest request = createBaseLeadRequest();
        request.setEmployeeId("EMP999");
        request.setEmployeeName(null);

        Lead created = leadService.createLead(request);

        assertNotNull(created);
        assertEquals("EMP999", created.getEmployeeId());
        assertEquals("Rohan Sharma", created.getEmployeeName());
    }

    @Test
    @DisplayName("Get Lead by ID should retrieve the persisted lead with Step 4 Referral Details")
    void testGetLeadById() {
        Lead lead = new Lead(createBaseLeadRequest());
        lead.setLeadId("LD2026100001");
        lead.setLeadStatus("NEW");

        lenient().when(leadRepository.findById("LD2026100001")).thenReturn(Optional.of(lead));

        Optional<Lead> result = leadService.getLeadById("LD2026100001");

        assertTrue(result.isPresent());
        assertEquals("LD2026100001", result.get().getLeadId());
        assertEquals("2032-05-19", result.get().getPassportExpiryDate());
        assertEquals(11.5, result.get().getInterestRate());
        assertEquals(244428.0, result.get().getTotalInterest());

        // Step 4 Referral Details verification
        assertEquals("Website", result.get().getLeadAcquisitionChannel());
        assertEquals("2026-10-07", result.get().getDate());
        assertEquals("AGT-555", result.get().getSourcingAgentPartnerId());
        assertEquals("Apex Partners", result.get().getAgentPartnerName());
        assertEquals("EMP101", result.get().getEmployeeId());
        assertEquals("Anita Roy", result.get().getEmployeeName());
    }

    @Test
    @DisplayName("Update Lead should update interestRate, totalInterest, passportExpiryDate and Step 4 Referral Details correctly")
    void testUpdateLeadSuccess() {
        Lead existingLead = new Lead(createBaseLeadRequest());
        existingLead.setLeadId("LD2026100001");
        existingLead.setLeadStatus("NEW");

        lenient().when(leadRepository.findById("LD2026100001")).thenReturn(Optional.of(existingLead));

        LeadRequest updateReq = new LeadRequest();
        updateReq.setPassportExpiryDate("2035-10-15");
        updateReq.setInterestRate(12.25);
        updateReq.setTotalInterest(260000.0);

        // Update Step 4 Referral Details
        updateReq.setLeadAcquisitionChannel("Mobile App");
        updateReq.setDate("2026-10-10");
        updateReq.setSourcingAgentPartnerId("AGT-888");
        updateReq.setAgentPartnerName("FinTech Direct");
        updateReq.setEmployeeId("EMP202");
        updateReq.setEmployeeName("Vikram Malhotra");

        Lead updated = leadService.updateLead("LD2026100001", updateReq);

        assertNotNull(updated);
        assertEquals("LD2026100001", updated.getLeadId());
        assertEquals("2035-10-15", updated.getPassportExpiryDate());
        assertEquals(12.25, updated.getInterestRate());
        assertEquals(260000.0, updated.getTotalInterest());

        // Step 4 Referral Details verification
        assertEquals("Mobile App", updated.getLeadAcquisitionChannel());
        assertEquals("2026-10-10", updated.getDate());
        assertEquals("AGT-888", updated.getSourcingAgentPartnerId());
        assertEquals("FinTech Direct", updated.getAgentPartnerName());
        assertEquals("EMP202", updated.getEmployeeId());
        assertEquals("Vikram Malhotra", updated.getEmployeeName());
    }

    @Test
    @DisplayName("Export Leads to CSV should include Step 4 Referral Details headers and row data")
    void testExportLeadsToCsv() {
        Lead lead = new Lead(createBaseLeadRequest());
        lead.setLeadId("LD2026100001");
        lead.setLeadStatus("NEW");

        lenient().when(leadRepository.findAll()).thenReturn(List.of(lead));

        byte[] csvBytes = leadService.exportLeadsToCsv();
        assertNotNull(csvBytes);
        String csvContent = new String(csvBytes, StandardCharsets.UTF_8);

        String[] lines = csvContent.split("\r?\n");
        assertTrue(lines.length >= 2, "CSV should contain header and at least 1 data row");

        String header = lines[0];
        assertTrue(header.contains("Passport Expiry Date"), "Header must include 'Passport Expiry Date'");
        assertTrue(header.contains("Interest Rate"), "Header must include 'Interest Rate'");
        assertTrue(header.contains("Total Interest"), "Header must include 'Total Interest'");

        // Step 4 Referral Details headers
        assertTrue(header.contains("Lead Acquisition Channel"), "Header must include 'Lead Acquisition Channel'");
        assertTrue(header.contains("Date"), "Header must include 'Date'");
        assertTrue(header.contains("Sourcing Agent / Partner ID"), "Header must include 'Sourcing Agent / Partner ID'");
        assertTrue(header.contains("Agent / Partner Name"), "Header must include 'Agent / Partner Name'");
        assertTrue(header.contains("Employee ID"), "Header must include 'Employee ID'");
        assertTrue(header.contains("Employee Name"), "Header must include 'Employee Name'");

        assertFalse(header.contains("Mobile Number"), "Header must NOT include 'Mobile Number'");
        assertFalse(header.contains("OTP"), "Header must NOT include 'OTP'");
        assertFalse(header.contains("Property Value"), "Header must NOT include 'Property Value'");

        String dataRow = lines[1];
        assertTrue(dataRow.contains("2032-05-19"), "Data row must contain passport expiry date");
        assertTrue(dataRow.contains("11.5"), "Data row must contain interest rate");
        assertTrue(dataRow.contains("244428.0"), "Data row must contain total interest");

        // Step 4 Referral Details row values
        assertTrue(dataRow.contains("Website"), "Data row must contain lead acquisition channel");
        assertTrue(dataRow.contains("2026-10-07"), "Data row must contain date");
        assertTrue(dataRow.contains("AGT-555"), "Data row must contain sourcing agent partner id");
        assertTrue(dataRow.contains("Apex Partners"), "Data row must contain agent partner name");
        assertTrue(dataRow.contains("EMP101"), "Data row must contain employee id");
        assertTrue(dataRow.contains("Anita Roy"), "Data row must contain employee name");
    }

    @Test
    @DisplayName("Import Leads from CSV should parse Step 4 Referral Details successfully")
    void testImportLeadsFromCsv() {
        String csvHeader = CsvHelper.toCsvLine(List.of(CsvHelper.CSV_HEADERS));
        String csvRow = "LD2026100002,Sunil Verma,1985-03-12,41,Individual,XYZAB1234C,VALID,987654321098,VALID,Resident Indian,Male,Married,M1234567,2034-08-20,NO_DUPLICATE,CLEAR,Verma,sunil@example.com,560001,2,Personal Loan,800000.0,Home Renovation,24,24,37500.0,10.25,100000.0,50000.0,None,Salaried,1400000.0,Manager,TCS,Bangalore,Karnataka,95000.0,10000.0,ICICI Bank,987654321012,true,CHECKED_CLEAR,38.5,50.0,2.1,50000.0,Website,2026-10-01,AGT-101,Rajesh,EMP101,Anita Roy,NEW,EMP102,2026-10-02T10:00:00,EMP101\n";
        String csvContent = csvHeader + csvRow;

        ByteArrayInputStream in = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));
        CsvImportResult result = leadService.importLeadsFromCsv(in);

        assertNotNull(result);
        assertEquals(1, result.getSuccessCount(), "1 lead should import successfully");
        assertEquals(0, result.getFailureCount(), "0 failures expected");
        assertNotNull(result.getImportedLeads());
        assertEquals(1, result.getImportedLeads().size());

        Lead importedLead = result.getImportedLeads().get(0);
        assertEquals("Sunil Verma", importedLead.getCustomerName());
        assertEquals("2034-08-20", importedLead.getPassportExpiryDate());
        assertEquals(10.25, importedLead.getInterestRate());
        assertEquals(100000.0, importedLead.getTotalInterest());

        // Step 4 Referral Details verification
        assertEquals("Website", importedLead.getLeadAcquisitionChannel());
        assertEquals("2026-10-01", importedLead.getDate());
        assertEquals("AGT-101", importedLead.getSourcingAgentPartnerId());
        assertEquals("Rajesh", importedLead.getAgentPartnerName());
        assertEquals("EMP101", importedLead.getEmployeeId());
        assertEquals("Anita Roy", importedLead.getEmployeeName());
    }

    @Test
    @DisplayName("Validation should fail if sourcingAgentPartnerId is missing for Branch acquisition channel")
    void testValidationMissingPartnerIdForBranch() {
        Lead lead = new Lead(createBaseLeadRequest());
        lead.setLeadAcquisitionChannel("Branch Office");
        lead.setSourcingAgentPartnerId(null);
        lead.setLspPartnerCode(null);

        ValidationException ex = assertThrows(ValidationException.class, () -> leadValidator.validateAndNormalize(lead));
        assertTrue(ex.getMessage().contains("Sourcing Agent / Partner ID is mandatory when Lead Acquisition Channel is 'Branch Office'"));
    }

    @Test
    @DisplayName("Validation should fail if leadAcquisitionChannel is invalid")
    void testValidationInvalidLeadAcquisitionChannel() {
        Lead lead = new Lead(createBaseLeadRequest());
        lead.setLeadAcquisitionChannel("InvalidChannelName");

        ValidationException ex = assertThrows(ValidationException.class, () -> leadValidator.validateAndNormalize(lead));
        assertTrue(ex.getMessage().contains("Lead Acquisition Channel must be one of"));
    }

    @Test
    @DisplayName("Validation should fail if passportExpiryDate format is invalid")
    void testValidationInvalidPassportExpiryDate() {
        Lead lead = new Lead(createBaseLeadRequest());
        lead.setPassportExpiryDate("19-05-2032"); // invalid format, should be YYYY-MM-DD

        ValidationException ex = assertThrows(ValidationException.class, () -> leadValidator.validateAndNormalize(lead));
        assertTrue(ex.getMessage().contains("Passport Expiry Date must follow format YYYY-MM-DD"));
    }

    @Test
    @DisplayName("Validation should fail if interestRate is negative")
    void testValidationNegativeInterestRate() {
        Lead lead = new Lead(createBaseLeadRequest());
        lead.setInterestRate(-5.0);

        ValidationException ex = assertThrows(ValidationException.class, () -> leadValidator.validateAndNormalize(lead));
        assertTrue(ex.getMessage().contains("Interest Rate must be a non-negative number"));
    }

    @Test
    @DisplayName("Validation should fail if totalInterest is negative")
    void testValidationNegativeTotalInterest() {
        Lead lead = new Lead(createBaseLeadRequest());
        lead.setTotalInterest(-1000.0);

        ValidationException ex = assertThrows(ValidationException.class, () -> leadValidator.validateAndNormalize(lead));
        assertTrue(ex.getMessage().contains("Total Interest must be a non-negative amount"));
    }

    @Test
    @DisplayName("Validation should pass when mobileNumber and otp are omitted")
    void testValidationWithoutMobileOrOtp() {
        Lead lead = new Lead(createBaseLeadRequest());
        assertDoesNotThrow(() -> leadValidator.validateAndNormalize(lead));
    }
}

