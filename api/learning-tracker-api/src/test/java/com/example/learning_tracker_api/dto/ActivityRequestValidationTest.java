package com.example.learning_tracker_api.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActivityRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private Set<ConstraintViolation<ActivityRequest>> check(String title, String topic, Double hours) {
        return validator.validate(new ActivityRequest(title, topic, hours));
    }

    @Test
    void validRequestPasses() {
        assertTrue(check("React Basics", "Frontend", 6.0).isEmpty());
    }

    @Test
    void blankTitleIsRejected() {
        assertEquals(1, check("  ", "Frontend", 6.0).size());
    }

    @Test
    void blankTopicIsRejected() {
        assertEquals(1, check("React Basics", "", 6.0).size());
    }

    @Test
    void missingHoursIsRejected() {
        assertEquals(1, check("React Basics", "Frontend", null).size());
    }

    @Test
    void zeroHoursIsRejected() {
        assertEquals(1, check("React Basics", "Frontend", 0.0).size());
    }

    @Test
    void negativeHoursIsRejected() {
        assertEquals(1, check("React Basics", "Frontend", -2.0).size());
    }
}