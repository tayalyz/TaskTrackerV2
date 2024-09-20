package handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Task;
import service.TaskManager;
import utils.LocalDateTimeAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class TaskHandler implements HttpHandler {

    private final TaskManager<Task> taskManager;
    private final Gson gson;

    public TaskHandler(TaskManager<Task> taskManager) {
        this.taskManager = taskManager;
        this.gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try (OutputStream out = exchange.getResponseBody()) {
            String query = exchange.getRequestURI().getQuery();
            String method = exchange.getRequestMethod();

            switch (method) {
                case "GET":
                    if (Objects.isNull(query)) {
                        String json;

                        List<Task> tasks = taskManager.getAllTasks().stream()
                                .filter(task -> task.getClass().equals(Task.class))
                                .collect(Collectors.toList());

                        json = gson.toJson(tasks);
                        exchange.getResponseCode();
                        exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
                        out.write(json.getBytes(StandardCharsets.UTF_8));

                    } else {
                        int id = Integer.parseInt(parseQueryParams(query).get("id"));
                        Task task = taskManager.getTaskById(id);

                        if (Objects.isNull(task) || !task.getClass().equals(Task.class)) {
                            exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, 0);
                            out.write(String.format("Задача c id %d не найдена", id).getBytes(StandardCharsets.UTF_8));
                            break;
                        }

                        String json = gson.toJson(task);
                        exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
                        out.write(json.getBytes(StandardCharsets.UTF_8));
                    }
                    break;

                case "POST":
                    try (InputStream in = exchange.getRequestBody()) {
                        String body;
                        Task task;

                        try {
                            body = new String(in.readAllBytes());
                            task = gson.fromJson(body, Task.class);

                        } catch (JsonSyntaxException e) {
                            exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, 0);
                            out.write(e.getCause().getLocalizedMessage().getBytes(StandardCharsets.UTF_8));
                            break;
                        }

                        if (!task.validRequiredFields()) {
                            exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, 0);
                            out.write("Невалидная задача".getBytes(StandardCharsets.UTF_8));
                            break;
                        }

                        if (Objects.nonNull(taskManager.getTaskById(task.getId()))) {
                            taskManager.update(task);
                            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
                            out.write(String.format("Задача с id %d успешно обновлена", task.getId()).getBytes(StandardCharsets.UTF_8));

                        } else {
                            taskManager.add(task);
                            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
                            out.write("Задача успешно добавлена".getBytes(StandardCharsets.UTF_8));
                        }
                    }
                    break;

                case "DELETE":
                    if (Objects.isNull(query)) {
                        exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, 0);
                        out.write("Некорректный запрос. Не передан id задачи".getBytes(StandardCharsets.UTF_8));
                        break;
                    }
                    int id = Integer.parseInt(parseQueryParams(query).get("id"));
                    Task task = taskManager.getTaskById(id);

                    if (Objects.isNull(task) || !task.getClass().equals(Task.class)) {
                        exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, 0);
                        out.write(String.format("Задача c id %d не найдена", id).getBytes(StandardCharsets.UTF_8));
                        break;
                    }
                    taskManager.removeById(id);
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
                    out.write(String.format("Задача c id %d удалена", id).getBytes(StandardCharsets.UTF_8));
                    break;

                default:
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_METHOD, 0);
                    out.write("Некорректный метод запроса".getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    protected static Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] parts = param.split("=");
                if (parts.length == 2) {
                    params.put(parts[0], parts[1]);
                }
            }
        }
        return params;
    }
}
