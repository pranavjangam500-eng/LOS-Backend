package com.bank.los.bank.lead.entity;

import com.bank.los.common.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "leads", schema = "customer")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Lead extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lead_number", unique = true, length = 40)
    private String leadNumber;

    @Column(name = "customer_name", nullable = false, length = 120)
    private String customerName;

    @Column(length = 150)
    private String email;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(name = "loan_product_type", length = 50)
    private String loanProductType;

    @Column(name = "requested_amount")
    private Double requestedAmount;

    @Column(name = "assigned_to_user_id")
    private Long assignedToUserId;

    @Column(name = "branch_id")
    private Long branchId;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String status = "NEW"; // NEW, CONTACTED, QUALIFIED, CONVERTED, DROPPED

    @Column(length = 500)
    private String remarks;
}
