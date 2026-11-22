package com.example.learning_tracker_api.repository;

import com.example.learning_tracker_api.entity.Learner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LearnerRepository extends JpaRepository<Learner, Long> {
    Optional<Learner> findByAccountId(Long accountId);
}