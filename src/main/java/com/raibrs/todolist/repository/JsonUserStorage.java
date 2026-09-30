package com.raibrs.todolist.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.raibrs.todolist.model.User;
import com.raibrs.todolist.security.PasswordHasher;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/** Persists normalized usernames and password hashes; plain-text passwords never enter the JSON file. */
public class JsonUserStorage implements UserStorage {
    private final Path filePath;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonUserStorage(Path filePath) {
        this.filePath = filePath;
    }

    @Override
    public List<User> loadUsers() {
        if (Files.notExists(filePath)) {
            return List.of();
        }

        try {
            JsonNode root = objectMapper.readTree(filePath.toFile());
            if (root == null || !root.isObject()) {
                throw invalidFile("the root must be a JSON object");
            }

            List<User> users = new ArrayList<>();
            Iterator<Map.Entry<String, JsonNode>> entries = root.properties().iterator();
            while (entries.hasNext()) {
                Map.Entry<String, JsonNode> entry = entries.next();
                JsonNode account = entry.getValue();
                JsonNode passwordHash = account.isObject() ? account.get("passwordHash") : null;
                if (passwordHash == null || !passwordHash.isTextual()
                        || !PasswordHasher.isValidHash(passwordHash.asText())) {
                    throw invalidFile("each account must contain a valid password hash");
                }

                try {
                    users.add(User.restore(entry.getKey(), passwordHash.asText()));
                } catch (IllegalArgumentException exception) {
                    throw invalidFile("an account username or password hash is invalid");
                }
            }

            return users;
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read saved user file " + filePath + ".", exception);
        }
    }

    @Override
    public void saveUsers(List<User> users) {
        Path absoluteFilePath = filePath.toAbsolutePath();
        Path parentDirectory = absoluteFilePath.getParent();
        Path temporaryFile = null;

        try {
            Files.createDirectories(parentDirectory);
            ObjectNode root = objectMapper.createObjectNode();
            for (User user : users) {
                ObjectNode account = objectMapper.createObjectNode();
                account.put("passwordHash", user.getPasswordHashForStorage());
                root.set(user.getUsername(), account);
            }

            // Replace the old file only after the complete JSON document is ready.
            temporaryFile = Files.createTempFile(parentDirectory, "users-", ".tmp");
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(temporaryFile.toFile(), root);
            Files.move(temporaryFile, absoluteFilePath,
                    StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not safely save users to " + filePath + ".", exception);
        } finally {
            if (temporaryFile != null) {
                try {
                    Files.deleteIfExists(temporaryFile);
                } catch (IOException exception) {
                    throw new IllegalStateException("Could not remove temporary user file.", exception);
                }
            }
        }
    }

    private IllegalStateException invalidFile(String reason) {
        return new IllegalStateException("Invalid saved user file " + filePath + ": " + reason + ".");
    }
}
