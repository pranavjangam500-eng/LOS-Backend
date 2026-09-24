package com.bank.los.bank.master.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Bank / NBFC Organization Role entity.
 * Renamed from TenantRole -> OrganizationRole.
 */
@Data
@Entity
@Table(name = "roles", schema = "identity")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(nullable = false, length = 30)
    private String panel;

    @Column(length = 255)
    private String description;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
