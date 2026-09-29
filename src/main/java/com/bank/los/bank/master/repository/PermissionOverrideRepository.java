package com.bank.los.bank.master.repository;

import com.bank.los.bank.master.entity.PermissionOverride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionOverrideRepository extends JpaRepository<PermissionOverride, Long> {
    List<PermissionOverride> findByTargetTypeAndTargetNameIgnoreCaseAndIsActiveTrue(String targetType, String targetName);
    List<PermissionOverride> findByTargetTypeAndTargetNameIgnoreCase(String targetType, String targetName);
    Optional<PermissionOverride> findByTargetTypeAndTargetNameIgnoreCaseAndPermissionCodeIgnoreCase(String targetType, String targetName, String permissionCode);
    boolean existsByTargetTypeAndTargetNameIgnoreCaseAndPermissionCodeIgnoreCase(String targetType, String targetName, String permissionCode);
}
