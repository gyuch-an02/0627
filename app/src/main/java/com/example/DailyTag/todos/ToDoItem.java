package com.example.DailyTag.todos;

import java.util.HashSet;
import java.util.Set;

public class ToDoItem {
    private String task;
    private boolean done;
    private Set<String> tags;

    public ToDoItem(String task, boolean done) {
        this.task = task;
        this.done = done;
        this.tags = new HashSet<>();
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }
}
