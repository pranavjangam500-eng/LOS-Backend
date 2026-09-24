package com.bank.los.bank.loan.entity;

import com.bank.los.common.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "loan_applications", schema = "customer")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LoanApplication extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "application_number", unique = true, length = 40)
    private String applicationNumber;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    @Column(name = "product_code", nullable = false, length = 50)
    private String productCode;

    @Column(name = "applied_amount", nullable = false)
    private Double appliedAmount;

    @Column(name = "sanctioned_amount")
    private Double sanctionedAmount;

    @Column(name = "interest_rate")
    private Double interestRate;

    @Column(name = "tenure_months", nullable = false)
    private Integer tenureMonths;

    @Column(name = "cibil_score")
    private Integer cibilScore;

    @Column(name = "kyc_status", length = 30)
    @Builder.Default
    private String kycStatus = "PENDING";

    @Column(name = "maker_user_id")
    private Long makerUserId;

    @Column(name = "checker_user_id")
    private Long checkerUserId;

    @Column(nullable = false, length = 40)
    @Builder.Default
    private String status = "DRAFT"; // DRAFT, SUBMITTED_TO_CHECKER, APPROVED, REJECTED, DISBURSED

    @Column(name = "checker_remarks", length = 500)
    private String checkerRemarks;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
}
