package com.bank.los.bank.master.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Lookup Sub Type Master (Table 51101) - Bank Database
 */
@Entity
@Table(name = "lookup_sub_types", schema = "identity",
       uniqueConstraints = @UniqueConstraint(name = "uk_bank_lookup_sub_type", columnNames = {"lookup_type_code", "sub_type_code"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankLookupSubType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lookup_type_code", nullable = false, length = 50)
    private String lookupTypeCode;

    @Column(name = "type_description", length = 255)
    private String typeDescription;

    @Column(name = "sub_type_code", nullable = false, length = 50)
    private String subTypeCode;

    @Column(name = "sub_type_description", nullable = false, length = 255)
    private String subTypeDescription;

    @Builder.Default
    @Column(name = "is_fixed", nullable = false)
    private Boolean isFixed = false;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Builder.Default
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lookup_type_code", referencedColumnName = "code", insertable = false, updatable = false)
    private BankLookupType lookupType;
}
