package com.raibrs.todolist.model;

import com.raibrs.todolist.security.PasswordHasher;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class User {
    private final String username;
    private final String passwordHash;

    public User(String username, String password) {
        this(username, password, false);
    }

    // Stored accounts already contain a hash, while new accounts supply a plain password.
    private User(String username, String credential, boolean credentialIsHash) {
        this.username = credentialIsHash ? validateNormalizedUsername(username) : normalizeUsername(username);

        if (credentialIsHash) {
            if (!PasswordHasher.isValidHash(credential)) {
                throw new IllegalArgumentException("Stored password hash is invalid.");
            }
            this.passwordHash = credential;
        } else {
            if (credential == null || credential.isBlank()) {
                throw new IllegalArgumentException("Password is required.");
            }
            this.passwordHash = PasswordHasher.hash(credential);
        }
    }

    private static String validateNormalizedUsername(String username) {
        if (username == null || !username.startsWith("@")
                || !normalizeUsername(username.substring(1)).equals(username)) {
            throw new IllegalArgumentException("Stored username is invalid.");
        }
        return username;
    }

    public static User restore(String normalizedUsername, String passwordHash) {
        return new User(normalizedUsername, passwordHash, true);
    }

    public static String normalizeUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required.");
        }

        String cleanedUsername = username.trim();

        if (!cleanedUsername.matches("[a-zA-Z0-9._]+")) {
            throw new IllegalArgumentException(
                    "Username can contain only letters, numbers, dots, and underscores.");
        }

        return "@" + cleanedUsername.toLowerCase(Locale.ROOT);
    }

    public String getUsername() {
        return username;
    }

    // Used by the repository for persistence; never display this hash in the UI.
    public String getPasswordHashForStorage() {
        return passwordHash;
    }

    private final List<Task> tasks = new ArrayList<>();

    public void addTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task is required.");
        }

        tasks.add(task);
    }

    public List<Task> getTasks() {
        return List.copyOf(tasks);
    }

    public Task getTask(int index) {
        return tasks.get(index);
    }

    public void removeTask(int index) {
        tasks.remove(index);
    }

    public void replaceTasks(List<Task> loadedTasks) {
        // Replacing the list on each login prevents tasks from being duplicated in memory.
        List<Task> validatedTasks = List.copyOf(loadedTasks);
        tasks.clear();
        tasks.addAll(validatedTasks);
    }

    // Re-derive the hash with the stored salt to validate the supplied password.
    public boolean matchesPassword(String password) {
        return PasswordHasher.matches(password, passwordHash);
    }
}
