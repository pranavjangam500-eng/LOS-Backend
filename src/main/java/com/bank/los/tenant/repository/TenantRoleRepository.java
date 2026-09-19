package com.bank.los.tenant.repository;

import com.bank.los.tenant.entity.TenantRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantRoleRepository extends JpaRepository<TenantRole, Integer> {
    Optional<TenantRole> findByName(String name);
}
