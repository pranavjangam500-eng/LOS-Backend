package com.bank.los.bank.master.repository;

import com.bank.los.bank.master.entity.OrganizationRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationRoleRepository extends JpaRepository<OrganizationRole, Integer> {
    Optional<OrganizationRole> findByName(String name);
}
