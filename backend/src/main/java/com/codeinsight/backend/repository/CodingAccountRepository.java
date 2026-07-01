package com.codeinsight.backend.repository;

import com.codeinsight.backend.entity.CodingAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CodingAccountRepository extends JpaRepository<CodingAccount, Long> {
    Optional<CodingAccount> findByUserId(Long userId);
}
