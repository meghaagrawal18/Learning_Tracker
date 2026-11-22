package com.example.learning_tracker_api.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AssignmentStatus {
    NOT_STARTED("Not Started"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed");

    private final String label;

    AssignmentStatus(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static AssignmentStatus fromLabel(String value) {
        for (AssignmentStatus status : values()) {
            if (status.label.equalsIgnoreCase(value == null ? "" : value.trim())) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid status.");
    }
}