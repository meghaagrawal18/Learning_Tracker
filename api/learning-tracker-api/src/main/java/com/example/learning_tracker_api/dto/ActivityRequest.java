package com.example.learning_tracker_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ActivityRequest(
        @NotBlank(message = "Title is required.") String title,
        @NotBlank(message = "Topic is required.") String topic,
        @NotNull(message = "Planned hours is required.")
        @Positive(message = "Planned hours must be greater than zero.") Double plannedHours
) {}