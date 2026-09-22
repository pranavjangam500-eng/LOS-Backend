package com.bank.los.tenant.repository;

import com.bank.los.tenant.entity.TenantUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantUserRepository extends JpaRepository<TenantUser, Long> {

    Optional<TenantUser> findByEmail(String email);

    /** Login via username (UserName field in senior's design) */
    Optional<TenantUser> findByUsername(String username);

    /** Legacy / alternate lookup by empNo */
    Optional<TenantUser> findByEmpNo(String empNo);

    Optional<TenantUser> findByMobile(String mobile);

    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmpNo(String empNo);

    long countByLoginBranchId(Long branchId);
}
