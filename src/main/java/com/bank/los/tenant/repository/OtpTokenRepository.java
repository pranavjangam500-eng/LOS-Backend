package com.bank.los.tenant.repository;

import com.bank.los.tenant.entity.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    /** Get the latest valid (unused, not-expired) OTP for a user */
    Optional<OtpToken> findTopByUserIdAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
            Long userId, LocalDateTime now);

    /** Void all existing OTPs for a user before issuing a new one */
    @Modifying
    @Transactional
    @Query("UPDATE OtpToken o SET o.used = true WHERE o.userId = :userId AND o.used = false")
    void invalidateAllForUser(Long userId);
}
