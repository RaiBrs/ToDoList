package com.todolist;

import com.todolist.repository.JsonTaskStorage;
import com.todolist.repository.TaskStorage;
import com.todolist.repository.UserRepository;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        UserRepository userRepository = new UserRepository();

        // Store task data locally outside the source code.
        TaskStorage taskStorage = new JsonTaskStorage(Path.of("data", "tasks.json"));

        User demoUser = new User("rai", "1234");

        // Restore saved tasks once before the login loop starts.
        for (Task task : taskStorage.loadTasks(demoUser.getUsername())) {
            demoUser.addTask(task);
        }

        userRepository.addUser(demoUser);

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
            runTaskMenu(scanner, user, taskStorage);
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

    private static void runTaskMenu(Scanner scanner, User user, TaskStorage taskStorage) {
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
                    addTask(scanner, user, taskStorage);
                    break;
                case "C":
                    completeTask(scanner, user, taskStorage);
                    break;
                case "R":
                    removeTask(scanner, user, taskStorage);
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

    private static void addTask(Scanner scanner, User user, TaskStorage taskStorage) {
        System.out.println();
        System.out.print("Task title: ");

        if (!scanner.hasNextLine()) {
            return;
        }

        String title = scanner.nextLine();

        try {
            // Task validates its own title before the user receives it.
            user.addTask(new Task(title));

            // Save the updated list before confirming the action.
            if (persistTasks(user, taskStorage)) {
                System.out.println("✅ Task added.");
            }
        } catch (IllegalArgumentException exception) {
            System.out.println("⚠️  " + exception.getMessage());
        }

        System.out.println();
    }

    private static void completeTask(Scanner scanner, User user, TaskStorage taskStorage) {
        int taskIndex = readTaskIndex(scanner, user, "Complete task number: ");

        if (taskIndex < 0) {
            return;
        }

        Task task = user.getTask(taskIndex);

        if (task.isCompleted()) {
            System.out.println("ℹ️  This task is already complete.");
        } else {
            task.complete();

            // Save the new completion status before confirming the action.
            if (persistTasks(user, taskStorage)) {
                System.out.println("✅ Task completed.");
            }
        }

        System.out.println();
    }

    private static void removeTask(Scanner scanner, User user, TaskStorage taskStorage) {
        int taskIndex = readTaskIndex(scanner, user, "Remove task number: ");

        if (taskIndex < 0) {
            return;
        }

        user.removeTask(taskIndex);

        // Save the list without the removed task.
        if (persistTasks(user, taskStorage)) {
            System.out.println("🗑️  Task removed.");
        }
        System.out.println();
    }

    private static boolean persistTasks(User user, TaskStorage taskStorage) {
        try {
            taskStorage.saveTasks(user.getUsername(), user.getTasks());
            return true;
        } catch (IllegalStateException exception) {
            // Keep the application running when the storage file cannot be updated.
            System.out.println("⚠️  Could not save tasks.");
            return false;
        }
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
