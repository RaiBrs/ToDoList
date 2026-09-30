package com.raibrs.todolist.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PasswordHasherTest {

    // A random salt makes identical passwords produce different hashes.
    @Test
    void hashesSamePasswordDifferentlyAndVerifiesIt() {
        String firstHash = PasswordHasher.hash("1234");
        String secondHash = PasswordHasher.hash("1234");

        assertNotEquals(firstHash, secondHash);
        assertTrue(PasswordHasher.matches("1234", firstHash));
        assertFalse(PasswordHasher.matches("wrong-password", firstHash));
    }
}
