package com.bank.los.administration.master.repository;

import com.bank.los.administration.master.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    
    @Query("SELECT o FROM Organization o WHERE o.bankCode = :code")
    Optional<Organization> findByCode(@Param("code") String code);

    Optional<Organization> findByBankCode(String bankCode);

    @Query("SELECT o FROM Organization o WHERE o.bankCode = :institutionCode")
    Optional<Organization> findByInstitutionCode(@Param("institutionCode") String institutionCode);

    Optional<Organization> findByUuid(UUID uuid);

    Optional<Organization> findByDbName(String dbName);

    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END FROM Organization o WHERE o.bankCode = :code")
    boolean existsByCode(@Param("code") String code);

    boolean existsByBankCode(String bankCode);

    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END FROM Organization o WHERE o.bankCode = :institutionCode")
    boolean existsByInstitutionCode(@Param("institutionCode") String institutionCode);

    boolean existsByDbName(String dbName);
}


