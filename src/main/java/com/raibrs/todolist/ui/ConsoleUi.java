package com.raibrs.todolist.ui;

import com.raibrs.todolist.model.Task;
import com.raibrs.todolist.model.User;
import com.raibrs.todolist.repository.SessionStorage;
import com.raibrs.todolist.repository.UserRepository;
import com.raibrs.todolist.service.TaskService;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Scanner;

public final class ConsoleUi {
    private ConsoleUi() {
    }

    public static void run(UserRepository userRepository, TaskService taskService,
                           SessionStorage sessionStorage) {
        Scanner scanner = new Scanner(System.in);
        showWelcome();
        User resumedUser = restoreSession(userRepository, sessionStorage);

        // Logging out returns to the account menu without closing the application.
        while (true) {
            User user = resumedUser;
            resumedUser = null;

            if (user == null) {
                user = accountMenu(scanner, userRepository);
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
            TaskMenuResult result = runTaskMenu(scanner, user, taskService, userRepository, sessionStorage);
            if (result == TaskMenuResult.QUIT) {
                System.out.println("Goodbye! 👋");
                return;
            }
        }
    }

    private enum TaskMenuResult {
        LOGOUT,
        QUIT
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

    private static User accountMenu(Scanner scanner, UserRepository userRepository) {
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
                    User loggedInUser = login(scanner, userRepository);
                    if (loggedInUser != null) {
                        return loggedInUser;
                    }
                    break;
                case "N":
                    User user = registerUser(scanner, userRepository);
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

    private static User login(Scanner scanner, UserRepository userRepository) {
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

    private static User registerUser(Scanner scanner, UserRepository userRepository) {
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

    private static void showLoginSuccess(User user) {
        System.out.println();
        System.out.println("────────────────────────────────────");
        System.out.println("✅ Login successful!");
        System.out.println("👋 Welcome, " + user.getUsername() + ".");
        System.out.println("────────────────────────────────────");
        System.out.println();
    }

    private static TaskMenuResult runTaskMenu(Scanner scanner, User user, TaskService taskService,
                                              UserRepository userRepository, SessionStorage sessionStorage) {
        while (true) {

            showTasks(user);
            showMenu();

            if (!scanner.hasNextLine()) {
                return TaskMenuResult.QUIT;
            }

            String option = scanner.nextLine().trim().toUpperCase(Locale.ROOT);

            switch (option) {
                case "A":
                    addTask(scanner, user, taskService);
                    break;
                case "E":
                    editTask(scanner, user, taskService);
                    break;
                case "C":
                    completeTask(scanner, user, taskService);
                    break;
                case "R":
                    removeTask(scanner, user, taskService);
                    break;
                case "P":
                    // Keep the active session on the account object with the new password hash.
                    user = changePassword(scanner, user, userRepository);
                    break;
                case "L":
                    clearSession(sessionStorage);
                    System.out.println();
                    System.out.println("👋 Logged out.");
                    System.out.println();
                    return TaskMenuResult.LOGOUT;
                case "Q":
                    return TaskMenuResult.QUIT;
                default:
                    System.out.println();
                    System.out.println("⚠️  Invalid option. Try again.");
                    System.out.println();
            }
        }
    }

    private static void showTasks(User user) {
        List<Task> tasks = user.getTasks();

        System.out.println("📝 My Tasks");
        System.out.println("────────────────────────────────────");

        if (tasks.isEmpty()) {
            System.out.println("No tasks yet. Add your first task ✨");
        } else {
            for (int index = 0; index < tasks.size(); index++) {
                Task task = tasks.get(index);
                String status = task.isCompleted() ? "✓" : " ";
                System.out.printf("%d. [%s] %s%n", index + 1, status, task.getTitle());
            }
        }

        System.out.println();
    }

    private static void showMenu() {
        System.out.println("[A] Add task");
        System.out.println("[E] Edit task title");
        System.out.println("[C] Complete task");
        System.out.println("[R] Remove task");
        System.out.println("[P] Change password");
        System.out.println("[L] Logout");
        System.out.println("[Q] Quit and keep session");
        System.out.print("Choose an option: ");
    }

    private static User changePassword(Scanner scanner, User user, UserRepository userRepository) {
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

    private static void addTask(Scanner scanner, User user, TaskService taskService) {
        System.out.println();
        System.out.print("Task title: ");

        if (!scanner.hasNextLine()) {
            return;
        }

        String title = scanner.nextLine();

        try {
            taskService.addTask(user, title);
            System.out.println("✅ Task added.");
        } catch (IllegalArgumentException exception) {
            System.out.println("⚠️  " + exception.getMessage());
        } catch (IllegalStateException exception) {
            System.out.println("⚠️  Could not save tasks.");
        }

        System.out.println();
    }

    private static void editTask(Scanner scanner, User user, TaskService taskService) {
        int taskIndex = readTaskIndex(scanner, user, "Edit task number: ");
        if (taskIndex < 0) {
            return;
        }

        System.out.print("New task title: ");
        if (!scanner.hasNextLine()) {
            return;
        }

        String newTitle = scanner.nextLine();
        try {
            taskService.editTask(user, taskIndex, newTitle);
            System.out.println("✅ Task title updated.");
        } catch (IllegalArgumentException exception) {
            System.out.println("⚠️  " + exception.getMessage());
        } catch (IllegalStateException exception) {
            System.out.println("⚠️  Could not save tasks.");
        }
        System.out.println();
    }

    private static void completeTask(Scanner scanner, User user, TaskService taskService) {
        int taskIndex = readTaskIndex(scanner, user, "Complete task number: ");

        if (taskIndex < 0) {
            return;
        }

        try {
            if (taskService.completeTask(user, taskIndex)) {
                System.out.println("✅ Task completed.");
            } else {
                System.out.println("ℹ️  This task is already complete.");
            }
        } catch (IllegalStateException exception) {
            System.out.println("⚠️  Could not save tasks.");
        }

        System.out.println();
    }

    private static void removeTask(Scanner scanner, User user, TaskService taskService) {
        int taskIndex = readTaskIndex(scanner, user, "Remove task number: ");

        if (taskIndex < 0) {
            return;
        }

        try {
            taskService.removeTask(user, taskIndex);
            System.out.println("🗑️  Task removed.");
        } catch (IllegalStateException exception) {
            System.out.println("⚠️  Could not save tasks.");
        }
        System.out.println();
    }

    private static int readTaskIndex(Scanner scanner, User user, String prompt) {
        List<Task> tasks = user.getTasks();

        if (tasks.isEmpty()) {
            System.out.println("⚠️  There are no tasks to select.");
            System.out.println();
            return -1;
        }

        System.out.print(prompt);


        if (!scanner.hasNextLine()) {
            return -1;
        }

        try {
            int taskNumber = Integer.parseInt(scanner.nextLine().trim());
            // The UI starts numbering at 1, while Java lists start at 0.
            int taskIndex = taskNumber - 1;

            if (taskIndex >= 0 && taskIndex < tasks.size()) {
                return taskIndex;
            }
        } catch (NumberFormatException exception) {
        }

        System.out.println("⚠️  Choose a valid task number.");
        System.out.println();
        return -1;
    }
}
