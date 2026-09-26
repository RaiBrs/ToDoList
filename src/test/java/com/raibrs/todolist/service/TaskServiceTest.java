package com.raibrs.todolist.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.raibrs.todolist.model.Task;
import com.raibrs.todolist.model.User;
import com.raibrs.todolist.repository.TaskStorage;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class TaskServiceTest {

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

    // Completing a task keeps the completed state in storage.
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

    private static class RecordingTaskStorage implements TaskStorage {
        private String savedUsername;
        private List<Task> savedTasks = List.of();

        @Override
        public List<Task> loadTasks(String username) {
            return List.of();
        }

        @Override
        public void saveTasks(String username, List<Task> tasks) {
            savedUsername = username;
            savedTasks = new ArrayList<>(tasks);
        }
    }
}
