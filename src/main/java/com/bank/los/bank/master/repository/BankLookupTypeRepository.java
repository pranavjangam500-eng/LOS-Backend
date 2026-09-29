package com.bank.los.bank.master.repository;

import com.bank.los.bank.master.entity.BankLookupType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BankLookupTypeRepository extends JpaRepository<BankLookupType, Long> {
    Optional<BankLookupType> findByCode(String code);
    boolean existsByCode(String code);
    void deleteByCode(String code);
}
