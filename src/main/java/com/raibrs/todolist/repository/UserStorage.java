package com.raibrs.todolist.repository;

import com.raibrs.todolist.model.User;
import java.util.List;

/** Contract for loading and saving accounts without coupling the repository to file storage. */
public interface UserStorage {
    List<User> loadUsers();

    void saveUsers(List<User> users);
}
