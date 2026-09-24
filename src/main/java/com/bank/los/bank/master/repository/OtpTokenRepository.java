package com.bank.los.bank.master.repository;

import com.bank.los.bank.master.entity.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {
    Optional<OtpToken> findTopByUserIdAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(Long userId, LocalDateTime now);

    @Modifying
    @Query("UPDATE OtpToken o SET o.used = true WHERE o.userId = :userId AND o.used = false")
    void invalidateAllForUser(@Param("userId") Long userId);
}
