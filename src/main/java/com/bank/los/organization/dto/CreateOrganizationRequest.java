package com.bank.los.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrganizationRequest {

    @NotBlank(message = "Organization name is required")
    @Schema(description = "Full organization name", example = "HDFC Bank")
    private String name;

    @NotBlank(message = "Organization code is required")
    @Schema(description = "Unique alphanumeric code", example = "HDFC01")
    private String code;

    @NotBlank(message = "Type is required")
    @Pattern(regexp = "BANK|NBFC", message = "Type must be either BANK or NBFC")
    @Schema(description = "Type: BANK or NBFC", example = "BANK")
    private String type;

    @Schema(description = "Contact email", example = "admin@hdfcbank.com")
    private String contactEmail;

    @Schema(description = "Contact phone", example = "+919876543210")
    private String contactPhone;

    @NotBlank(message = "Database name is required")
    @Schema(description = "Dedicated PostgreSQL database name", example = "los_hdfc01_db")
    private String dbName;

    @Schema(description = "Database host", example = "localhost")
    @Builder.Default
    private String dbHost = "localhost";

    @Schema(description = "Database port", example = "5432")
    @Builder.Default
    private Integer dbPort = 5432;
}
