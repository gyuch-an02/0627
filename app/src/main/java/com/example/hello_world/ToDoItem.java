package com.example.hello_world;

public class ToDoItem {
    private String task;
    private boolean done;
    private long timestamp; // new field for the creation timestamp

    public ToDoItem(String task, boolean done) {
        this.task = task;
        this.done = done;
        this.timestamp = System.currentTimeMillis(); // set creation time when item is created
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

    public long getTimestamp() {
        return timestamp;
    }
}
