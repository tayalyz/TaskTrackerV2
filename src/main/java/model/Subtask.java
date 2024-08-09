package model;

public class Subtask extends Task {
    private final Epic parent;

    public Subtask(String title, String description, Epic parent) {
        super(title, description);
        this.parent = parent;
        this.type = Type.SUBTASK;
    }

    public Subtask(int id, Type type, String title, String description, Status status, Epic parent) {
        super(id, type, title, description, status);
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
                ", parent=" + getParent().title +
                '}';
    }
}
