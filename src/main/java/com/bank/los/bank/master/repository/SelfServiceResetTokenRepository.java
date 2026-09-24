package com.bank.los.bank.master.repository;

import com.bank.los.bank.master.entity.SelfServiceResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface SelfServiceResetTokenRepository extends JpaRepository<SelfServiceResetToken, Long> {

    Optional<SelfServiceResetToken> findByTokenHashAndUsedFalseAndExpiresAtAfter(String tokenHash, LocalDateTime now);

    @Modifying
    @Query("UPDATE SelfServiceResetToken s SET s.used = true WHERE s.empNo = :empNo AND s.used = false")
    void invalidateAllForEmpNo(@Param("empNo") String empNo);

    @Query("SELECT COUNT(s) FROM SelfServiceResetToken s WHERE s.empNo = :empNo AND s.createdAt >= :since")
    long countRecentByEmpNo(@Param("empNo") String empNo, @Param("since") LocalDateTime since);
}
