package com.raibrs.todolist.security;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Derives and verifies password hashes with a per-password salt and configurable work factor. */
public final class PasswordHasher {
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String HASH_FORMAT = "pbkdf2-sha256";
    private static final int CURRENT_ITERATIONS = 600_000;
    private static final int MAX_SUPPORTED_ITERATIONS = 2_000_000;
    private static final int SALT_LENGTH_BYTES = 16;
    private static final int HASH_LENGTH_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordHasher() {
    }

    public static String hash(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Password is required.");
        }

        byte[] salt = new byte[SALT_LENGTH_BYTES];
        RANDOM.nextBytes(salt);
        byte[] derivedKey = deriveKey(password, salt, CURRENT_ITERATIONS);

        // Store the algorithm and work factor with the hash to support future upgrades.
        return HASH_FORMAT + "$" + CURRENT_ITERATIONS + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(derivedKey);
    }

    public static boolean matches(String password, String storedHash) {
        if (password == null || !isValidHash(storedHash)) {
            return false;
        }

        String[] parts = storedHash.split("\\$", -1);
        int iterations = Integer.parseInt(parts[1]);
        byte[] salt = Base64.getDecoder().decode(parts[2]);
        byte[] expectedKey = Base64.getDecoder().decode(parts[3]);
        byte[] actualKey = deriveKey(password, salt, iterations);
        // Constant-time comparison reduces information leaks through response timing.
        return MessageDigest.isEqual(expectedKey, actualKey);
    }

    public static boolean isValidHash(String storedHash) {
        if (storedHash == null) {
            return false;
        }

        String[] parts = storedHash.split("\\$", -1);
        if (parts.length != 4 || !HASH_FORMAT.equals(parts[0])) {
            return false;
        }

        try {
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] derivedKey = Base64.getDecoder().decode(parts[3]);
            // Bound verification cost if the account file has been modified.
            return iterations > 0 && iterations <= MAX_SUPPORTED_ITERATIONS
                    && salt.length == SALT_LENGTH_BYTES
                    && derivedKey.length == HASH_LENGTH_BITS / 8;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private static byte[] deriveKey(String password, byte[] salt, int iterations) {
        char[] passwordCharacters = password.toCharArray();
        PBEKeySpec keySpec = new PBEKeySpec(passwordCharacters, salt, iterations, HASH_LENGTH_BITS);
        try {
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(keySpec).getEncoded();
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Could not hash password.", exception);
        } finally {
            keySpec.clearPassword();
            Arrays.fill(passwordCharacters, '\0');
        }
    }
}
