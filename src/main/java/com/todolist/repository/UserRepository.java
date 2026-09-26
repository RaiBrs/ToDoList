package com.todolist.repository;

import com.todolist.User;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {

    private final List<User> users = new ArrayList<>();

    public void addUser(User user) {

        // Prevent the list from receiving null values
        if (user == null) {
            throw new IllegalArgumentException("User is required.");
        }

        // Prevent duplicate usernames
        for (User u : users) {
            if (u.getUsername().equals(user.getUsername())) {
                throw new IllegalArgumentException("User already exists.");
            }
        }

        users.add(user);
    }

    // Find user
    public User findByUsername(String username) {
        String normalizedUsername = User.normalizeUsername(username);

        for (User user : users) {
            if (user.getUsername().equals(normalizedUsername)) {
                return user;
            }
        }

        throw new IllegalArgumentException("User not found.");
    }
}
