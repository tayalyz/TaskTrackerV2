package model;

import java.util.HashMap;
import java.util.Map;

import static model.Status.*;

public class Epic extends Task {
    private Map<Integer, Subtask> subtasks;

    public Epic(String title, String description) {
        super(title, description);
        this.subtasks = new HashMap<>();
        this.type = Type.EPIC;
    }

    public Map<Integer, Subtask> getSubtasks() {
        return subtasks;
    }

    public void setSubtasks(HashMap<Integer, Subtask> subtasks) {
        this.subtasks = subtasks;
    }

    public Subtask addSubtask(Subtask subtask) {
        return subtasks.put(subtask.getId(), subtask);
    }

    public void updateStatus() {
        Status newStatus = Status.NEW;

        boolean isNew = false;
        boolean isDone = false;
        boolean isInProgress = false;

        for (Subtask subtask : subtasks.values()) {
            if (subtask != null) {
                switch (subtask.getStatus()) {
                    case NEW:
                        isNew = true;
                        break;
                    case DONE:
                        isDone = true;
                        break;
                    default:
                        isInProgress = true;
                }

                if (isInProgress || (isNew && isDone) ){
                    newStatus = IN_PROGRESS;
                } else {
                    newStatus = isDone ? DONE : NEW;
                }
            }
            this.status = newStatus;
        }
    }

    @Override
    public String toString() {
        return "Epic{" +
                "type=" + type +
                ", status=" + status +
                ", description='" + description + '\'' +
                ", title='" + title + '\'' +
                ", id=" + id +
                ", subtasks=" + subtasks +
                '}';
    }
}
