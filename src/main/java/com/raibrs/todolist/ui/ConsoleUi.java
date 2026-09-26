package com.raibrs.todolist.ui;

import com.raibrs.todolist.model.Task;
import com.raibrs.todolist.model.User;
import com.raibrs.todolist.repository.UserRepository;
import com.raibrs.todolist.service.TaskService;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public final class ConsoleUi {
    private ConsoleUi() {
    }

    public static void run(UserRepository userRepository, TaskService taskService) {
        Scanner scanner = new Scanner(System.in);
        showWelcome();

        // Logging out returns the user to the sign-in screen.
        while (true) {
            User user = login(scanner, userRepository);

            if (user == null) {
                System.out.println("Goodbye! 👋");
                return;
            }

            showLoginSuccess(user);
            runTaskMenu(scanner, user, taskService);
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

    private static User login(Scanner scanner, UserRepository userRepository) {

        // Keep asking until authentication succeeds or the input stream ends.
        while (true) {
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
                // The repository normalizes the entered username before searching.
                User user = userRepository.findByUsername(username);

                if (user.matchesPassword(password)) {
                    return user;
                }
            } catch (IllegalArgumentException exception) {
                // Avoid revealing whether the username or password is invalid.
            }

            System.out.println();
            System.out.println("⚠️  Invalid username or password. Try again.");
            System.out.println();
        }
    }

    private static void showLoginSuccess(User user) {
        System.out.println();
        System.out.println("────────────────────────────────────");
        System.out.println("✅ Login successful!");
        System.out.println("👋 Welcome back, " + user.getUsername() + ".");
        System.out.println("────────────────────────────────────");
        System.out.println();
    }

    private static void runTaskMenu(Scanner scanner, User user, TaskService taskService) {
        while (true) {

            // Redraw the current task state after every action.
            showTasks(user);
            showMenu();

            // Stop the task menu when terminal input ends.
            if (!scanner.hasNextLine()) {
                return;
            }

            String option = scanner.nextLine().trim().toUpperCase(Locale.ROOT);

            switch (option) {
                case "A":
                    addTask(scanner, user, taskService);
                    break;
                case "C":
                    completeTask(scanner, user, taskService);
                    break;
                case "R":
                    removeTask(scanner, user, taskService);
                    break;
                case "L":
                    System.out.println();
                    System.out.println("👋 Logged out.");
                    System.out.println();
                    return;
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
                // The terminal shows one-based numbers, while the list uses zero-based indexes.
                String status = task.isCompleted() ? "✓" : " ";
                System.out.printf("%d. [%s] %s%n", index + 1, status, task.getTitle());
            }
        }

        System.out.println();
    }

    private static void showMenu() {
        System.out.println("[A] Add task");
        System.out.println("[C] Complete task");
        System.out.println("[R] Remove task");
        System.out.println("[L] Logout");
        System.out.print("Choose an option: ");
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
            // Convert the number shown to the user into the list index.
            int taskIndex = taskNumber - 1;

            if (taskIndex >= 0 && taskIndex < tasks.size()) {
                return taskIndex;
            }
        } catch (NumberFormatException exception) {
            // Invalid numeric input falls through to the shared validation message.
        }

        System.out.println("⚠️  Choose a valid task number.");
        System.out.println();
        return -1;
    }
}
