package utils;

import model.Task;
import service.HistoryManager;
import service.InMemoryHistoryManager;
import service.InMemoryTaskManager;
import service.TaskManager;

import java.util.Objects;

public class Managers {
    private static TaskManager<Task> taskManager;
    private static HistoryManager<Task> historyManager;

    private Managers() {}

    public static TaskManager<Task> getDefault() {
        if (Objects.isNull(taskManager)) {
            taskManager = new InMemoryTaskManager<>();
        }
        return taskManager;
    }

    public static HistoryManager<Task> getDefaultHistory() {
        if (Objects.isNull(historyManager)) {
            historyManager = new InMemoryHistoryManager<>();
        }
        return historyManager;
    }
}
