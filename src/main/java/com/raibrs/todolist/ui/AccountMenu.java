package com.raibrs.todolist.ui;

import com.raibrs.todolist.model.User;
import com.raibrs.todolist.repository.UserRepository;
import java.util.Locale;
import java.util.Scanner;

/** Owns account prompts while delegating account rules and persistence to UserRepository. */
final class AccountMenu {
    private final Scanner scanner;
    private final UserRepository userRepository;

    AccountMenu(Scanner scanner, UserRepository userRepository) {
        this.scanner = scanner;
        this.userRepository = userRepository;
    }

    User run() {
        while (true) {
            System.out.println("[L] Login");
            System.out.println("[N] Create account");
            System.out.println("[Q] Quit");
            System.out.print("Choose an option: ");

            if (!scanner.hasNextLine()) {
                return null;
            }

            String option = scanner.nextLine().trim().toUpperCase(Locale.ROOT);
            switch (option) {
                case "L":
                    User loggedInUser = login();
                    if (loggedInUser != null) {
                        return loggedInUser;
                    }
                    break;
                case "N":
                    User user = registerUser();
                    if (user != null) {
                        return user;
                    }
                    break;
                case "Q":
                    return null;
                default:
                    System.out.println("⚠️  Invalid option. Try again.");
            }
            System.out.println();
        }
    }

    User changePassword(User user) {
        System.out.println();
        System.out.print("Current password: ");
        if (!scanner.hasNextLine()) {
            return user;
        }
        String currentPassword = scanner.nextLine();

        System.out.print("New password: ");
        if (!scanner.hasNextLine()) {
            return user;
        }
        String newPassword = scanner.nextLine();

        System.out.print("Confirm new password: ");
        if (!scanner.hasNextLine()) {
            return user;
        }
        String confirmation = scanner.nextLine();

        if (!newPassword.equals(confirmation)) {
            System.out.println("⚠️  Passwords do not match.");
            System.out.println();
            return user;
        }

        try {
            User updatedUser = userRepository.changePassword(
                    user.getUsername().substring(1), currentPassword, newPassword);
            System.out.println("✅ Password changed.");
            System.out.println();
            return updatedUser;
        } catch (IllegalArgumentException exception) {
            System.out.println("⚠️  " + exception.getMessage());
        } catch (IllegalStateException exception) {
            System.out.println("⚠️  Could not save the new password.");
        }

        System.out.println();
        return user;
    }

    private User login() {
        System.out.println("🔐 Sign in");
        System.out.print("Username: ");
        if (!scanner.hasNextLine()) {
            return null;
        }
        String username = scanner.nextLine();

        System.out.print("Password: ");
        if (!scanner.hasNextLine()) {
            return null;
        }
        String password = scanner.nextLine();

        try {
            // Normalize the username so login is case-insensitive.
            User user = userRepository.findByUsername(username);
            if (user.matchesPassword(password)) {
                return user;
            }
        } catch (IllegalArgumentException exception) {
            // Use the same message to avoid revealing whether the account exists.
        }

        System.out.println("⚠️  Invalid username or password.");
        return null;
    }

    private User registerUser() {
        System.out.println("👤 Create account");
        System.out.print("Username: ");
        if (!scanner.hasNextLine()) {
            return null;
        }
        String username = scanner.nextLine();

        System.out.print("Password: ");
        if (!scanner.hasNextLine()) {
            return null;
        }
        String password = scanner.nextLine();

        System.out.print("Confirm password: ");
        if (!scanner.hasNextLine()) {
            return null;
        }
        String confirmation = scanner.nextLine();

        if (!password.equals(confirmation)) {
            System.out.println("⚠️  Passwords do not match.");
            return null;
        }

        try {
            User user = userRepository.registerUser(username, password);
            System.out.println("✅ Account created.");
            return user;
        } catch (IllegalArgumentException exception) {
            System.out.println("⚠️  " + exception.getMessage());
        } catch (IllegalStateException exception) {
            System.out.println("⚠️  Could not save the account.");
        }
        return null;
    }
}
