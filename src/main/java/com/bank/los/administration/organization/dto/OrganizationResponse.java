package com.bank.los.administration.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response representing an onboarded Bank or NBFC Organization")
public class OrganizationResponse {

    @Schema(description = "Organization primary key ID", example = "1")
    private Long id;

    @Schema(description = "Institution name", example = "HDFC Bank")
    private String name;

    @Schema(description = "Unique short code", example = "HDFC01")
    private String code;

    @Schema(description = "Entity type", example = "BANK")
    private String type;

    @Schema(description = "Operational status", example = "ACTIVE")
    private String status;

    @Schema(description = "Contact email", example = "contact@hdfcbank.com")
    private String contactEmail;

    @Schema(description = "Contact phone", example = "+912261606161")
    private String contactPhone;

    @Schema(description = "Database name", example = "los_hdfc01_db")
    private String dbName;

    @Schema(description = "Database host", example = "localhost")
    private String dbHost;

    @Schema(description = "Database port", example = "5432")
    private Integer dbPort;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;
}
