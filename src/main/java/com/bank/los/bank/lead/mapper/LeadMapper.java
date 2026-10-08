package com.bank.los.bank.lead.mapper;

import com.bank.los.bank.lead.dto.LeadRequest;
import com.bank.los.bank.lead.entity.Lead;
import org.springframework.stereotype.Component;

/**
 * Mapper component between LeadRequest DTO and Lead entity, ensuring all 4-section
 * origination form fields are consistently mapped, with first-class handling of Step 4
 * Referral Details:
 * - leadAcquisitionChannel
 * - date
 * - sourcingAgentPartnerId
 * - agentPartnerName
 * - employeeId
 * - employeeName
 */
@Component
public class LeadMapper {

    /**
     * Converts a LeadRequest DTO into a Lead entity.
     */
    public Lead toEntity(LeadRequest request) {
        if (request == null) {
            return null;
        }
        if (request instanceof Lead lead) {
            return lead;
        }
        return new Lead(request);
    }

    /**
     * Updates an existing Lead entity in-place from a LeadRequest DTO across all 4 sections.
     * System-managed fields (leadId, leadStatus, assignment fields) are strictly untouched.
     */
    public void updateEntity(Lead existingLead, LeadRequest updateData) {
        if (existingLead == null || updateData == null) {
            return;
        }

        // 1. Personal Details
        if (updateData.getFirstNameBusinessName() != null) existingLead.setFirstNameBusinessName(updateData.getFirstNameBusinessName());
        if (updateData.getLastName() != null) existingLead.setLastName(updateData.getLastName());
        if (updateData.getDob() != null) existingLead.setDob(updateData.getDob());
        if (updateData.getAge() != null) existingLead.setAge(updateData.getAge());
        if (updateData.getCustomerType() != null) existingLead.setCustomerType(updateData.getCustomerType());
        if (updateData.getPanNumber() != null) existingLead.setPanNumber(updateData.getPanNumber());
        if (updateData.getPanValidationStatus() != null) existingLead.setPanValidationStatus(updateData.getPanValidationStatus());
        if (updateData.getAadhaarNumber() != null) existingLead.setAadhaarNumber(updateData.getAadhaarNumber());
        if (updateData.getAadhaarValidationStatus() != null) existingLead.setAadhaarValidationStatus(updateData.getAadhaarValidationStatus());
        if (updateData.getResidentialStatus() != null) existingLead.setResidentialStatus(updateData.getResidentialStatus());
        if (updateData.getGender() != null) existingLead.setGender(updateData.getGender());
        if (updateData.getMaritalStatus() != null) existingLead.setMaritalStatus(updateData.getMaritalStatus());
        if (updateData.getPassportNumber() != null) existingLead.setPassportNumber(updateData.getPassportNumber());
        if (updateData.getPassportExpiryDate() != null) existingLead.setPassportExpiryDate(updateData.getPassportExpiryDate());
        if (updateData.getDedupeStatus() != null) existingLead.setDedupeStatus(updateData.getDedupeStatus());
        if (updateData.getBlacklistStatus() != null) existingLead.setBlacklistStatus(updateData.getBlacklistStatus());
        if (updateData.getNumberOfDependents() != null) existingLead.setNumberOfDependents(updateData.getNumberOfDependents());
        if (updateData.getEmailAddress() != null) existingLead.setEmailAddress(updateData.getEmailAddress());
        if (updateData.getPinCode() != null) existingLead.setPinCode(updateData.getPinCode());

        // 2. Loan Details
        if (updateData.getLoanProductType() != null) existingLead.setLoanProductType(updateData.getLoanProductType());
        if (updateData.getLoanAmount() != null) existingLead.setLoanAmount(updateData.getLoanAmount());
        if (updateData.getPurposeOfLoan() != null) existingLead.setPurposeOfLoan(updateData.getPurposeOfLoan());
        if (updateData.getTenure() != null) existingLead.setTenure(updateData.getTenure());
        if (updateData.getNumberOfInstalments() != null) existingLead.setNumberOfInstalments(updateData.getNumberOfInstalments());
        if (updateData.getEmi() != null) existingLead.setEmi(updateData.getEmi());
        if (updateData.getInterestRate() != null) existingLead.setInterestRate(updateData.getInterestRate());
        if (updateData.getTotalInterest() != null) existingLead.setTotalInterest(updateData.getTotalInterest());
        if (updateData.getSecurityAmount() != null) existingLead.setSecurityAmount(updateData.getSecurityAmount());
        if (updateData.getDownPaymentCollateral() != null) existingLead.setDownPaymentCollateral(updateData.getDownPaymentCollateral());

        // 3. Income Profile
        if (updateData.getEmploymentType() != null) existingLead.setEmploymentType(updateData.getEmploymentType());
        if (updateData.getAnnualIncome() != null) existingLead.setAnnualIncome(updateData.getAnnualIncome());
        if (updateData.getDesignation() != null) existingLead.setDesignation(updateData.getDesignation());
        if (updateData.getEmployerBusinessName() != null) existingLead.setEmployerBusinessName(updateData.getEmployerBusinessName());
        if (updateData.getLocation() != null) existingLead.setLocation(updateData.getLocation());
        if (updateData.getState() != null) existingLead.setState(updateData.getState());
        if (updateData.getTakeHomePay() != null) existingLead.setTakeHomePay(updateData.getTakeHomePay());
        if (updateData.getDeductionsOrEmisPayable() != null) existingLead.setDeductionsOrEmisPayable(updateData.getDeductionsOrEmisPayable());
        if (updateData.getBankName() != null) existingLead.setBankName(updateData.getBankName());
        if (updateData.getPrimaryBankAccount() != null) existingLead.setPrimaryBankAccount(updateData.getPrimaryBankAccount());
        if (updateData.getAccountStatementConsent() != null) existingLead.setAccountStatementConsent(updateData.getAccountStatementConsent());
        if (updateData.getCibilLiabilityCheck() != null) existingLead.setCibilLiabilityCheck(updateData.getCibilLiabilityCheck());
        if (updateData.getDebtToIncomeRatio() != null) existingLead.setDebtToIncomeRatio(updateData.getDebtToIncomeRatio());
        if (updateData.getLoanToValueRatio() != null) existingLead.setLoanToValueRatio(updateData.getLoanToValueRatio());
        if (updateData.getDebtServiceCoverageRatio() != null) existingLead.setDebtServiceCoverageRatio(updateData.getDebtServiceCoverageRatio());
        if (updateData.getNetDisposableIncome() != null) existingLead.setNetDisposableIncome(updateData.getNetDisposableIncome());

        // 4. Referral Details (Step 4)
        String channel = updateData.getLeadAcquisitionChannel() != null ? updateData.getLeadAcquisitionChannel() : updateData.getSourcingChannel();
        if (channel != null) {
            existingLead.setLeadAcquisitionChannel(channel);
        }

        String dateVal = updateData.getDate() != null ? updateData.getDate() : updateData.getReferralDate();
        if (dateVal != null) {
            existingLead.setDate(dateVal);
        }

        String partnerId = updateData.getSourcingAgentPartnerId() != null ? updateData.getSourcingAgentPartnerId() : updateData.getLspPartnerCode();
        if (partnerId != null) {
            existingLead.setSourcingAgentPartnerId(partnerId);
        }

        if (updateData.getAgentPartnerName() != null) {
            existingLead.setAgentPartnerName(updateData.getAgentPartnerName());
        }

        String empId = updateData.getEmployeeId() != null ? updateData.getEmployeeId() : updateData.getSourcingEmployeeId();
        if (empId != null) {
            existingLead.setEmployeeId(empId);
        }

        String empName = updateData.getEmployeeName() != null ? updateData.getEmployeeName() : updateData.getSourcingEmployeeName();
        if (empName != null) {
            existingLead.setEmployeeName(empName);
        }
    }
}
