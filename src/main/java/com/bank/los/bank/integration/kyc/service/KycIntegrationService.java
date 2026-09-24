package com.bank.los.bank.integration.kyc.service;

import com.bank.los.bank.integration.kyc.dto.KycVerificationRequest;
import com.bank.los.bank.integration.kyc.dto.KycVerificationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class KycIntegrationService {

    public KycVerificationResponse verifyDocument(KycVerificationRequest request) {
        log.info("Processing KYC verification for documentType={}, documentNumber={}",
                request.getDocumentType(), maskDocumentNumber(request.getDocumentNumber()));

        // Simulated integration logic with third-party KYC provider (e.g. NSDL / Karza / UIDAI)
        boolean isValid = request.getDocumentNumber() != null && !request.getDocumentNumber().trim().isEmpty();

        return KycVerificationResponse.builder()
                .verificationId(UUID.randomUUID().toString())
                .documentType(request.getDocumentType())
                .documentNumber(maskDocumentNumber(request.getDocumentNumber()))
                .status(isValid ? "VERIFIED" : "REJECTED")
                .nameMatchConfidence("98.5%")
                .verifiedAt(LocalDateTime.now())
                .build();
    }

    private String maskDocumentNumber(String docNum) {
        if (docNum == null || docNum.length() <= 4) {
            return "****";
        }
        return "X".repeat(docNum.length() - 4) + docNum.substring(docNum.length() - 4);
    }
}
