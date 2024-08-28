package model;

import utils.Identifier;
import java.time.LocalDateTime;

public class Task {
    protected Integer id;
    protected String title;
    protected String description;
    protected Status status;
    protected Type type;
    protected int duration;
    protected LocalDateTime startTime;

    public Task(String title, String description) {
        this.id = Identifier.INSTANCE.generate();
        this.type = Type.TASK;
        this.status = Status.NEW;
        this.title = title;
        this.description = description;
        this.duration = 0;
    }

    public Task(String title, String description, LocalDateTime startTime) {
        this.id = Identifier.INSTANCE.generate();
        this.type = Type.TASK;
        this.status = Status.NEW;
        this.title = title;
        this.description = description;
        this.duration = 0;
        this.startTime = startTime;
    }

    public Task(String description, String title, Status status) {
        this.description = description;
        this.title = title;
        this.status = status;
    }

    public Task(Integer id,
                Type type,
                String title,
                String description,
                Status status,
                int duration,
                LocalDateTime startTime) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.description = description;
        this.status = status;
        this.duration = duration;
        this.startTime = startTime;
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

    public LocalDateTime getEndTime() {
        return startTime != null ? startTime.plusMinutes(duration) : null;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getDuration() {
        return duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", type=" + type +
                ", duration=" + duration +
                ", startTime=" + startTime +
                '}';
    }
}
