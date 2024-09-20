package handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Subtask;
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

import static handler.TaskHandler.parseQueryParams;

public class SubtaskHandler implements HttpHandler {

    private final TaskManager<Task> taskManager;
    private final Gson gson;

    public SubtaskHandler(TaskManager<Task> taskManager) {
        this.taskManager = taskManager;
        this.gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try (OutputStream out = exchange.getResponseBody()) {
            String query = exchange.getRequestURI().getQuery();

            switch (exchange.getRequestMethod()) {
                case "GET":
                    if (Objects.isNull(query)) {
                        String json;

                        List<Task> subtasks = taskManager.getAllTasks().stream()
                                .filter(task -> task.getClass().equals(Subtask.class))
                                .collect(Collectors.toList());

                        json = gson.toJson(subtasks);
                        exchange.getResponseCode();
                        exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
                        out.write(json.getBytes(StandardCharsets.UTF_8));

                    } else {
                        int id = Integer.parseInt(parseQueryParams(query).get("id"));
                        Task subtask = taskManager.getTaskById(id);

                        if (Objects.isNull(subtask) || !subtask.getClass().equals(Subtask.class)) {
                            exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, 0);
                            out.write(String.format("Подзадача c id %d не найдена", id).getBytes(StandardCharsets.UTF_8));
                            break;
                        }

                        String json = gson.toJson(subtask);
                        exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
                        out.write(json.getBytes(StandardCharsets.UTF_8));
                    }
                    break;

                case "POST":
                    try (InputStream in = exchange.getRequestBody()) {
                        String body;
                        Subtask subtask;

                        try {
                            body = new String(in.readAllBytes());
                            subtask = gson.fromJson(body, Subtask.class);

                        } catch (JsonSyntaxException e) {
                            exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, 0);
                            out.write(e.getCause().getLocalizedMessage().getBytes(StandardCharsets.UTF_8));
                            break;
                        }

                        if (!subtask.validRequiredFields()) {
                            exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, 0);
                            out.write("Невалидная подзадача".getBytes(StandardCharsets.UTF_8));
                            break;
                        }

                        if (Objects.nonNull(taskManager.getTaskById(subtask.getId()))) {
                            taskManager.update(subtask);
                            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
                            out.write(String.format("Подзадача с id %d успешно обновлена", subtask.getId()).getBytes(StandardCharsets.UTF_8));

                        } else {
                            taskManager.add(subtask);
                            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
                            out.write("Подзадача успешно добавлена".getBytes(StandardCharsets.UTF_8));
                        }
                    }
                    break;

                case "DELETE":
                    if (Objects.isNull(query)) {
                        exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, 0);
                        out.write("Некорректный запрос. Не передан id подзадачи".getBytes(StandardCharsets.UTF_8));
                        break;
                    }
                    int id = Integer.parseInt(parseQueryParams(query).get("id"));
                    Task subtask = taskManager.getTaskById(id);

                    if (Objects.isNull(subtask) || !subtask.getClass().equals(Subtask.class)) {
                        exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, 0);
                        out.write(String.format("Подзадача c id %d не найдена", id).getBytes(StandardCharsets.UTF_8));
                    }
                    taskManager.removeById(id);
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
                    out.write(String.format("Подзадача c id %d удалена", id).getBytes(StandardCharsets.UTF_8));
                    break;

                default:
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_METHOD, 0);
                    out.write("Некорректный метод запроса".getBytes(StandardCharsets.UTF_8));
            }
        }
    }
}
