package model;

public class Subtask extends Task {
    private final Epic parent;

    public Subtask(String title, String description, Epic parent) {
        super(title, description);
        this.parent = parent;
        this.type = Type.SUBTASK;
    }

    public Epic getParent() {
        return parent;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "type=" + type +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", id=" + id +
                ", parent=" + getParent().title +
                '}';
    }
}
