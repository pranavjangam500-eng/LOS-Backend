package com.bank.los.bank.integration.lms.service;

import com.bank.los.bank.integration.lms.dto.LmsDisbursalSyncRequest;
import com.bank.los.bank.integration.lms.dto.LmsDisbursalSyncResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class LmsIntegrationService {

    public LmsDisbursalSyncResponse syncDisbursal(LmsDisbursalSyncRequest request) {
        log.info("Synchronizing loan with Core Banking / LMS for applicationNumber={}", request.getApplicationNumber());

        String lmsLoanAccNo = "LMS-" + System.currentTimeMillis();

        return LmsDisbursalSyncResponse.builder()
                .lmsLoanAccountNumber(lmsLoanAccNo)
                .applicationNumber(request.getApplicationNumber())
                .syncStatus("SYNCED")
                .disbursedAmount(request.getApprovedAmount())
                .synchronizedAt(LocalDateTime.now())
                .remarks("Loan booked and handed off to LMS successfully")
                .build();
    }
}
