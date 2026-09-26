package com.todolist.repository;

import com.todolist.Task;
import java.util.List;

public interface TaskStorage {
    List<Task> loadTasks(String username);

    void saveTasks(String username, List<Task> tasks);
}
