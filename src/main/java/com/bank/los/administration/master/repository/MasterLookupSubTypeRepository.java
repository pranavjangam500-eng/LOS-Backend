package com.bank.los.administration.master.repository;

import com.bank.los.administration.master.entity.MasterLookupSubType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MasterLookupSubTypeRepository extends JpaRepository<MasterLookupSubType, Long>, JpaSpecificationExecutor<MasterLookupSubType> {

    List<MasterLookupSubType> findByLookupTypeCodeOrderByDisplayOrderAscIdAsc(String lookupTypeCode);

    List<MasterLookupSubType> findByLookupTypeCodeAndIsActiveTrueOrderByDisplayOrderAscIdAsc(String lookupTypeCode);

    Optional<MasterLookupSubType> findByLookupTypeCodeAndSubTypeCode(String lookupTypeCode, String subTypeCode);

    boolean existsByLookupTypeCodeAndSubTypeCode(String lookupTypeCode, String subTypeCode);

    boolean existsByLookupTypeCodeAndSubTypeDescriptionIgnoreCase(String lookupTypeCode, String subTypeDescription);

    boolean existsByLookupTypeCodeAndSubTypeDescriptionIgnoreCaseAndIdNot(String lookupTypeCode, String subTypeDescription, Long id);

    long countByLookupTypeCode(String lookupTypeCode);

    @org.springframework.data.jpa.repository.Query("SELECT st FROM MasterLookupSubType st WHERE st.lookupTypeCode = :typeCode AND (st.ownerType = 'INTERNAL_ADMIN' OR st.ownerBankId = :bankId) ORDER BY st.displayOrder ASC, st.id ASC")
    List<MasterLookupSubType> findVisibleToBank(@org.springframework.data.repository.query.Param("typeCode") String typeCode, @org.springframework.data.repository.query.Param("bankId") Long bankId);

    @org.springframework.data.jpa.repository.Query("SELECT st FROM MasterLookupSubType st WHERE st.lookupTypeCode = :typeCode AND st.isActive = true AND (st.ownerType = 'INTERNAL_ADMIN' OR st.ownerBankId = :bankId) ORDER BY st.displayOrder ASC, st.id ASC")
    List<MasterLookupSubType> findActiveVisibleToBank(@org.springframework.data.repository.query.Param("typeCode") String typeCode, @org.springframework.data.repository.query.Param("bankId") Long bankId);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(st) FROM MasterLookupSubType st WHERE st.lookupTypeCode = :typeCode AND (st.ownerType = 'INTERNAL_ADMIN' OR st.ownerBankId = :bankId)")
    long countVisibleToBank(@org.springframework.data.repository.query.Param("typeCode") String typeCode, @org.springframework.data.repository.query.Param("bankId") Long bankId);

    void deleteByLookupTypeCode(String lookupTypeCode);
}
