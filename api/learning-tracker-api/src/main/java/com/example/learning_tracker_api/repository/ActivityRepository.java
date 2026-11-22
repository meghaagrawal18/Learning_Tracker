package com.example.learning_tracker_api.repository;

import com.example.learning_tracker_api.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
}