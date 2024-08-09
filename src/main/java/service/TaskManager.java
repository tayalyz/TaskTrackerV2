package service;

import model.Task;

import java.util.List;
import java.util.Optional;

public interface TaskManager<T extends Task> {
    T add(T task);
    void removeAllTasks();
    void removeById(int id);
    T getTaskById(int id);
    Optional<T> update(T task);
    List<T> getAllTasks();
}
