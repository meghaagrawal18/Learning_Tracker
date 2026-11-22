package com.example.learning_tracker_api.repository;

import com.example.learning_tracker_api.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByLearnerId(Long learnerId);
    boolean existsByLearnerIdAndActivityId(Long learnerId, Long activityId);
    long countByStatus(com.example.learning_tracker_api.entity.AssignmentStatus status);
}