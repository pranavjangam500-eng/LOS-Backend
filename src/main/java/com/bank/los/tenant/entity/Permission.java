package com.bank.los.tenant.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Permission lookup code — DB-backed so ADMIN can configure per role at runtime. */
@Data
@Entity
@Table(name = "permissions", schema = "identity")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** e.g. LOAN_APPLICATION_CREATE */
    @Column(name = "code", nullable = false, unique = true, length = 100)
    private String code;

    @Column(name = "description", length = 255)
    private String description;

    /** Module grouping: LOAN, USER, BRANCH, DOCUMENT, REPORT, DASHBOARD, SYSTEM */
    @Column(name = "module", length = 60)
    private String module;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
