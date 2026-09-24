package com.bank.los.bank.master.repository;

import com.bank.los.bank.master.entity.UserPasswordReset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPasswordResetRepository extends JpaRepository<UserPasswordReset, Long> {
    Optional<UserPasswordReset> findTopByEmpNoAndAppliedFalseOrderByCreatedAtDesc(String empNo);
}
