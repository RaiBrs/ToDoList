package com.todolist;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class User {
    private final String username;
    private final String password;

    public User(String username, String password) {
        this.username = normalizeUsername(username);

        // password cannot be null or blank
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required.");
        }

        this.password = password;
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

    // Username getter
    public String getUsername() {
        return username;
    }

    // User tasks
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

    // Checks whether the provided password matches this user's password.
    public boolean matchesPassword(String password){
        return this.password.equals(password);
    }
}
