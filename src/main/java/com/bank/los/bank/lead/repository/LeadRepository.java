package com.bank.los.bank.lead.repository;

import com.bank.los.bank.lead.entity.Lead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {
    Optional<Lead> findByLeadNumber(String leadNumber);
    Page<Lead> findByStatus(String status, Pageable pageable);
    Page<Lead> findByBranchId(Long branchId, Pageable pageable);
}
