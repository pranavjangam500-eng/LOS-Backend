package com.bank.los.administration.master.repository;

import com.bank.los.administration.master.entity.MasterLookupTypePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasterLookupTypePermissionRepository extends JpaRepository<MasterLookupTypePermission, Long> {
    List<MasterLookupTypePermission> findByLookupTypeCode(String lookupTypeCode);
    void deleteByLookupTypeCode(String lookupTypeCode);
}
