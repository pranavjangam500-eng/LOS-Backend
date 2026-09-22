package com.bank.los.tenant.repository;

import com.bank.los.tenant.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Integer> {

    Optional<Permission> findByCode(String code);

    /** Get all permissions assigned to a given role via join table */
    @Query(value = "SELECT p.* FROM identity.permissions p INNER JOIN identity.role_permissions rp ON p.id = rp.permission_id WHERE rp.role_id = :roleId", nativeQuery = true)
    List<Permission> findByRoleId(@Param("roleId") Integer roleId);
}
