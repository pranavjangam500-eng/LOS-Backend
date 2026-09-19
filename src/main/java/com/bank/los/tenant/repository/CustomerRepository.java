package com.bank.los.tenant.repository;

import com.bank.los.tenant.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByCustomerCode(String customerCode);
    Optional<Customer> findByPhone(String phone);
    boolean existsByEmail(String email);
    long countByBranchId(Long branchId);
}
