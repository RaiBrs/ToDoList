package com.raibrs.todolist;

import com.raibrs.todolist.repository.JsonTaskStorage;
import com.raibrs.todolist.repository.JsonUserStorage;
import com.raibrs.todolist.repository.SessionStorage;
import com.raibrs.todolist.repository.TaskStorage;
import com.raibrs.todolist.repository.UserRepository;
import com.raibrs.todolist.service.TaskService;
import com.raibrs.todolist.ui.ConsoleUi;
import java.nio.file.Path;

public class App {
    public static void main(String[] args) {
        UserRepository userRepository;
        try {
            userRepository = new UserRepository(new JsonUserStorage(Path.of("data", "users.json")));
        } catch (IllegalStateException exception) {
            // Do not start with an empty account list when the file is invalid; that could overwrite it.
            System.err.println("Could not load saved users: " + exception.getMessage());
            System.err.println("The user file was left unchanged. Back it up or fix it before restarting.");
            return;
        }

        TaskStorage taskStorage = new JsonTaskStorage(Path.of("data", "tasks.json"));
        TaskService taskService = new TaskService(taskStorage);
        SessionStorage sessionStorage = new SessionStorage(Path.of("data", "session.txt"));
        ConsoleUi.run(userRepository, taskService, sessionStorage);
    }
}
