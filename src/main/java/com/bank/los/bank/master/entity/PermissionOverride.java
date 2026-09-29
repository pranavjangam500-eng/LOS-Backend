package com.bank.los.bank.master.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Bank Permission Customization Override Entity - Tenant Database
 * Stores explicit ALLOW / DENY permission overrides for specific Roles or Designations.
 * E.g., General Manager (Designation) -> LOOKUP_BANK_DELETE -> DENY
 * Keeps the master permission and base role intact while enforcing custom bank security rules.
 */
@Entity
@Table(name = "permission_overrides", schema = "identity",
       uniqueConstraints = @UniqueConstraint(name = "uk_permission_override", columnNames = {"target_type", "target_name", "permission_code"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionOverride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Target type: 'ROLE' or 'DESIGNATION'
     */
    @Column(name = "target_type", nullable = false, length = 30)
    private String targetType;

    /**
     * Target name: e.g. "ADMIN", "MAKER", "General Manager", "Branch Manager"
     */
    @Column(name = "target_name", nullable = false, length = 100)
    private String targetName;

    /**
     * Permission code: e.g. "LOOKUP_BANK_DELETE", "LOOKUP_BANK_ADD"
     */
    @Column(name = "permission_code", nullable = false, length = 100)
    private String permissionCode;

    /**
     * Effect: 'ALLOW' or 'DENY'
     */
    @Column(name = "effect", nullable = false, length = 10)
    private String effect;

    @Column(name = "reason", length = 255)
    private String reason;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_by")
    private Long createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "modified_by")
    private Long modifiedBy;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
