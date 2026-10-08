package com.bank.los.otp.repository;

import com.bank.los.otp.model.OtpRecord;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe in-memory OTP repository backed by ConcurrentHashMap.
 * Provides high-throughput, zero database latency, and instant record invalidation.
 */
@Repository("inMemoryOtpRepository")
public class InMemoryOtpRepository implements OtpRepository {

    private final ConcurrentHashMap<String, OtpRecord> store = new ConcurrentHashMap<>();

    @Override
    public void save(String email, OtpRecord record) {
        store.put(email, record);
    }

    @Override
    public Optional<OtpRecord> findByEmail(String email) {
        return Optional.ofNullable(store.get(email));
    }

    @Override
    public void deleteByEmail(String email) {
        store.remove(email);
    }

    @Override
    public void removeExpired(Instant now) {
        store.entrySet().removeIf(entry -> now.isAfter(entry.getValue().expiresAt()));
    }

    @Override
    public void clearAll() {
        store.clear();
    }
}
