package service;//a. Получение списка всех задач.
//b. Удаление всех задач.
//c. Получение по идентификатору.
//d. Создание. Сам объект должен передаваться в качестве параметра.
//e. Обновление. Новая версия объекта с верным идентификатором передаётся в виде параметра.
//f. Удаление по идентификатору.
//Дополнительные методы:
//a. Получение списка всех подзадач определённого эпика.

import exception.TaskNotFoundException;
import model.Epic;
import model.Subtask;
import model.Task;
import utils.Managers;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryTaskManager<T extends Task> implements TaskManager<T> {
    private static final AtomicInteger taskId = new AtomicInteger(0);
    private Map<Integer, T> tasks;

    public InMemoryTaskManager() {
        this.tasks = new HashMap<>();
    }

    @Override
    public T getTaskById(int id) {
        T task = Optional.ofNullable(tasks.get(id))
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));
        Managers.getDefaultHistory().add(task); // TODO
        return task;
    }

    @Override
    public Subtask addSubtask(Subtask subtask, Epic epic) {
        subtask.setId(taskId.incrementAndGet());
        return epic.addSubtask(subtask);
    }

    @Override
    public T add(T task) {
        task.setId(taskId.incrementAndGet());
        return tasks.put(task.getId(), task);
    }

    @Override
    public void removeAllTasks() {
        tasks.clear();
    }

    @Override
    public void removeById(int id) {
        if (tasks.containsKey(id)) {
            tasks.remove(id);
        } else {
            throw new TaskNotFoundException("Task does not exist");
        }
    }

    @Override
    public Optional<T> update(T task) {     // TODO
        if (tasks.containsKey(task.getId()) || getAllSubtasks().stream()
                .anyMatch(subtask -> subtask.getId() == task.getId())) {
            if (task instanceof Subtask) {
                ((Subtask) task).getParent().updateStatus();
            }
            tasks.put(task.getId(), task);
            return Optional.of(task);
        } else {
            throw new TaskNotFoundException("Task does not exist");
        }
    }

    @Override
    public List<Subtask> getSubTasks(Epic epic) {
        List<Subtask> subtasks = new ArrayList<>();
        if (getAllEpics().contains(epic)) {
            for (Map.Entry<Integer, Subtask> set : epic.getSubtasks().entrySet()) {
                subtasks.add(set.getValue());
            }
        } else {
            throw new RuntimeException("This epic does not have subtasks");
        }
        return subtasks;
    }

    @Override
    public List<T> getAllTasks() {
        if (!tasks.isEmpty()) {
            return new ArrayList<>(tasks.values());
        } else {
            throw new TaskNotFoundException("No tasks found");
        }
    }

    @Override
    public List<Epic> getAllEpics() {
        List<Epic> epics = new ArrayList<>();
        for (T task : tasks.values()) {
            if (task instanceof Epic) {
                epics.add((Epic) task);
            }
        }

        if (!epics.isEmpty()) {
            return epics;
        } else {
            throw new TaskNotFoundException("No epics found");
        }
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        List<Subtask> sub = new ArrayList<>();
        for (Epic epic : getAllEpics()) {
            sub.addAll(getSubTasks(epic));
        }
        if (sub.isEmpty()) {
            throw new TaskNotFoundException("No subtasks found");
        }
        return sub;
    }
}
