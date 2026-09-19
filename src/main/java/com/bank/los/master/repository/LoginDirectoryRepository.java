package com.bank.los.master.repository;

import com.bank.los.master.entity.LoginDirectory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoginDirectoryRepository extends JpaRepository<LoginDirectory, Long> {
    Optional<LoginDirectory> findByEmail(String email);
    Optional<LoginDirectory> findByUserCode(String userCode);
    Optional<LoginDirectory> findByPhone(String phone);
    Optional<LoginDirectory> findByEmailOrUserCodeOrPhone(String email, String userCode, String phone);
    boolean existsByEmail(String email);
}
