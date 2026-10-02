package com.raibrs.todolist.ui;

import com.raibrs.todolist.model.Task;
import com.raibrs.todolist.model.User;
import com.raibrs.todolist.service.TaskService;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/** Owns task prompts and returns navigation outcomes to the session coordinator. */
final class TaskMenu {
    enum Result {
        LOGOUT,
        QUIT
    }

    private final Scanner scanner;
    private final TaskService taskService;
    private final AccountMenu accountMenu;

    TaskMenu(Scanner scanner, TaskService taskService, AccountMenu accountMenu) {
        this.scanner = scanner;
        this.taskService = taskService;
        this.accountMenu = accountMenu;
    }

    Result run(User user) {
        while (true) {
            showTasks(user);
            showMenu();

            if (!scanner.hasNextLine()) {
                return Result.QUIT;
            }

            String option = scanner.nextLine().trim().toUpperCase(Locale.ROOT);

            switch (option) {
                case "A":
                    addTask(user);
                    break;
                case "E":
                    editTask(user);
                    break;
                case "C":
                    completeTask(user);
                    break;
                case "R":
                    removeTask(user);
                    break;
                case "P":
                    // Keep the active session on the account object with the new password hash.
                    user = accountMenu.changePassword(user);
                    break;
                case "L":
                    return Result.LOGOUT;
                case "Q":
                    return Result.QUIT;
                default:
                    System.out.println();
                    System.out.println("⚠️  Invalid option. Try again.");
                    System.out.println();
            }
        }
    }

    private void showTasks(User user) {
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

    private void showMenu() {
        System.out.println("[A] Add task");
        System.out.println("[E] Edit task title");
        System.out.println("[C] Complete task");
        System.out.println("[R] Remove task");
        System.out.println("[P] Change password");
        System.out.println("[L] Logout");
        System.out.println("[Q] Quit and keep session");
        System.out.print("Choose an option: ");
    }

    private void addTask(User user) {
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

    private void editTask(User user) {
        int taskIndex = readTaskIndex(user, "Edit task number: ");
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

    private void completeTask(User user) {
        int taskIndex = readTaskIndex(user, "Complete task number: ");
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

    private void removeTask(User user) {
        int taskIndex = readTaskIndex(user, "Remove task number: ");

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

    private int readTaskIndex(User user, String prompt) {
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
            // Show the same validation message for text and out-of-range numbers.
        }

        System.out.println("⚠️  Choose a valid task number.");
        System.out.println();
        return -1;
    }
}
