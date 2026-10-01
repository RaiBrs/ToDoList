package com.raibrs.todolist.security;

import com.raibrs.todolist.model.User;
import java.util.Locale;

/** Validates new passwords before the application stores their hashes. */
public final class PasswordPolicy {
    private static final int MINIMUM_LENGTH = 8;

    private PasswordPolicy() {
    }

    public static void validate(String username, String password) {
        String normalizedUsername = User.normalizeUsername(username).substring(1);

        if (password == null || password.length() < MINIMUM_LENGTH) {
            throw new IllegalArgumentException("Password must contain at least 8 characters.");
        }

        if (password.toLowerCase(Locale.ROOT).contains(normalizedUsername)) {
            throw new IllegalArgumentException("Password must not contain your username.");
        }
    }
}
