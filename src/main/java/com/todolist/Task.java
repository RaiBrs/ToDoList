package com.todolist;

public class Task {
    private final String title;
    private boolean completed;

    private Task(String title, boolean completed) {
        // Title cannot be null or blank
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be null or empty.");
        }

        this.title = title;
        this.completed = completed;
    }

    public Task(String title) {
        this(title, false);
    }

    public static Task restore(String title, boolean completed) {
        return new Task(title, completed);
    }

    public String getTitle() {
        return title;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void complete() {
        this.completed = true;
    }
}
