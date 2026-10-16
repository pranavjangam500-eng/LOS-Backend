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

    void deleteByLookupTypeCode(String lookupTypeCode);
}
