package service;

import model.Task;

import java.util.List;

public interface HistoryManager<T extends Task> {
    void add(T task);
    List<T> getHistory();
}
