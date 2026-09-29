package com.bank.los.administration.master.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Dynamic lookup-level permissions mapping in Master Database.
 */
@Entity
@Table(name = "master_lookup_type_permissions", schema = "identity",
        uniqueConstraints = @UniqueConstraint(name = "uk_master_lt_permission", columnNames = {"lookup_type_code", "permission_code"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasterLookupTypePermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lookup_type_code", nullable = false, length = 50)
    private String lookupTypeCode;

    @Column(name = "permission_code", nullable = false, length = 50)
    private String permissionCode;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
