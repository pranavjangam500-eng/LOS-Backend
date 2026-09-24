package com.bank.los.administration.master.repository;

import com.bank.los.administration.master.entity.MasterRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MasterRoleRepository extends JpaRepository<MasterRole, Integer> {
    Optional<MasterRole> findByName(String name);
}
