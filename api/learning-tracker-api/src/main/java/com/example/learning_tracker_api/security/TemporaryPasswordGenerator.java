package com.example.learning_tracker_api.security;

import java.security.SecureRandom;

public final class TemporaryPasswordGenerator {

    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private TemporaryPasswordGenerator() {}

    public static String generate() {
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 14; i++) {
            password.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return password.toString();
    }
}