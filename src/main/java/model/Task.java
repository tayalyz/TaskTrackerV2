package model;

import utils.Identifier;

public class Task {
    protected int id;
    protected String title;
    protected String description;
    protected Status status;
    protected Type type;

    public Task(String title, String description) {
        this.id = Identifier.INSTANCE.generate();
        this.type = Type.TASK;
        this.status = Status.NEW;
        this.title = title;
        this.description = description;
    }

    public Task(String description, String title, Status status) {

        this.description = description;
        this.title = title;
        this.status = status;
    }

    public Task(int id, String title, String description, Status status, Type type) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.type = type;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Task{" +
                "type=" + type +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", id=" + id +
                '}';
    }
}
