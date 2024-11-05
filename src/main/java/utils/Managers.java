package utils;

import model.Task;
import server.KVServer;
import service.*;

import java.io.IOException;
import java.util.Objects;

public class Managers {
    private static TaskManager<Task> taskManager;
    private static HistoryManager<Task> historyManager;
    private static KVServer kvServer;

    private Managers() {}

    public static TaskManager<Task> getDefault() {
        return Objects.requireNonNullElseGet(taskManager, () -> taskManager = new HttpTaskManager<>("localhost"));
    }

    public static HistoryManager<Task> getDefaultHistory() {
        if (Objects.isNull(historyManager)) {
            historyManager = new InMemoryHistoryManager<>();
        }
        return historyManager;
    }

    public static KVServer getKVServer() {
        if (Objects.isNull(kvServer)) {
            try {
                kvServer = new KVServer();
            } catch (IOException e) {
                throw new RuntimeException();
            }
        }
        return kvServer;
    }
}
