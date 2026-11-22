package com.example.learning_tracker_api.dto;

import com.example.learning_tracker_api.entity.AssignmentStatus;
import jakarta.validation.constraints.NotNull;

public record StatusRequest(@NotNull(message = "Status is required.") AssignmentStatus status) {}