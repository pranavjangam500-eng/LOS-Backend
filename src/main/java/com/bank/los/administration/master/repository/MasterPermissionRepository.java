package com.bank.los.administration.master.repository;

import com.bank.los.administration.master.entity.MasterPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MasterPermissionRepository extends JpaRepository<MasterPermission, Integer> {
    Optional<MasterPermission> findByCode(String code);
    boolean existsByCode(String code);
    List<MasterPermission> findByModuleOrderByCodeAsc(String module);
}
