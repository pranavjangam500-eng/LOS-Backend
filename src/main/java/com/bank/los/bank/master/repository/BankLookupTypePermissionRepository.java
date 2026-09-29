package com.bank.los.bank.master.repository;

import com.bank.los.bank.master.entity.BankLookupTypePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankLookupTypePermissionRepository extends JpaRepository<BankLookupTypePermission, Long> {

    List<BankLookupTypePermission> findByLookupTypeCode(String lookupTypeCode);

    boolean existsByLookupTypeCodeAndPermissionCode(String lookupTypeCode, String permissionCode);

    void deleteByLookupTypeCode(String lookupTypeCode);
}
