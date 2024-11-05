package model;

import java.time.LocalDateTime;

public class Subtask extends Task {
    private final int parentId;

    public Subtask(String title, String description, int parentId) {
        super(title, description);
        this.parentId = parentId;
        this.type = Type.SUBTASK;
    }

    public Subtask(Integer id, Type type, String title, String description, Status status, int duration, LocalDateTime startTime, int parentId) {
        super(id, type, title, description, status, duration, startTime);
        this.parentId = parentId;
    }

    public int getParentId() {
        return parentId;
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
                ", parent=" + getParentId() +
                '}';
    }
}
