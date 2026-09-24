package com.bank.los.administration.master.repository;

import com.bank.los.administration.master.entity.InternalUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InternalUserRepository extends JpaRepository<InternalUser, Long> {
    Optional<InternalUser> findByEmail(String email);
    Optional<InternalUser> findByUsername(String username);
    Optional<InternalUser> findByEmpNo(String empNo);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmpNo(String empNo);
}
