package com.bank.los.bank.master.repository;

import com.bank.los.bank.master.entity.BankLookupSubType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankLookupSubTypeRepository extends JpaRepository<BankLookupSubType, Long> {
    List<BankLookupSubType> findByLookupTypeCodeOrderByDisplayOrderAscIdAsc(String lookupTypeCode);
    List<BankLookupSubType> findByLookupTypeCodeAndIsActiveTrueOrderByDisplayOrderAscIdAsc(String lookupTypeCode);
    Optional<BankLookupSubType> findByLookupTypeCodeAndSubTypeCode(String lookupTypeCode, String subTypeCode);
    boolean existsByLookupTypeCodeAndSubTypeCode(String lookupTypeCode, String subTypeCode);
    void deleteByLookupTypeCode(String lookupTypeCode);
}
