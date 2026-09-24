package com.bank.los.bank.master.repository;

import com.bank.los.bank.master.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByCustomerCode(String customerCode);
    Optional<Customer> findByPhone(String phone);
    boolean existsByEmail(String email);
    boolean existsByCustomerCode(String customerCode);
    boolean existsByPhone(String phone);
    Page<Customer> findByBranchId(Long branchId, Pageable pageable);
    long countByBranchId(Long branchId);
}
