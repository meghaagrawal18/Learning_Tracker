package com.example.learning_tracker_api.dto;

import java.time.Instant;

public record CredentialsResponse(String email, String temporaryPassword, Instant expiresAt) {}