package service;

import model.Epic;
import model.Subtask;
import model.Task;

import java.util.List;
import java.util.Optional;

public interface TaskManager<T extends Task> {
    T add(T task);
    Subtask addSubtask(Subtask subtask, Epic epic);
    void removeAllTasks();
    void removeById(int id);
    T getTaskById(int id);
    Optional<T> update(T task);
    List<T> getAllTasks();
    List<Subtask> getSubTasks(Epic epic);
    List<Epic> getAllEpics();
    List<Subtask> getAllSubtasks();
}
