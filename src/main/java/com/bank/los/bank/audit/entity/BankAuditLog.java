package com.bank.los.bank.audit.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "audit_logs", schema = "identity")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_code", length = 30)
    private String userCode;

    @Column(name = "role_name", length = 50)
    private String roleName;

    @Column(nullable = false, length = 80)
    private String action;

    @Column(length = 100)
    private String resource;

    @Column(length = 500)
    private String details;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
