package com.raibrs.todolist.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raibrs.todolist.model.User;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JsonUserStorageTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void savesOnlyPasswordHashAndRestoresAccount() throws IOException {
        Path filePath = temporaryDirectory.resolve("users.json");
        UserStorage storage = new JsonUserStorage(filePath);
        User user = new User("rai", "secret");

        storage.saveUsers(List.of(user));

        JsonNode savedAccount = new ObjectMapper().readTree(filePath.toFile()).get("@rai");
        assertTrue(savedAccount.get("passwordHash").asText().startsWith("pbkdf2-sha256$"));
        assertFalse(savedAccount.toString().contains("secret"));

        User restoredUser = storage.loadUsers().get(0);
        assertEquals("@rai", restoredUser.getUsername());
        assertTrue(restoredUser.matchesPassword("secret"));
        assertFalse(restoredUser.matchesPassword("wrong-password"));
    }
}
