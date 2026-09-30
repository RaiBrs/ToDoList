package com.raibrs.todolist.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class UserTest {

    // Usernames are normalized consistently.
    @Test
    void usernameIsNormalized() {
        User user = new User("  Rai.Dev  ", "1234");

        assertEquals("@rai.dev", user.getUsername());
    }

    // Null tasks are rejected.
    @Test
    void nullTaskIsRejected() {
        User user = new User("rai", "1234");

        assertThrows(IllegalArgumentException.class, () -> user.addTask(null));
    }

    // Callers cannot modify the returned task list.
    @Test
    void taskListIsImmutableOutsideUser() {
        User user = new User("rai", "1234");
        user.addTask(new Task("Study Java"));
        List<Task> tasks = user.getTasks();

        assertThrows(UnsupportedOperationException.class, tasks::clear);
        assertEquals(1, user.getTasks().size());
    }

    // The correct password is verified without storing its plain text in the user.
    @Test
    void passwordMatchesOnlyWhenCorrect() {
        User user = new User("rai", "1234");

        assertTrue(user.matchesPassword("1234"));
        assertFalse(user.matchesPassword("wrong-password"));
        assertFalse(user.matchesPassword(null));
    }
}
