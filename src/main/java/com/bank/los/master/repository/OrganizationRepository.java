package com.bank.los.master.repository;

import com.bank.los.master.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    Optional<Organization> findByCode(String code);
    Optional<Organization> findByDbName(String dbName);
    boolean existsByCode(String code);
    boolean existsByDbName(String dbName);
}
