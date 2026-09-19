package com.bank.los.master.repository;

import com.bank.los.master.entity.MasterRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MasterRoleRepository extends JpaRepository<MasterRole, Integer> {
    Optional<MasterRole> findByName(String name);
}
