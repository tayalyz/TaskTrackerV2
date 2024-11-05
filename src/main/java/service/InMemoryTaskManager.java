package service;

import exception.WrongTimeIntervalException;
import model.Epic;
import model.Subtask;
import model.Task;
import utils.Managers;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager<T extends Task> implements TaskManager<T> {
    private final Map<Integer, T> tasks;
    protected final HistoryManager<Task> historyManager;
    protected final Set<T> prioritizedTasks;

    public InMemoryTaskManager() {
        this.tasks = new HashMap<>();
        this.historyManager = Managers.getDefaultHistory();
        this.prioritizedTasks = new TreeSet<>(Comparator
                .comparing(T::getStartTime, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(T::getId));
    }

    @Override
    public T getTaskById(int id) {
        if (tasks.get(id) == null) {
            System.err.println("Task not found");
        } else {
            T task = tasks.get(id);
            historyManager.add(task);
            return task;
        }
        return null;
    }

    @Override
    public T add(T task) {
        if (task instanceof Subtask) {
            Epic epic = (Epic) tasks.get(((Subtask) task).getParentId());
            epic.addSubtask((Subtask) task);
            updateEpicData(epic);
        }
        prioritizedTasks.add(task);
        checkTaskFreeInterval(task);
        return tasks.put(task.getId(), task);
    }

    @Override
    public void removeAllTasks() {
        if (!tasks.isEmpty()) {
            for (T task : tasks.values()) {
                historyManager.remove(task.getId());
            }
            tasks.clear();
            prioritizedTasks.clear();
        }
        System.out.println("All tasks deleted");
    }

    @Override
    public void removeById(int id) {
        if (tasks.containsKey(id)) {
            T task = tasks.get(id);

            if (task instanceof Epic && !((Epic) task).getSubtasks().isEmpty()) {
                List<Integer> subtasksIds = ((Epic) task).getSubtasksIds();
                for (int subtaskId : subtasksIds) {
                    removeById(subtaskId);
                }
            } else if (task instanceof Subtask) {
                Epic epic = (Epic) tasks.get(((Subtask) task).getParentId());
                epic.removeSubtaskById(task.getId());
                updateEpicData(epic);
            }

            historyManager.remove(id);
            tasks.remove(id);
            prioritizedTasks.removeIf(t -> t.getId() == task.getId());
        } else {
            System.err.println("Task does not exist");
        }
    }

    @Override
    public Optional<T> update(T task) {
        if (tasks.containsKey(task.getId())) {
            if (task instanceof Subtask) {
                updateEpicData((Epic) tasks.get(((Subtask) task).getParentId()));
            }
            checkTaskFreeInterval(task);

            tasks.put(task.getId(), task);
            prioritizedTasks.removeIf(t -> t.getId() == task.getId());
            prioritizedTasks.add(task);
            return Optional.of(task);
        } else {
            System.err.println("Task does not exist");
        }
        return Optional.empty();
    }

    @Override
    public List<T> getAllTasks() {
        if (!tasks.isEmpty()) {
            return new ArrayList<>(tasks.values());
        } else {
            System.err.println("No tasks found");
        }
        return List.of();
    }

    @Override
    public Set<T> getPrioritizedTasks() {
        return prioritizedTasks;
    }

    @Override
    public List<Integer> getHistory() {
        return historyManager.getHistory().stream()
                .map(Task::getId)
                .collect(Collectors.toList());
    }

    private void checkTaskFreeInterval(T task) {
        if (task.getStartTime() == null || (task.getStartTime() == null && task.getDuration() == 0)) {
            return;
        }

        boolean validEndTime = task.getEndTime().isAfter(task.getStartTime()) || task.getDuration() == 0;
        boolean validStartTime = task.getStartTime().isBefore(task.getEndTime()) || task.getDuration() == 0;

        if (!validStartTime || !validEndTime) {
            throw new WrongTimeIntervalException("Time interval is wrong");
        }

        for (Task savedTask : prioritizedTasks) {
            if (!task.equals(savedTask)) {
                if (task instanceof Subtask) {
                    if (!savedTask.equals(tasks.get(((Subtask) task).getParentId()))) {
                        isValidTime(task, savedTask);
                    }
                } else if (task instanceof Epic) {
                    if (!((Epic) task).getSubtasks().containsValue(savedTask)) {
                        isValidTime(task, savedTask);
                    }
                } else {
                    isValidTime(task, savedTask);
                }
            }
        }
    }

    private void isValidTime(T task, Task savedTask) {
        if (savedTask.getStartTime() == null || (savedTask.getStartTime() == null && savedTask.getDuration() == 0)) {
            return;
        }
        boolean checkEndTime = task.getEndTime().isAfter(savedTask.getEndTime()) || task.getEndTime().isBefore(savedTask.getStartTime());
        boolean checkStartTime = task.getStartTime().isAfter(savedTask.getEndTime()) || task.getStartTime().isBefore(savedTask.getStartTime());

        if (!checkEndTime || !checkStartTime) {
            throw new WrongTimeIntervalException("Time interval is wrong");
        }
    }

    private void updateEpicData(Epic epic) {
        epic.updateStatus();
        epic.updateTime();
    }

}
