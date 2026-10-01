package com.raibrs.todolist.security;

import com.raibrs.todolist.model.User;
import java.util.Locale;
import java.util.Set;

/** Validates new passwords before the application stores their hashes. */
public final class PasswordPolicy {
    private static final int MINIMUM_LENGTH = 8;
    // Keep a small local denylist so account creation does not depend on an external service.
    private static final Set<String> COMMON_PASSWORDS = Set.of(
            "12345678",
            "123456789",
            "password",
            "password123",
            "qwerty123",
            "admin1234",
            "iloveyou",
            "letmein",
            "welcome1");

    private PasswordPolicy() {
    }

    public static void validate(String username, String password) {
        String normalizedUsername = User.normalizeUsername(username).substring(1);

        if (password == null || password.length() < MINIMUM_LENGTH) {
            throw new IllegalArgumentException("Password must contain at least 8 characters.");
        }

        String normalizedPassword = password.toLowerCase(Locale.ROOT);

        if (normalizedPassword.contains(normalizedUsername)) {
            throw new IllegalArgumentException("Password must not contain your username.");
        }

        if (COMMON_PASSWORDS.contains(normalizedPassword)) {
            throw new IllegalArgumentException("Choose a less common password.");
        }
    }
}
