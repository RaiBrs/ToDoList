package com.raibrs.todolist.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TaskTest {

    // A new task starts incomplete.
    @Test
    void newTaskStartsIncomplete() {
        Task task = new Task("Study Java");

        assertFalse(task.isCompleted());
    }

    // Restored tasks keep their completion status.
    @Test
    void restoredTaskKeepsCompletionStatus() {
        Task task = Task.restore("Study Java", true);

        assertTrue(task.isCompleted());
    }

    // Restored tasks can remain incomplete.
    @Test
    void restoredTaskCanRemainIncomplete() {
        Task task = Task.restore("Study Java", false);

        assertFalse(task.isCompleted());
    }

    // Blank titles are invalid.
    @Test
    void blankTitleIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Task("   "));
    }
}
