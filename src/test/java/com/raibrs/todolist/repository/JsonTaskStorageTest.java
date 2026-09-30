package com.raibrs.todolist.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raibrs.todolist.model.Task;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JsonTaskStorageTest {

    @TempDir
    Path temporaryDirectory;

    // A missing file means the user has no saved tasks yet.
    @Test
    void missingFileLoadsEmptyTaskList() {
        Path filePath = temporaryDirectory.resolve("tasks.json");
        TaskStorage storage = new JsonTaskStorage(filePath);

        assertTrue(storage.loadTasks("@rai").isEmpty());
    }

    // The saved JSON preserves the task title and completion state.
    @Test
    void savesTasksForUserAsJson() throws IOException {
        Path filePath = temporaryDirectory.resolve("tasks.json");
        TaskStorage storage = new JsonTaskStorage(filePath);

        storage.saveTasks("@rai", List.of(new Task("Study Java")));

        JsonNode savedTasks = new ObjectMapper().readTree(filePath.toFile()).get("@rai");
        assertEquals("Study Java", savedTasks.get(0).get("title").asText());
        assertFalse(savedTasks.get(0).get("completed").asBoolean());
    }

    // Replacing the file leaves complete JSON and removes the temporary file.
    @Test
    void safelyReplacesExistingTaskFile() throws IOException {
        Path filePath = temporaryDirectory.resolve("tasks.json");
        TaskStorage storage = new JsonTaskStorage(filePath);
        storage.saveTasks("@rai", List.of(new Task("Old task")));

        storage.saveTasks("@rai", List.of(new Task("New task")));

        JsonNode root = new ObjectMapper().readTree(filePath.toFile());
        assertEquals("New task", root.get("@rai").get(0).get("title").asText());
        try (Stream<Path> files = Files.list(temporaryDirectory)) {
            assertEquals(1, files.count());
        }
    }

    // Loading preserves the completion state.
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

    // An empty file is invalid; it does not mean the user has no tasks.
    @Test
    void emptyFileIsReportedAsInvalidAndLeftUnchanged() throws IOException {
        Path filePath = temporaryDirectory.resolve("tasks.json");
        Files.writeString(filePath, "");
        TaskStorage storage = new JsonTaskStorage(filePath);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class, () -> storage.loadTasks("@rai"));

        assertTrue(exception.getMessage().contains("Invalid saved task file"));
        assertEquals("", Files.readString(filePath));
    }

    // Fields with unexpected types are rejected.
    @Test
    void taskWithInvalidFieldsIsReported() throws IOException {
        Path filePath = temporaryDirectory.resolve("tasks.json");
        Files.writeString(filePath, "{\"@rai\":[{\"title\":\"Study Java\",\"completed\":\"no\"}]}");
        TaskStorage storage = new JsonTaskStorage(filePath);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class, () -> storage.loadTasks("@rai"));

        assertTrue(exception.getMessage().contains("boolean completed value"));
    }

    // Invalid JSON syntax produces a readable storage error.
    @Test
    void malformedJsonIsReported() throws IOException {
        Path filePath = temporaryDirectory.resolve("tasks.json");
        Files.writeString(filePath, "{ invalid json");
        TaskStorage storage = new JsonTaskStorage(filePath);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class, () -> storage.loadTasks("@rai"));

        assertTrue(exception.getMessage().contains("Could not read saved task file"));
    }
}
