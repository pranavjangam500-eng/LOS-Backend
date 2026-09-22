package com.bank.los.tenant.repository;

import com.bank.los.tenant.entity.SelfServiceResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface SelfServiceResetTokenRepository extends JpaRepository<SelfServiceResetToken, Long> {

    Optional<SelfServiceResetToken> findByTokenHashAndUsedFalseAndExpiresAtAfter(
            String tokenHash, LocalDateTime now);

    /** Count recent requests for rate-limiting (max 3/hr) */
    @Query("SELECT COUNT(t) FROM SelfServiceResetToken t WHERE t.empNo = :empNo AND t.createdAt > :since")
    long countRecentByEmpNo(String empNo, LocalDateTime since);

    /** Invalidate all prior unused tokens for this empNo before issuing new one */
    @Modifying
    @Transactional
    @Query("UPDATE SelfServiceResetToken t SET t.used = true WHERE t.empNo = :empNo AND t.used = false")
    void invalidateAllForEmpNo(String empNo);
}
