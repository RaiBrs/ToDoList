package com.raibrs.todolist.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.raibrs.todolist.model.Task;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class JsonTaskStorage implements TaskStorage {

    private final Path filePath;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonTaskStorage(Path filePath) {
        this.filePath = filePath;
    }

    @Override
    public List<Task> loadTasks(String username) {
        if (Files.notExists(filePath)) {
            return List.of();
        }

        try {
            JsonNode root = objectMapper.readTree(filePath.toFile());
            if (root == null || !root.isObject()) {
                throw invalidFile("the root must be a JSON object");
            }

            JsonNode savedTasks = root.get(username);

            if (savedTasks == null) {
                return List.of();
            }
            if (!savedTasks.isArray()) {
                throw invalidFile("tasks for " + username + " must be a JSON array");
            }

            List<Task> tasks = new ArrayList<>();

            for (JsonNode savedTask : savedTasks) {
                if (!savedTask.isObject()) {
                    throw invalidFile("each task must be a JSON object");
                }

                JsonNode titleNode = savedTask.get("title");
                JsonNode completedNode = savedTask.get("completed");
                if (titleNode == null || !titleNode.isTextual() || titleNode.asText().isBlank()) {
                    throw invalidFile("each task must have a non-blank string title");
                }
                if (completedNode == null || !completedNode.isBoolean()) {
                    throw invalidFile("each task must have a boolean completed value");
                }

                tasks.add(Task.restore(titleNode.asText(), completedNode.asBoolean()));
            }

            return tasks;
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read saved task file " + filePath + ".", exception);
        }
    }

    @Override
    public void saveTasks(String username, List<Task> tasks) {
        try {
            Path absoluteFilePath = filePath.toAbsolutePath();
            Path parentDirectory = absoluteFilePath.getParent();

            Files.createDirectories(parentDirectory);

            // Read the full document so saving one account preserves every other task list.
            JsonNode savedRoot = Files.exists(absoluteFilePath)
                    ? objectMapper.readTree(absoluteFilePath.toFile())
                    : objectMapper.createObjectNode();
            if (savedRoot == null || !savedRoot.isObject()) {
                throw invalidFile("the root must be a JSON object");
            }
            ObjectNode root = (ObjectNode) savedRoot;

            root.set(username, objectMapper.valueToTree(tasks));

            // Keep the temporary file beside the original so replacement can be atomic.
            Path temporaryFile = Files.createTempFile(parentDirectory, "tasks-", ".tmp");
            try {
                objectMapper.writerWithDefaultPrettyPrinter()
                        .writeValue(temporaryFile.toFile(), root);

                // Replace the original only after the complete JSON has been written.
                Files.move(temporaryFile, absoluteFilePath,
                        StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } finally {
                Files.deleteIfExists(temporaryFile);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Could not safely save tasks to " + filePath + ".", exception);
        }
    }

    private IllegalStateException invalidFile(String reason) {
        return new IllegalStateException("Invalid saved task file " + filePath + ": " + reason + ".");
    }
}
