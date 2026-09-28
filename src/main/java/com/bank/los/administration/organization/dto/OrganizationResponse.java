package com.bank.los.administration.organization.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response representing an onboarded Bank or NBFC Organization")
public class OrganizationResponse {

    @JsonProperty("id")
    @Schema(description = "Institution UUID", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
    private UUID id;

    @JsonProperty("pkid")
    @Schema(description = "Internal sequential ID", example = "1")
    private Long pkid;

    @JsonProperty("institution_name")
    @Schema(description = "Institution Name", example = "HDFC Bank")
    private String institutionName;

    @JsonProperty("legal_name")
    @Schema(description = "Legal Business Name", example = "HDFC Bank Limited")
    private String legalName;

    @JsonProperty("institution_type")
    @Schema(description = "Institution Type", example = "COMMERCIAL_BANK")
    private String institutionType;

    @JsonProperty("registration_number")
    @Schema(description = "Registration Number", example = "REG-MH-2024-8899")
    private String registrationNumber;

    @JsonProperty("PAN")
    @Schema(description = "PAN", example = "AAACH1234F")
    private String pan;

    @JsonProperty("CIN")
    @Schema(description = "CIN", example = "L65920MH1994PLC080618")
    private String cin;

    @JsonProperty("website")
    @Schema(description = "Website URL", example = "https://www.hdfcbank.com")
    private String website;

    @JsonProperty("logo")
    @Schema(description = "Logo URL or base64 asset identifier", example = "https://assets.bank.com/logos/hdfc.png")
    private String logo;

    @JsonProperty("regulatory_authority_id")
    @Schema(description = "Regulatory Authority UUID", example = "b5a76e2d-3c9f-4321-9e87-654321fedcba")
    private UUID regulatoryAuthorityId;

    @JsonProperty("regulatory_status")
    @Schema(description = "Regulatory Status", example = "ACTIVE")
    private String regulatoryStatus;

    @JsonProperty("country")
    @Schema(description = "Country", example = "India")
    private String country;

    @JsonProperty("status")
    @Schema(description = "Operational Status", example = "ACTIVE")
    private String status;

    @JsonProperty("contact_email")
    @Schema(description = "Contact Email", example = "compliance@hdfcbank.com")
    private String contactEmail;

    @JsonProperty("contact_phone")
    @Schema(description = "Contact Phone", example = "+912261606161")
    private String contactPhone;

    @JsonProperty("db_name")
    @Schema(description = "Assigned Database Name", example = "los_hdfc01_db")
    private String dbName;

    @JsonProperty("db_host")
    @Schema(description = "Database Host", example = "localhost")
    private String dbHost;

    @JsonProperty("db_port")
    @Schema(description = "Database Port", example = "5432")
    private Integer dbPort;

    @JsonProperty("created_at")
    @Schema(description = "Creation Timestamp")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    @Schema(description = "Last Updated Timestamp")
    private LocalDateTime updatedAt;

    // Helper getters
    public String getName() {
        return institutionName != null ? institutionName : legalName;
    }

    public String getType() {
        return institutionType;
    }
}

