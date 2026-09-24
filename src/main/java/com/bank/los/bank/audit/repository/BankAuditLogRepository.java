package com.bank.los.bank.audit.repository;

import com.bank.los.bank.audit.entity.BankAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BankAuditLogRepository extends JpaRepository<BankAuditLog, Long> {
    Page<BankAuditLog> findByUserId(Long userId, Pageable pageable);
    Page<BankAuditLog> findByAction(String action, Pageable pageable);
}
