package com.example.learning_tracker_api.dto;

import com.example.learning_tracker_api.entity.Role;

public record LoginResponse(Role role, boolean mustChangePassword) {}