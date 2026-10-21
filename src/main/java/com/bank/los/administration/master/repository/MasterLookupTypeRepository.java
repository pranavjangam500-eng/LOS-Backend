package com.bank.los.administration.master.repository;

import com.bank.los.administration.master.entity.MasterLookupType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MasterLookupTypeRepository extends JpaRepository<MasterLookupType, Long>, JpaSpecificationExecutor<MasterLookupType> {

    Optional<MasterLookupType> findByCode(String code);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM MasterLookupType t WHERE t.code = :code")
    Optional<MasterLookupType> findByCodeForUpdate(@Param("code") String code);

    boolean existsByCode(String code);

    boolean existsByDescriptionIgnoreCase(String description);

    boolean existsByDescriptionIgnoreCaseAndCodeNot(String description, String code);

    @Query("SELECT t FROM MasterLookupType t WHERE t.ownerType = 'INTERNAL_ADMIN' OR t.ownerBankId = :bankId ORDER BY t.code ASC")
    java.util.List<MasterLookupType> findAllVisibleToBank(@Param("bankId") Long bankId);

    void deleteByCode(String code);
}
