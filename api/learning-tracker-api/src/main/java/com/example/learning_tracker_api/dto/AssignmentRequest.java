package com.example.learning_tracker_api.dto;

import jakarta.validation.constraints.NotNull;

public record AssignmentRequest(
        @NotNull(message = "Learner is required.") Long learnerId,
        @NotNull(message = "Activity is required.") Long activityId
) {}