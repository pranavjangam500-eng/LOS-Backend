package com.bank.los.bank.master.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Designation to Role Mapping Entity - Tenant Database
 * Maps a Bank Designation (e.g. "General Manager", "Manager", "Clerk")
 * to a base Role (e.g. ADMIN, MAKER, CHECKER), inheriting the role's permissions.
 */
@Entity
@Table(name = "designation_role_mappings", schema = "identity",
       uniqueConstraints = @UniqueConstraint(name = "uk_designation_mapping", columnNames = {"designation"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DesignationRoleMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "designation", nullable = false, unique = true, length = 100)
    private String designation;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private OrganizationRole role;

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
