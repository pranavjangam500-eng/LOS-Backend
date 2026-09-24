package com.bank.los.bank.master.repository;

import com.bank.los.bank.master.entity.SessionActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SessionActivityRepository extends JpaRepository<SessionActivity, String> {

    Optional<SessionActivity> findByJtiAndInvalidatedFalse(String jti);

    @Modifying
    @Query("UPDATE SessionActivity s SET s.invalidated = true WHERE s.userId = :userId AND s.invalidated = false")
    void invalidateAllForUser(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE SessionActivity s SET s.invalidated = true WHERE s.jti = :jti")
    void invalidateByJti(@Param("jti") String jti);
}
