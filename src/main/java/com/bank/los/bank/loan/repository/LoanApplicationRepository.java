package com.bank.los.bank.loan.repository;

import com.bank.los.bank.loan.entity.LoanApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {
    Optional<LoanApplication> findByApplicationNumber(String applicationNumber);
    Page<LoanApplication> findByCustomerId(Long customerId, Pageable pageable);
    Page<LoanApplication> findByStatus(String status, Pageable pageable);
    Page<LoanApplication> findByBranchId(Long branchId, Pageable pageable);
}
