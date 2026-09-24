package com.bank.los.bank.integration.bureau.service;

import com.bank.los.bank.integration.bureau.dto.BureauCheckRequest;
import com.bank.los.bank.integration.bureau.dto.BureauCheckResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
public class CreditBureauIntegrationService {

    private final Random random = new Random();

    public BureauCheckResponse checkCreditScore(BureauCheckRequest request) {
        String bureau = request.getBureauType() != null ? request.getBureauType().toUpperCase() : "CIBIL";
        log.info("Initiating credit bureau check for bureau={}, customer={}", bureau, request.getCustomerName());

        // Simulated credit bureau pull (e.g. CIBIL score between 650 and 850)
        int score = 650 + random.nextInt(200);
        String riskCategory;
        if (score >= 750) {
            riskCategory = "LOW_RISK";
        } else if (score >= 680) {
            riskCategory = "MEDIUM_RISK";
        } else {
            riskCategory = "HIGH_RISK";
        }

        return BureauCheckResponse.builder()
                .reportId("BUREAU-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .bureauType(bureau)
                .panNumber(maskPan(request.getPanNumber()))
                .creditScore(score)
                .riskCategory(riskCategory)
                .totalActiveLoans(random.nextInt(4))
                .defaultAccountsCount(0)
                .reportGeneratedAt(LocalDateTime.now())
                .build();
    }

    private String maskPan(String pan) {
        if (pan == null || pan.length() <= 4) {
            return "****";
        }
        return pan.substring(0, 2) + "XXXXX" + pan.substring(pan.length() - 2);
    }
}
