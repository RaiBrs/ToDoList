package com.raibrs.todolist.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class UserTest {

    // Usernames are formatted consistently.
    @Test
    void usernameIsNormalized() {
        User user = new User("  Rai.Dev  ", "1234");

        assertEquals("@rai.dev", user.getUsername());
    }

    // Null tasks are invalid.
    @Test
    void nullTaskIsRejected() {
        User user = new User("rai", "1234");

        assertThrows(IllegalArgumentException.class, () -> user.addTask(null));
    }

    // Task lists cannot be changed externally.
    @Test
    void taskListIsImmutableOutsideUser() {
        User user = new User("rai", "1234");
        user.addTask(new Task("Study Java"));
        List<Task> tasks = user.getTasks();

        assertThrows(UnsupportedOperationException.class, tasks::clear);
        assertEquals(1, user.getTasks().size());
    }
}
