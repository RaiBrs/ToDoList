package com.raibrs.todolist.repository;

import com.raibrs.todolist.model.User;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/** Stores the username used to resume this local study application's session. */
public class SessionStorage {
    private final Path filePath;

    public SessionStorage(Path filePath) {
        if (filePath == null) {
            throw new IllegalArgumentException("Session file path is required.");
        }
        this.filePath = filePath;
    }

    public Optional<String> loadUsername() {
        if (Files.notExists(filePath)) {
            return Optional.empty();
        }

        try {
            String savedUsername = Files.readString(filePath, StandardCharsets.UTF_8).trim();
            if (savedUsername.isEmpty()) {
                clearSession();
                return Optional.empty();
            }

            return Optional.of(User.normalizeUsername(savedUsername).substring(1));
        } catch (IllegalArgumentException exception) {
            // A malformed marker should not prevent the user from signing in normally.
            clearSession();
            return Optional.empty();
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read saved session " + filePath + ".", exception);
        }
    }

    public void saveUsername(String username) {
        String normalizedUsername = User.normalizeUsername(username).substring(1);
        Path absoluteFilePath = filePath.toAbsolutePath();

        try {
            Files.createDirectories(absoluteFilePath.getParent());
            Files.writeString(absoluteFilePath, normalizedUsername, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not save session " + filePath + ".", exception);
        }
    }

    public void clearSession() {
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not clear saved session " + filePath + ".", exception);
        }
    }
}
