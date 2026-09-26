package com.todolist.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.todolist.Task;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;

public class JsonTaskStorage implements TaskStorage {

    private final Path filePath;

    // Converts Java objects to and from JSON.
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonTaskStorage(Path filePath) {
        this.filePath = filePath;
    }

    @Override
    public List<Task> loadTasks(String username) {
        // New users do not have a storage file yet.
        if (Files.notExists(filePath)) {
            return List.of();
        }

        try {
            // Load the JSON root containing all users.
            JsonNode root = objectMapper.readTree(filePath.toFile());
            JsonNode savedTasks = root.get(username);

            // Users without saved tasks receive an empty list.
            if (savedTasks == null || !savedTasks.isArray()) {
                return List.of();
            }

            List<Task> tasks = new ArrayList<>();

            // Rebuild each saved task using its title and status.
            for (JsonNode savedTask : savedTasks) {
                String title = savedTask.get("title").asText();
                boolean completed = savedTask.get("completed").asBoolean();

                tasks.add(Task.restore(title, completed));
            }

            return tasks;
        } catch (IOException exception) {
            throw new IllegalStateException("Could not load tasks.", exception);
        }
    }

    @Override
    public void saveTasks(String username, List<Task> tasks) {
        try {
            Path parentDirectory = filePath.getParent();

            // Ensure the storage directory exists before writing the file.
            if (parentDirectory != null) {
                Files.createDirectories(parentDirectory);
            }

            // Keep task data already saved for other users.
            ObjectNode root = Files.exists(filePath)
                    ? (ObjectNode) objectMapper.readTree(filePath.toFile())
                    : objectMapper.createObjectNode();

            // Replace only the current user's task list.
            root.set(username, objectMapper.valueToTree(tasks));

            // Write readable JSON to the storage file.
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(filePath.toFile(), root);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not save tasks.", exception);
        }
    }
}
