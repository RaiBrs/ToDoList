package com.raibrs.todolist.ui;

import com.raibrs.todolist.model.User;
import com.raibrs.todolist.repository.SessionStorage;
import com.raibrs.todolist.repository.UserRepository;
import com.raibrs.todolist.service.TaskService;
import java.util.Optional;
import java.util.Scanner;

/** Coordinates the application session and transitions between the account and task menus. */
public final class ConsoleUi {
    private ConsoleUi() {
    }

    public static void run(UserRepository userRepository, TaskService taskService,
                           SessionStorage sessionStorage) {
        Scanner scanner = new Scanner(System.in);
        // Both menus share one scanner so they consume the same continuous console input.
        AccountMenu accountMenu = new AccountMenu(scanner, userRepository);
        TaskMenu taskMenu = new TaskMenu(scanner, taskService, accountMenu);

        showWelcome();
        User resumedUser = restoreSession(userRepository, sessionStorage);

        // Logging out returns to the account menu without closing the application.
        while (true) {
            User user = resumedUser;
            resumedUser = null;

            if (user == null) {
                user = accountMenu.run();
                if (user == null) {
                    System.out.println("Goodbye! 👋");
                    return;
                }
                saveSession(user, sessionStorage);
            }

            try {
                // Reload tasks by the authenticated username on every login.
                taskService.loadTasks(user);
            } catch (IllegalStateException exception) {
                System.out.println("⚠️  Could not load tasks for " + user.getUsername() + ".");
                clearSession(sessionStorage);
                System.out.println();
                continue;
            }

            showLoginSuccess(user);
            TaskMenu.Result result = taskMenu.run(user);
            if (result == TaskMenu.Result.QUIT) {
                System.out.println("Goodbye! 👋");
                return;
            }

            clearSession(sessionStorage);
            System.out.println();
            System.out.println("👋 Logged out.");
            System.out.println();
        }
    }

    private static User restoreSession(UserRepository userRepository, SessionStorage sessionStorage) {
        Optional<String> savedUsername;
        try {
            savedUsername = sessionStorage.loadUsername();
        } catch (IllegalStateException exception) {
            System.out.println("⚠️  Could not load the saved session.");
            return null;
        }

        if (savedUsername.isEmpty()) {
            return null;
        }

        try {
            User user = userRepository.findByUsername(savedUsername.get());
            System.out.println("🔄 Resuming saved session for " + user.getUsername() + ".");
            return user;
        } catch (IllegalArgumentException exception) {
            clearSession(sessionStorage);
            return null;
        }
    }

    private static void saveSession(User user, SessionStorage sessionStorage) {
        try {
            // Store only the normalized username; never write the password to the session file.
            sessionStorage.saveUsername(user.getUsername().substring(1));
        } catch (IllegalStateException exception) {
            System.out.println("⚠️  Could not save the session. You may need to sign in next time.");
        }
    }

    private static void clearSession(SessionStorage sessionStorage) {
        try {
            sessionStorage.clearSession();
        } catch (IllegalStateException exception) {
            System.out.println("⚠️  Could not clear the saved session file.");
        }
    }

    private static void showWelcome() {
        System.out.println();
        System.out.println("════════════════════════════════════");
        System.out.println("          📝  TODO LIST");
        System.out.println("════════════════════════════════════");
        System.out.println("        Stay focused. Get it done.");
        System.out.println();
    }

    private static void showLoginSuccess(User user) {
        System.out.println();
        System.out.println("────────────────────────────────────");
        System.out.println("✅ Login successful!");
        System.out.println("👋 Welcome, " + user.getUsername() + ".");
        System.out.println("────────────────────────────────────");
        System.out.println();
    }
}
