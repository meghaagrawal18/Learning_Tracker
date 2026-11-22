package com.example.learning_tracker_api.security;

import com.example.learning_tracker_api.exception.TooManyRequestsException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginRateLimiterTest {

    @Test
    void blocksAfterTooManyFailures() {
        LoginRateLimiter limiter = new LoginRateLimiter();
        for (int i = 0; i < 5; i++) {
            limiter.checkAllowed("a@x.com|1.1.1.1", 5);
            limiter.recordFailure("a@x.com|1.1.1.1");
        }
        assertThrows(TooManyRequestsException.class, () -> limiter.checkAllowed("a@x.com|1.1.1.1", 5));
    }

    @Test
    void otherKeysAreNotAffected() {
        LoginRateLimiter limiter = new LoginRateLimiter();
        for (int i = 0; i < 5; i++) {
            limiter.recordFailure("a@x.com|1.1.1.1");
        }
        assertDoesNotThrow(() -> limiter.checkAllowed("b@x.com|1.1.1.1", 5));
    }

    @Test
    void successfulLoginClearsFailures() {
        LoginRateLimiter limiter = new LoginRateLimiter();
        for (int i = 0; i < 5; i++) {
            limiter.recordFailure("a@x.com|1.1.1.1");
        }
        limiter.reset("a@x.com|1.1.1.1");
        assertDoesNotThrow(() -> limiter.checkAllowed("a@x.com|1.1.1.1", 5));
    }
}