package com.example.learning_tracker_api.dto;

import com.example.learning_tracker_api.entity.AssignmentStatus;

public record AssignmentResponse(Long id, Long learnerId, ActivityResponse activity, AssignmentStatus status) {}