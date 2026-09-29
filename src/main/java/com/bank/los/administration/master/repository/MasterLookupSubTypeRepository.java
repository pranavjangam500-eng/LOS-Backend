package com.bank.los.administration.master.repository;

import com.bank.los.administration.master.entity.MasterLookupSubType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MasterLookupSubTypeRepository extends JpaRepository<MasterLookupSubType, Long> {
    List<MasterLookupSubType> findByLookupTypeCodeOrderByDisplayOrderAscIdAsc(String lookupTypeCode);
    List<MasterLookupSubType> findByLookupTypeCodeAndIsActiveTrueOrderByDisplayOrderAscIdAsc(String lookupTypeCode);
    Optional<MasterLookupSubType> findByLookupTypeCodeAndSubTypeCode(String lookupTypeCode, String subTypeCode);
    boolean existsByLookupTypeCodeAndSubTypeCode(String lookupTypeCode, String subTypeCode);
    void deleteByLookupTypeCode(String lookupTypeCode);
}
