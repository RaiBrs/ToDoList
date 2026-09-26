package com.raibrs.todolist.repository;

import com.raibrs.todolist.model.Task;
import java.util.List;

public interface TaskStorage {
    List<Task> loadTasks(String username);

    void saveTasks(String username, List<Task> tasks);
}
