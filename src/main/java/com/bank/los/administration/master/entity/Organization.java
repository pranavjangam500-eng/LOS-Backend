package com.bank.los.administration.master.entity;

import com.bank.los.common.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

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

    @Column(name = "uuid", unique = true)
    @Builder.Default
    private UUID uuid = UUID.randomUUID();

    @Column(name = "institution_code", nullable = false, unique = true, length = 50)
    private String institutionCode;

    @Column(name = "institution_name", nullable = false, length = 150)
    private String institutionName;

    @Column(name = "legal_name", length = 200)
    private String legalName;

    @Column(name = "short_name", length = 50)
    private String shortName;

    @Column(name = "institution_type", nullable = false, length = 50)
    private String institutionType; // 'BANK', 'NBFC', 'COMMERCIAL_BANK', etc.

    @Column(name = "registration_number", length = 100)
    private String registrationNumber;

    @Column(name = "pan", length = 20)
    private String pan;

    @Column(name = "cin", length = 50)
    private String cin;

    @Column(name = "website", length = 255)
    private String website;

    @Column(name = "logo", columnDefinition = "TEXT")
    private String logo;

    @Column(name = "regulatory_authority_id")
    private UUID regulatoryAuthorityId;

    @Column(name = "regulatory_status", length = 50)
    @Builder.Default
    private String regulatoryStatus = "ACTIVE"; // 'ACTIVE', 'LICENSED', 'REGULATED', 'SUSPENDED'

    @Column(name = "country", length = 100)
    @Builder.Default
    private String country = "India";

    // Regulatory details
    @Column(name = "direct_clearing_member")
    private Boolean directClearingMember;

    @Column(name = "direct_member_iftas")
    private Boolean directMemberIftas;

    @Column(name = "micr_city_code", length = 3)
    private String micrCityCode;

    @Column(name = "micr_bank_code", length = 3)
    private String micrBankCode;

    @Column(name = "micr_branch_code", length = 3)
    private String micrBranchCode;

    @Column(name = "ifsc_code", length = 11)
    private String ifscCode;

    @Column(name = "number_of_branches")
    private Integer numberOfBranches;

    @Column(name = "sponsor_bank_for_clearing", length = 150)
    private String sponsorBankForClearing;

    @Column(name = "sponsor_bank_for_iftas", length = 150)
    private String sponsorBankForIftas;

    // Address details
    @Column(name = "address_type", length = 50)
    private String addressType;

    @Column(name = "unit_gala_name_number", length = 200)
    private String unitGalaNameNumber;

    @Column(name = "street_road", length = 200)
    private String streetRoad;

    @Column(name = "landmark", length = 150)
    private String landmark;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "pincode", length = 6)
    private String pincode;

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

    // Helper compatibility getters / setters
    public String getCode() {
        return institutionCode != null ? institutionCode : "";
    }

    public void setCode(String code) {
        this.institutionCode = code;
    }

    public String getName() {
        return institutionName != null ? institutionName : (legalName != null ? legalName : "");
    }

    public void setName(String name) {
        this.institutionName = name;
    }

    public String getType() {
        return institutionType != null ? institutionType : "BANK";
    }

    public void setType(String type) {
        this.institutionType = type;
    }

    public static class OrganizationBuilder {
        public OrganizationBuilder code(String code) {
            this.institutionCode = code;
            return this;
        }

        public OrganizationBuilder name(String name) {
            this.institutionName = name;
            return this;
        }

        public OrganizationBuilder type(String type) {
            this.institutionType = type;
            return this;
        }
    }
}


