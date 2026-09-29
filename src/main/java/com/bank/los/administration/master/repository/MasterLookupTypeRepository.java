package com.bank.los.administration.master.repository;

import com.bank.los.administration.master.entity.MasterLookupType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MasterLookupTypeRepository extends JpaRepository<MasterLookupType, Long> {
    Optional<MasterLookupType> findByCode(String code);
    boolean existsByCode(String code);
    void deleteByCode(String code);
}
