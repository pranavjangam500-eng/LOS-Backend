package com.bank.los.otp.model;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory representation of an active, cryptographically hashed OTP record.
 */
public record OtpRecord(
    String email,
    byte[] hash,
    Instant expiresAt,
    Instant lastSentAt,
    AtomicInteger attempts
) {
    public OtpRecord(byte[] hash, Instant expiresAt, Instant lastSentAt, AtomicInteger attempts) {
        this(null, hash, expiresAt, lastSentAt, attempts);
    }
}
