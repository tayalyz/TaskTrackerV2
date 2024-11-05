package model;

import java.time.LocalDateTime;
import java.util.*;

import static model.Status.*;

public class Epic extends Task {
    private Map<Integer, Subtask> subtasks;
    private LocalDateTime endTime;

    public Epic(String title, String description) {
        super(title, description);
        this.subtasks = new HashMap<>();
        this.type = Type.EPIC;
    }

    public Epic(Integer id, Type type, String title, String description, Status status, int duration, LocalDateTime startTime) {
        super(id, type, title, description, status, duration, startTime);
        this.subtasks = new HashMap<>();
    }

    public Map<Integer, Subtask> getSubtasks() {
        return subtasks;
    }

    public void setSubtasks(HashMap<Integer, Subtask> subtasks) {
        this.subtasks = subtasks;
    }

    public List<Integer> getSubtasksIds() {
        List<Integer> list = new ArrayList<>();
        for (Map.Entry<Integer, Subtask> set : subtasks.entrySet()) {
            list.add(set.getKey());
        }
        return list;
    }

    public void removeSubtaskById(int id) {
        subtasks.remove(id);
    }

    public void addSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
    }

    public void updateStatus() {
        Status newStatus = NEW;

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

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public LocalDateTime getEndTime() {
        return startTime != null && !subtasks.isEmpty() ? subtasks.values().stream()
                .map(Subtask::getEndTime)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(null) : startTime != null ? startTime.plusMinutes(duration) : null;
    }

    public void updateTime() {
        this.startTime = subtasks.values().stream()
                .map(Subtask::getStartTime)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(null);

        this.endTime = startTime != null ? subtasks.values().stream()
                .map(Subtask::getEndTime)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(startTime.plusMinutes(duration)) : null;

        this.duration = subtasks.values().stream()
                .mapToInt(Subtask::getDuration)
                .sum();
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + id +
                ", type=" + type +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", duration=" + duration +
                ", startTime=" + startTime +
                ", subtasks=" + subtasks +
                '}';
    }
}
