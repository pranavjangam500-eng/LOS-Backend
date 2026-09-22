package com.bank.los.master.repository;

import com.bank.los.master.entity.InternalUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InternalUserRepository extends JpaRepository<InternalUser, Long> {

    Optional<InternalUser> findByEmail(String email);

    /** Login via username (UserName field in senior's design) */
    Optional<InternalUser> findByUsername(String username);

    /** Lookup by EmpNo */
    Optional<InternalUser> findByEmpNo(String empNo);

    Optional<InternalUser> findByMobile(String mobile);

    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}
