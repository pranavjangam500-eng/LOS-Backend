package com.bank.los.bank.master.repository;

import com.bank.los.bank.master.entity.DesignationRoleMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DesignationRoleMappingRepository extends JpaRepository<DesignationRoleMapping, Long> {
    Optional<DesignationRoleMapping> findByDesignationIgnoreCase(String designation);
    Optional<DesignationRoleMapping> findByDesignationIgnoreCaseAndIsActiveTrue(String designation);
    List<DesignationRoleMapping> findByRoleId(Integer roleId);
    boolean existsByDesignationIgnoreCase(String designation);
    void deleteByDesignationIgnoreCase(String designation);
}
