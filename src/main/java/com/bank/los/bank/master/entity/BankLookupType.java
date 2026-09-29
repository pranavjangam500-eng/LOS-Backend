package com.bank.los.bank.master.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Lookup Type Master (Table 51001) - Bank Database
 */
@Entity
@Table(name = "lookup_types", schema = "identity")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankLookupType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "description", nullable = false, length = 255)
    private String description;

    @Builder.Default
    @Column(name = "is_fixed", nullable = false)
    private Boolean isFixed = false;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Builder.Default
    @Column(name = "can_view", nullable = false)
    private Boolean canView = true;

    @Builder.Default
    @Column(name = "can_add", nullable = false)
    private Boolean canAdd = true;

    @Builder.Default
    @Column(name = "can_import_from_master", nullable = false)
    private Boolean canImportFromMaster = true;

    @Builder.Default
    @Column(name = "can_edit", nullable = false)
    private Boolean canEdit = true;

    @Builder.Default
    @Column(name = "can_delete", nullable = false)
    private Boolean canDelete = true;

    @Builder.Default
    @Column(name = "can_activate", nullable = false)
    private Boolean canActivate = true;

    @Builder.Default
    @Column(name = "can_deactivate", nullable = false)
    private Boolean canDeactivate = true;

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

    @Builder.Default
    @OneToMany(mappedBy = "lookupType", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC, id ASC")
    private List<BankLookupSubType> subTypes = new ArrayList<>();
}
