package com.bank.los.bank.integration.kyc.dto;

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
@Schema(description = "KYC Verification Response")
public class KycVerificationResponse {
    private String verificationId;
    private String documentType;
    private String documentNumber;
    private String status; // VERIFIED, REJECTED, MANUAL_REVIEW
    private String nameMatchConfidence;
    private LocalDateTime verifiedAt;
}
