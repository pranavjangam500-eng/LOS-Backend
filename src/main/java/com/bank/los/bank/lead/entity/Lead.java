package com.bank.los.bank.lead.entity;

import com.bank.los.bank.lead.dto.LeadRequest;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Lead entity model stored in the database (table: customer.leads).
 * Extends LeadRequest to inherit all 4-section multi-step form fields, annotations,
 * and getters/setters without duplicating field declarations:
 * 1. Personal Details
 * 2. Loan Details
 * 3. Income Profile
 * 4. Referral Details
 */
@Entity
@Table(name = "leads", schema = "customer")
public class Lead extends LeadRequest {

    public Lead() {
        super();
    }

    public Lead(String leadId, String sourcingChannel, String lspPartnerCode, String userCategory,
                String firstNameBusinessName, String lastName, String emailAddress) {
        super(leadId, sourcingChannel, lspPartnerCode, userCategory, firstNameBusinessName, lastName, emailAddress);
    }

    public Lead(LeadRequest request) {
        super(request);
    }

    @jakarta.persistence.PrePersist
    public void prePersist() {
        if (getCreatedAt() == null) {
            setCreatedAt(java.time.LocalDateTime.now());
        }
        if (getUpdatedAt() == null) {
            setUpdatedAt(java.time.LocalDateTime.now());
        }
    }

    @jakarta.persistence.PreUpdate
    public void preUpdate() {
        setUpdatedAt(java.time.LocalDateTime.now());
    }

    @Override
    public String toString() {
        return "Lead{" +
                "leadId='" + getLeadId() + '\'' +
                ", customerName='" + getFirstNameBusinessName() + '\'' +
                ", mobileNumber='" + getMobileNumber() + '\'' +
                ", panNumber='" + getPanNumber() + '\'' +
                ", loanProductType='" + getLoanProductType() + '\'' +
                ", loanAmount=" + getLoanAmount() +
                ", leadAcquisitionChannel='" + getLeadAcquisitionChannel() + '\'' +
                ", date='" + getDate() + '\'' +
                ", sourcingAgentPartnerId='" + getSourcingAgentPartnerId() + '\'' +
                ", agentPartnerName='" + getAgentPartnerName() + '\'' +
                ", employeeId='" + getEmployeeId() + '\'' +
                ", employeeName='" + getEmployeeName() + '\'' +
                ", leadStatus='" + getLeadStatus() + '\'' +
                ", assignedEmployeeId='" + getAssignedEmployeeId() + '\'' +
                '}';
    }
}
