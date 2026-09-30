package com.raibrs.todolist.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.raibrs.todolist.model.Task;
import com.raibrs.todolist.model.User;
import com.raibrs.todolist.repository.JsonTaskStorage;
import com.raibrs.todolist.repository.TaskStorage;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TaskServiceTest {

    @TempDir
    Path temporaryDirectory;

    // Adding a task also saves the user's updated list.
    @Test
    void addTaskSavesUpdatedTaskList() {
        RecordingTaskStorage storage = new RecordingTaskStorage();
        TaskService service = new TaskService(storage);
        User user = new User("rai", "1234");

        service.addTask(user, "Study Java");

        assertEquals("@rai", storage.savedUsername);
        assertEquals(1, storage.savedTasks.size());
        assertEquals("Study Java", storage.savedTasks.get(0).getTitle());
    }

    // A failed save must not add the task to the in-memory list.
    @Test
    void addTaskDoesNotChangeUserWhenSaveFails() {
        RecordingTaskStorage storage = new RecordingTaskStorage();
        storage.failOnSave = true;
        TaskService service = new TaskService(storage);
        User user = new User("rai", "1234");

        assertThrows(IllegalStateException.class, () -> service.addTask(user, "Study Java"));

        assertTrue(user.getTasks().isEmpty());
    }

    // Completing a task persists its new state.
    @Test
    void completeTaskSavesCompletedStatus() {
        RecordingTaskStorage storage = new RecordingTaskStorage();
        TaskService service = new TaskService(storage);
        User user = new User("rai", "1234");
        user.addTask(new Task("Study Java"));

        boolean completed = service.completeTask(user, 0);

        assertTrue(completed);
        assertTrue(storage.savedTasks.get(0).isCompleted());
    }

    // A failed save leaves the original task incomplete.
    @Test
    void completeTaskDoesNotChangeUserWhenSaveFails() {
        RecordingTaskStorage storage = new RecordingTaskStorage();
        TaskService service = new TaskService(storage);
        User user = new User("rai", "1234");
        user.addTask(new Task("Study Java"));
        storage.failOnSave = true;

        assertThrows(IllegalStateException.class, () -> service.completeTask(user, 0));

        assertFalse(user.getTask(0).isCompleted());
    }

    // A failed save leaves the task in the in-memory list.
    @Test
    void removeTaskDoesNotChangeUserWhenSaveFails() {
        RecordingTaskStorage storage = new RecordingTaskStorage();
        TaskService service = new TaskService(storage);
        User user = new User("rai", "1234");
        user.addTask(new Task("Study Java"));
        storage.failOnSave = true;

        assertThrows(IllegalStateException.class, () -> service.removeTask(user, 0));

        assertEquals(1, user.getTasks().size());
        assertEquals("Study Java", user.getTask(0).getTitle());
    }

    // Each username loads only the list saved under its key.
    @Test
    void taskListsAreIsolatedByUsername() {
        TaskService service = new TaskService(
                new JsonTaskStorage(temporaryDirectory.resolve("tasks.json")));
        User firstUser = new User("first", "secret");
        User secondUser = new User("second", "secret");
        service.addTask(firstUser, "First user's task");
        service.addTask(secondUser, "Second user's task");

        User restoredFirstUser = new User("first", "secret");
        User restoredSecondUser = new User("second", "secret");
        service.loadTasks(restoredFirstUser);
        service.loadTasks(restoredSecondUser);

        assertEquals("First user's task", restoredFirstUser.getTask(0).getTitle());
        assertEquals("Second user's task", restoredSecondUser.getTask(0).getTitle());
        assertEquals(1, restoredFirstUser.getTasks().size());
        assertEquals(1, restoredSecondUser.getTasks().size());
    }

    private static class RecordingTaskStorage implements TaskStorage {
        private String savedUsername;
        private List<Task> savedTasks = List.of();
        private boolean failOnSave;

        @Override
        public List<Task> loadTasks(String username) {
            return List.of();
        }

        @Override
        public void saveTasks(String username, List<Task> tasks) {
            if (failOnSave) {
                throw new IllegalStateException("Simulated storage failure.");
            }

            savedUsername = username;
            savedTasks = new ArrayList<>(tasks);
        }
    }
}
