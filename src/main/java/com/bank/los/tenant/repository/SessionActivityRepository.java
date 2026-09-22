package com.bank.los.tenant.repository;

import com.bank.los.tenant.entity.SessionActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SessionActivityRepository extends JpaRepository<SessionActivity, String> {

    /** Invalidate all active sessions for a user (e.g. on password change, deactivation) */
    @Modifying
    @Transactional
    @Query("UPDATE SessionActivity s SET s.invalidated = true WHERE s.userId = :userId AND s.invalidated = false")
    void invalidateAllForUser(Long userId);

    /** Scheduled cleanup: find sessions that have been idle longer than their timeout */
    @Query("SELECT s FROM SessionActivity s WHERE s.invalidated = false AND s.lastSeen < :threshold")
    List<SessionActivity> findIdleSessions(LocalDateTime threshold);
}
