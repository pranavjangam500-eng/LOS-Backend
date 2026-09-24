package com.bank.los.bank.integration.kyc.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "KYC document verification request")
public class KycVerificationRequest {

    @NotBlank(message = "Document type is required (PAN, AADHAAR, PASSPORT, VOTER_ID)")
    private String documentType;

    @NotBlank(message = "Document number is required")
    private String documentNumber;

    private String fullName;
    private String dob;
}
