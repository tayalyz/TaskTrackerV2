package model;

import java.time.LocalDateTime;

public class Subtask extends Task {
    private final Epic parent;

    public Subtask(String title, String description, Epic parent) {
        super(title, description);
        this.parent = parent;
        this.type = Type.SUBTASK;
    }

    public Subtask(Integer id, Type type, String title, String description, Status status, int duration, LocalDateTime startTime, Epic parent) {
        super(id, type, title, description, status, duration, startTime);
        this.parent = parent;
    }

    public Epic getParent() {
        return parent;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "id=" + id +
                ", type=" + type +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", duration=" + duration +
                ", startTime=" + startTime +
                ", parent=" + getParent().title +
                '}';
    }
}
