package com.bank.los.master.entity;

import com.bank.los.common.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "organizations", schema = "organization")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Organization extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 20)
    private String type; // 'BANK' or 'NBFC'

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE"; // 'ACTIVE', 'INACTIVE', 'SUSPENDED'

    @Column(name = "contact_email", length = 150)
    private String contactEmail;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "db_name", nullable = false, unique = true, length = 100)
    private String dbName;

    @Column(name = "db_host", nullable = false, length = 150)
    @Builder.Default
    private String dbHost = "localhost";

    @Column(name = "db_port", nullable = false)
    @Builder.Default
    private Integer dbPort = 5432;
}
