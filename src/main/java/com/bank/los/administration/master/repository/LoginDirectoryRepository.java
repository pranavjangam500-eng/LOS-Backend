package com.bank.los.administration.master.repository;

import com.bank.los.administration.master.entity.LoginDirectory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoginDirectoryRepository extends JpaRepository<LoginDirectory, Long> {
    Optional<LoginDirectory> findByEmail(String email);
    Optional<LoginDirectory> findByUserCode(String userCode);
    Optional<LoginDirectory> findByPhone(String phone);

    @Query("SELECT ld FROM LoginDirectory ld WHERE ld.email = :email OR ld.userCode = :userCode OR ld.phone = :phone")
    Optional<LoginDirectory> findByEmailOrUserCodeOrPhone(
            @Param("email") String email,
            @Param("userCode") String userCode,
            @Param("phone") String phone
    );

    boolean existsByEmail(String email);
    boolean existsByUserCode(String userCode);
    boolean existsByPhone(String phone);
}
