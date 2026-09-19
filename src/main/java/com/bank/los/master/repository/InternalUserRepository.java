package com.bank.los.master.repository;

import com.bank.los.master.entity.InternalUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InternalUserRepository extends JpaRepository<InternalUser, Long> {
    Optional<InternalUser> findByEmail(String email);
    Optional<InternalUser> findByUserCode(String userCode);
    Optional<InternalUser> findByPhone(String phone);
    boolean existsByEmail(String email);
}
