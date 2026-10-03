package com.bank.los.administration.organization.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for onboarding a new Bank or NBFC Organization")
public class CreateOrganizationRequest {

    @Schema(description = "Optional custom UUID (auto-generated if omitted)", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
    private UUID id;

    @JsonProperty("bank_code")
    @JsonAlias({"bankCode", "code"})
    @Schema(description = "Optional bank code (auto-generated if omitted)", example = "HDFC01")
    private String bankCode;

    @NotBlank(message = "bank_name is required")
    @Size(max = 150, message = "bank_name must not exceed 150 characters")
    @JsonProperty("bank_name")
    @JsonAlias({"bankName", "name"})
    @Schema(description = "Display name of the bank", example = "HDFC Bank")
    private String bankName;

    @NotBlank(message = "legal_name is required")
    @Size(max = 200, message = "legal_name must not exceed 200 characters")
    @JsonProperty("legal_name")
    @JsonAlias({"legalName"})
    @Schema(description = "Registered legal entity name", example = "HDFC Bank Limited")
    private String legalName;

    @NotBlank(message = "bank_type is required")
    @JsonProperty("bank_type")
    @JsonAlias({"bankType", "type"})
    @Schema(description = "Type of financial institution / bank", example = "COMMERCIAL_BANK", allowableValues = {"BANK", "NBFC", "COMMERCIAL_BANK", "SMALL_FINANCE_BANK", "PAYMENT_BANK", "COOPERATIVE_BANK", "HOUSING_FINANCE"})
    private String bankType;

    @NotBlank(message = "registration_number is required")
    @JsonProperty("registration_number")
    @JsonAlias({"registrationNumber"})
    @Schema(description = "Company registration / incorporation certificate number", example = "REG-MH-2024-8899")
    private String registrationNumber;

    @NotBlank(message = "PAN is required")
    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$", message = "PAN must be a valid 10-character Indian PAN format (e.g. ABCDE1234F)")
    @JsonProperty("PAN")
    @JsonAlias({"pan", "Pan"})
    @Schema(description = "10-digit Permanent Account Number", example = "AAACH1234F")
    private String pan;

    @NotBlank(message = "CIN is required")
    @JsonProperty("CIN")
    @JsonAlias({"cin", "Cin"})
    @Schema(description = "Corporate Identification Number (CIN)", example = "L65920MH1994PLC080618")
    private String cin;

    @JsonProperty("website")
    @Schema(description = "Official website URL", example = "https://www.hdfcbank.com")
    private String website;

    @JsonProperty("logo")
    @Schema(description = "Logo image URL or base64 asset identifier", example = "https://assets.bank.com/logos/hdfc.png")
    private String logo;

    @NotNull(message = "regulatory_authority_id is required")
    @JsonProperty("regulatory_authority_id")
    @JsonAlias({"regulatoryAuthorityId"})
    @Schema(description = "UUID of the governing regulatory authority (e.g., RBI UUID)", example = "b5a76e2d-3c9f-4321-9e87-654321fedcba")
    private UUID regulatoryAuthorityId;

    @NotBlank(message = "regulatory_status is required")
    @JsonProperty("regulatory_status")
    @JsonAlias({"regulatoryStatus"})
    @Schema(description = "Regulatory compliance status", example = "ACTIVE", allowableValues = {"ACTIVE", "LICENSED", "REGULATED", "PENDING_APPROVAL", "SUSPENDED"})
    private String regulatoryStatus;

    @NotBlank(message = "country is required")
    @JsonProperty("country")
    @Schema(description = "Country of jurisdiction / operation", example = "India")
    private String country;

    @JsonProperty("contact_email")
    @JsonAlias({"contactEmail", "email"})
    @Schema(description = "Primary contact email address", example = "compliance@hdfcbank.com")
    private String contactEmail;

    @JsonProperty("contact_phone")
    @JsonAlias({"contactPhone", "phone"})
    @Schema(description = "Primary contact telephone number", example = "+912261606161")
    private String contactPhone;

    @JsonProperty("db_name")
    @JsonAlias({"dbName"})
    @Size(max = 100, message = "Database name must not exceed 100 characters")
    @Schema(description = "Dedicated PostgreSQL database name allocated for this organization (optional, auto-generated if omitted)", example = "los_hdfc01_db")
    private String dbName;

    @JsonProperty("db_host")
    @JsonAlias({"dbHost"})
    @Schema(description = "Database host", example = "localhost", defaultValue = "localhost")
    @Builder.Default
    private String dbHost = "localhost";

    @JsonProperty("db_port")
    @JsonAlias({"dbPort"})
    @Schema(description = "Database port", example = "5432", defaultValue = "5432")
    @Builder.Default
    private Integer dbPort = 5432;

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankType() {
        return bankType;
    }

    public void setBankType(String bankType) {
        this.bankType = bankType;
    }

    // Helper getters
    public String getName() {
        return bankName != null ? bankName : legalName;
    }

    public String getType() {
        return bankType;
    }
}

