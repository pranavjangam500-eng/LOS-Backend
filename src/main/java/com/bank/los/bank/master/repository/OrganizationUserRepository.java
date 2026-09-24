package com.bank.los.bank.master.repository;

import com.bank.los.bank.master.entity.OrganizationUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationUserRepository extends JpaRepository<OrganizationUser, Long> {
    Optional<OrganizationUser> findByEmail(String email);
    Optional<OrganizationUser> findByUsername(String username);
    Optional<OrganizationUser> findByEmpNo(String empNo);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmpNo(String empNo);
    List<OrganizationUser> findByLoginBranchId(Long branchId);
    long countByLoginBranchId(Long branchId);
}
