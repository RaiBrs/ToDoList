package com.raibrs.todolist.repository;

import com.raibrs.todolist.model.User;
import com.raibrs.todolist.security.PasswordPolicy;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {

    private final List<User> users = new ArrayList<>();
    private final UserStorage userStorage;

    public UserRepository(UserStorage userStorage) {
        if (userStorage == null) {
            throw new IllegalArgumentException("User storage is required.");
        }

        this.userStorage = userStorage;
        users.addAll(userStorage.loadUsers());
    }

    public void addUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User is required.");
        }

        // Compare normalized usernames so different casing cannot create duplicate accounts.
        for (User u : users) {
            if (u.getUsername().equals(user.getUsername())) {
                throw new IllegalArgumentException("User already exists.");
            }
        }

        List<User> updatedUsers = new ArrayList<>(users);
        updatedUsers.add(user);

        // Save before updating memory so an account is not available only until the next restart.
        userStorage.saveUsers(updatedUsers);
        users.add(user);
    }

    public User registerUser(String username, String password) {
        PasswordPolicy.validate(username, password);
        User user = new User(username, password);
        addUser(user);
        return user;
    }

    /** Verifies the current password and saves a policy-compliant replacement before updating memory. */
    public User changePassword(String username, String currentPassword, String newPassword) {
        User currentUser = findByUsername(username);
        if (!currentUser.matchesPassword(currentPassword)) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }

        PasswordPolicy.validate(username, newPassword);
        if (currentUser.matchesPassword(newPassword)) {
            throw new IllegalArgumentException("New password must be different from the current password.");
        }

        User updatedUser = currentUser.withPassword(newPassword);
        List<User> updatedUsers = new ArrayList<>(users);
        int userIndex = updatedUsers.indexOf(currentUser);
        updatedUsers.set(userIndex, updatedUser);

        // Persist the replacement before updating the repository's live account.
        userStorage.saveUsers(updatedUsers);
        users.set(userIndex, updatedUser);
        return updatedUser;
    }

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
