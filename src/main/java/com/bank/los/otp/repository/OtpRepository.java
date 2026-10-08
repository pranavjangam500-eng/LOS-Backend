package com.bank.los.otp.repository;

import com.bank.los.otp.model.OtpRecord;

import java.time.Instant;
import java.util.Optional;

/**
 * Storage abstraction for OTP records.
 */
public interface OtpRepository {

    void save(String email, OtpRecord record);

    Optional<OtpRecord> findByEmail(String email);

    void deleteByEmail(String email);

    void removeExpired(Instant now);

    void clearAll();
}
