package com.example.learning_tracker_api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LearnerRequest(
        @NotBlank(message = "Name is required.") String name,
        @NotBlank(message = "Email is required.")
        @Email(message = "Email must be a valid address.") String email,
        @NotBlank(message = "Cohort is required.") String cohort
) {}