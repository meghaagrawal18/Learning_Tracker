package com.example.learning_tracker_api.repository;

import com.example.learning_tracker_api.entity.Account;
import com.example.learning_tracker_api.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByRole(Role role);
}