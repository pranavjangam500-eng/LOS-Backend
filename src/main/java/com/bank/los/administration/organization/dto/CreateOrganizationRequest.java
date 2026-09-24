package com.bank.los.administration.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for onboarding a new Bank or NBFC Organization")
public class CreateOrganizationRequest {

    @NotBlank(message = "Organization name is required")
    @Size(max = 150, message = "Name must not exceed 150 characters")
    @Schema(description = "Legal business name of the financial institution", example = "HDFC Bank")
    private String name;

    @NotBlank(message = "Organization code is required")
    @Size(min = 3, max = 20, message = "Code must be between 3 and 20 characters")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "Code must be alphanumeric and may contain hyphens/underscores")
    @Schema(description = "Unique short code for the bank/NBFC (used in user codes and routing)", example = "HDFC01")
    private String code;

    @NotBlank(message = "Type is required")
    @Pattern(regexp = "^(BANK|NBFC)$", message = "Type must be 'BANK' or 'NBFC'")
    @Schema(description = "Type of financial entity", example = "BANK", allowableValues = {"BANK", "NBFC"})
    private String type;

    @Schema(description = "Primary contact email address", example = "contact@hdfcbank.com")
    private String contactEmail;

    @Schema(description = "Primary contact telephone number", example = "+912261606161")
    private String contactPhone;

    @NotBlank(message = "Database name is required")
    @Size(max = 100, message = "Database name must not exceed 100 characters")
    @Schema(description = "Dedicated PostgreSQL database name allocated for this organization", example = "los_hdfc01_db")
    private String dbName;

    @Schema(description = "Database host", example = "localhost", defaultValue = "localhost")
    @Builder.Default
    private String dbHost = "localhost";

    @Schema(description = "Database port", example = "5432", defaultValue = "5432")
    @Builder.Default
    private Integer dbPort = 5432;
}
