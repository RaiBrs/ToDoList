package com.raibrs.todolist.service;

import com.raibrs.todolist.model.Task;
import com.raibrs.todolist.model.User;
import com.raibrs.todolist.repository.TaskStorage;
import java.util.ArrayList;
import java.util.List;

public class TaskService {
    private final TaskStorage taskStorage;

    public TaskService(TaskStorage taskStorage) {
        if (taskStorage == null) {
            throw new IllegalArgumentException("Task storage is required.");
        }

        this.taskStorage = taskStorage;
    }

    public void loadTasks(User user) {
        user.replaceTasks(taskStorage.loadTasks(user.getUsername()));
    }

    public void addTask(User user, String title) {
        Task task = new Task(title);
        List<Task> updatedTasks = copyTasks(user);
        updatedTasks.add(task);

        // Persist first so a failed save leaves the user's in-memory state unchanged.
        saveTasks(user, updatedTasks);
        user.addTask(task);
    }

    public boolean completeTask(User user, int taskIndex) {
        Task task = user.getTask(taskIndex);

        if (task.isCompleted()) {
            return false;
        }

        List<Task> updatedTasks = copyTasks(user);
        updatedTasks.get(taskIndex).complete();

        // Task is mutable, so complete the copy and leave the live object unchanged until saving succeeds.
        saveTasks(user, updatedTasks);
        task.complete();
        return true;
    }

    public void removeTask(User user, int taskIndex) {
        // Validate the index before saving a list without the selected task.
        user.getTask(taskIndex);
        List<Task> updatedTasks = copyTasks(user);
        updatedTasks.remove(taskIndex);

        saveTasks(user, updatedTasks);
        user.removeTask(taskIndex);
    }

    private List<Task> copyTasks(User user) {
        List<Task> copy = new ArrayList<>();
        for (Task task : user.getTasks()) {
            copy.add(Task.restore(task.getTitle(), task.isCompleted()));
        }
        return copy;
    }

    private void saveTasks(User user, List<Task> tasks) {
        taskStorage.saveTasks(user.getUsername(), tasks);
    }
}
