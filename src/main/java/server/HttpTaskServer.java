package server;

import com.sun.net.httpserver.HttpServer;
import handler.*;
import model.Task;
import service.HistoryManager;
import service.TaskManager;
import utils.Managers;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    public static final int PORT = 8080;
    public String apiToken;
    private final HttpServer server;
    private final TaskManager<Task> taskManager;

    public HttpTaskServer() throws IOException {
        HistoryManager<Task> historyManager = Managers.getDefaultHistory();
        this.taskManager = Managers.getDefault();
        this.apiToken = generateApiToken();
        this.server = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);

        server.createContext("/tasks", new TasksHandler(taskManager));
        server.createContext("/tasks/task", new TaskHandler(taskManager)); // Normal tasks
        server.createContext("/tasks/subtask", new SubtaskHandler(taskManager)); // Subtasks
        server.createContext("/tasks/epic", new EpicHandler(taskManager)); // Epics
        server.createContext("/tasks/history", new HistoryHandler(historyManager)); // History
    }

    public TaskManager<Task> getTaskManager() {
        return taskManager;
    }

    public void start() {
        System.out.println("Запускаем сервер на порту " + PORT);
        System.out.println("Открой в браузере http://localhost:" + PORT + "/");
        System.out.println("API_TOKEN: " + apiToken);
        server.start();
    }

    public void stop() {
        server.stop(0);
        System.out.println("Сервер остановлен");
    }

    private String generateApiToken() {
        return "" + System.currentTimeMillis();
    }
}


