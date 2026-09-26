package com.todolist.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.todolist.Task;
import com.todolist.repository.JsonTaskStorage;
import com.todolist.repository.TaskStorage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JsonTaskStorageTest {

    @TempDir
    Path temporaryDirectory;

    // Missing files load as an empty task list.
    @Test
    void missingFileLoadsEmptyTaskList() {
        Path filePath = temporaryDirectory.resolve("tasks.json");
        TaskStorage storage = new JsonTaskStorage(filePath);

        assertTrue(storage.loadTasks("@rai").isEmpty());
    }

    // Saved tasks keep their title and status in JSON.
    @Test
    void savesTasksForUserAsJson() throws IOException {
        Path filePath = temporaryDirectory.resolve("tasks.json");
        TaskStorage storage = new JsonTaskStorage(filePath);

        storage.saveTasks("@rai", List.of(new Task("Study Java")));

        JsonNode savedTasks = new ObjectMapper().readTree(filePath.toFile()).get("@rai");
        assertEquals("Study Java", savedTasks.get(0).get("title").asText());
        assertFalse(savedTasks.get(0).get("completed").asBoolean());
    }

    // Loaded tasks keep their completion status.
    @Test
    void loadsSavedTasksWithCompletionStatus() {
        Path filePath = temporaryDirectory.resolve("tasks.json");
        TaskStorage storage = new JsonTaskStorage(filePath);

        storage.saveTasks("@rai", List.of(Task.restore("Study Java", true)));

        List<Task> loadedTasks = storage.loadTasks("@rai");
        assertEquals(1, loadedTasks.size());
        assertEquals("Study Java", loadedTasks.get(0).getTitle());
        assertTrue(loadedTasks.get(0).isCompleted());
    }
}
