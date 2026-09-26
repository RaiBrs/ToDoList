package com.todolist.tests;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.todolist.Task;
import org.junit.jupiter.api.Test;

class TaskTest {

    // A new task starts incomplete.
    @Test
    void newTaskStartsIncomplete() {
        Task task = new Task("Study Java");

        assertFalse(task.isCompleted());
    }

    // Blank titles are invalid.
    @Test
    void blankTitleIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Task("   "));
    }
}
