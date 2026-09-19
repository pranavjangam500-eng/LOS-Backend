package com.bank.los.tenant.repository;

import com.bank.los.tenant.entity.TenantUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantUserRepository extends JpaRepository<TenantUser, Long> {
    Optional<TenantUser> findByEmail(String email);
    Optional<TenantUser> findByUserCode(String userCode);
    Optional<TenantUser> findByPhone(String phone);
    boolean existsByEmail(String email);
    long countByBranchId(Long branchId);
}
