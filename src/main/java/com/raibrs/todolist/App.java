package com.raibrs.todolist;

import com.raibrs.todolist.model.User;
import com.raibrs.todolist.repository.JsonTaskStorage;
import com.raibrs.todolist.repository.TaskStorage;
import com.raibrs.todolist.repository.UserRepository;
import com.raibrs.todolist.service.TaskService;
import com.raibrs.todolist.ui.ConsoleUi;
import java.nio.file.Path;

public class App {
    public static void main(String[] args) {
        UserRepository userRepository = new UserRepository();
        TaskStorage taskStorage = new JsonTaskStorage(Path.of("data", "tasks.json"));
        TaskService taskService = new TaskService(taskStorage);
        User demoUser = new User("rai", "1234");

        // Restore the demo user's saved tasks before the terminal starts.
        taskService.loadTasks(demoUser);

        userRepository.addUser(demoUser);
        ConsoleUi.run(userRepository, taskService);
    }
}
