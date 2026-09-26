package com.raibrs.todolist.service;

import com.raibrs.todolist.model.Task;
import com.raibrs.todolist.model.User;
import com.raibrs.todolist.repository.TaskStorage;

public class TaskService {
    private final TaskStorage taskStorage;

    public TaskService(TaskStorage taskStorage) {
        if (taskStorage == null) {
            throw new IllegalArgumentException("Task storage is required.");
        }

        this.taskStorage = taskStorage;
    }

    public void loadTasks(User user) {
        for (Task task : taskStorage.loadTasks(user.getUsername())) {
            user.addTask(task);
        }
    }

    public void addTask(User user, String title) {
        user.addTask(new Task(title));
        saveTasks(user);
    }

    public boolean completeTask(User user, int taskIndex) {
        Task task = user.getTask(taskIndex);

        if (task.isCompleted()) {
            return false;
        }

        task.complete();
        saveTasks(user);
        return true;
    }

    public void removeTask(User user, int taskIndex) {
        user.removeTask(taskIndex);
        saveTasks(user);
    }

    private void saveTasks(User user) {
        taskStorage.saveTasks(user.getUsername(), user.getTasks());
    }
}
