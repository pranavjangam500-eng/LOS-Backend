package com.bank.los.tenant.repository;

import com.bank.los.tenant.entity.UserPasswordReset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPasswordResetRepository extends JpaRepository<UserPasswordReset, Long> {

    /** Pending resets awaiting verification for this employee */
    List<UserPasswordReset> findByEmpNoAndVerifiedByIsNullAndAppliedFalse(String empNo);

    /** Get the most recent unapplied reset created for verification */
    Optional<UserPasswordReset> findTopByEmpNoAndAppliedFalseOrderByCreatedAtDesc(String empNo);
}
