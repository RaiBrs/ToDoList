package com.raibrs.todolist.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.raibrs.todolist.model.Task;
import com.raibrs.todolist.model.User;
import com.raibrs.todolist.repository.SessionStorage;
import com.raibrs.todolist.repository.TaskStorage;
import com.raibrs.todolist.repository.UserRepository;
import com.raibrs.todolist.repository.UserStorage;
import com.raibrs.todolist.service.TaskService;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ConsoleUiTest {

    @TempDir
    Path temporaryDirectory;

    // The console changes the password, accepts it on a new login, and edits a task title.
    @Test
    void userCanChangePasswordAndEditTaskAfterSigningIn() {
        String input = String.join(System.lineSeparator(),
                "N", "rai", "OldPass8", "OldPass8",
                "P", "OldPass8", "Different9", "Different8",
                "P", "OldPass8", "NewPass9", "NewPass9",
                "L", "L", "rai", "NewPass9",
                "A", "Draft title", "E", "1", "Final title", "L", "Q", "");
        UserRepository userRepository = new UserRepository(new InMemoryUserStorage());
        InMemoryTaskStorage taskStorage = new InMemoryTaskStorage();
        SessionStorage sessionStorage = new SessionStorage(temporaryDirectory.resolve("session.txt"));

        String output = runConsole(input, userRepository, taskStorage, sessionStorage);

        assertTrue(output.contains("Passwords do not match."));
        assertTrue(output.contains("Password changed."), output);
        assertTrue(output.contains("Login successful!"));
        assertTrue(output.contains("Task title updated."));

        User user = userRepository.findByUsername("rai");
        assertFalse(user.matchesPassword("OldPass8"));
        assertTrue(user.matchesPassword("NewPass9"));
        assertEquals("Final title", taskStorage.loadTasks("@rai").get(0).getTitle());
        assertTrue(sessionStorage.loadUsername().isEmpty());
    }

    // Quitting preserves the session so the next run can resume without credentials.
    @Test
    void savedSessionResumesWithoutAskingForCredentials() {
        UserRepository userRepository = new UserRepository(new InMemoryUserStorage());
        InMemoryTaskStorage taskStorage = new InMemoryTaskStorage();
        TaskService taskService = new TaskService(taskStorage);
        SessionStorage sessionStorage = new SessionStorage(temporaryDirectory.resolve("session.txt"));

        runConsole("N\nrai\nOldPass8\nOldPass8\nQ\n", userRepository, taskService, sessionStorage);
        assertEquals("rai", sessionStorage.loadUsername().orElseThrow());

        String resumedOutput = runConsole("L\nQ\n", userRepository, taskService, sessionStorage);

        assertTrue(resumedOutput.contains("Resuming saved session for @rai."));
        assertFalse(resumedOutput.contains("Sign in"));
        assertTrue(sessionStorage.loadUsername().isEmpty());
    }

    private String runConsole(String input, UserRepository userRepository,
                              InMemoryTaskStorage taskStorage, SessionStorage sessionStorage) {
        return runConsole(input, userRepository, new TaskService(taskStorage), sessionStorage);
    }

    private String runConsole(String input, UserRepository userRepository,
                              TaskService taskService, SessionStorage sessionStorage) {
        InputStream originalInput = System.in;
        PrintStream originalOutput = System.out;
        ByteArrayOutputStream capturedOutput = new ByteArrayOutputStream();

        try (PrintStream testOutput = new PrintStream(capturedOutput, true, StandardCharsets.UTF_8)) {
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            System.setOut(testOutput);
            ConsoleUi.run(userRepository, taskService, sessionStorage);
            return capturedOutput.toString(StandardCharsets.UTF_8);
        } finally {
            System.setIn(originalInput);
            System.setOut(originalOutput);
        }
    }

    private static class InMemoryUserStorage implements UserStorage {
        private List<User> users = List.of();

        @Override
        public List<User> loadUsers() {
            return users;
        }

        @Override
        public void saveUsers(List<User> users) {
            this.users = new ArrayList<>(users);
        }
    }

    private static class InMemoryTaskStorage implements TaskStorage {
        private final Map<String, List<Task>> tasksByUsername = new HashMap<>();

        @Override
        public List<Task> loadTasks(String username) {
            return tasksByUsername.getOrDefault(username, List.of());
        }

        @Override
        public void saveTasks(String username, List<Task> tasks) {
            tasksByUsername.put(username, new ArrayList<>(tasks));
        }
    }
}
