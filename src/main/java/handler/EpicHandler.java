package handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Epic;
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

public class EpicHandler implements HttpHandler {

    private final TaskManager<Task> taskManager;
    private final Gson gson;

    public EpicHandler(TaskManager<Task> taskManager) {
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

                        List<Task> epics = taskManager.getAllTasks().stream()
                                .filter(epic -> epic.getClass().equals(Epic.class))
                                .collect(Collectors.toList());

                        json = gson.toJson(epics);
                        exchange.getResponseCode();
                        exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
                        out.write(json.getBytes(StandardCharsets.UTF_8));

                    } else {
                        int id = Integer.parseInt(parseQueryParams(query).get("id"));
                        Task epic = taskManager.getTaskById(id);

                        if (Objects.isNull(epic) || !epic.getClass().equals(Epic.class)) {
                            exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, 0);
                            out.write(String.format("Эпик c id %d не найден", id).getBytes(StandardCharsets.UTF_8));
                            break;
                        }

                        String json = gson.toJson(epic);
                        exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
                        out.write(json.getBytes(StandardCharsets.UTF_8));
                    }
                    break;

                case "POST":
                    try (InputStream in = exchange.getRequestBody()) {
                        String body;
                        Epic epic;

                        try {
                            body = new String(in.readAllBytes());
                            epic = gson.fromJson(body, Epic.class);

                        } catch (JsonSyntaxException e) {
                            exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, 0);
                            out.write(e.getCause().getLocalizedMessage().getBytes(StandardCharsets.UTF_8));
                            break;
                        }

                        if (!epic.validRequiredFields()) {
                            exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, 0);
                            out.write("Невалидный эпик".getBytes(StandardCharsets.UTF_8));
                            break;
                        }

                        if (Objects.nonNull(taskManager.getTaskById(epic.getId()))) {
                            taskManager.update(epic);
                            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
                            out.write(String.format("Эпик с id %d успешно обновлен", epic.getId()).getBytes(StandardCharsets.UTF_8));

                        } else {
                            taskManager.add(epic);
                            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
                            out.write("Эпик успешно добавлен".getBytes(StandardCharsets.UTF_8));
                        }
                    }
                    break;

                case "DELETE":
                    if (Objects.isNull(query)) {
                        exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, 0);
                        out.write("Некорректный запрос. Не передан id эпика".getBytes(StandardCharsets.UTF_8));
                        break;
                    }
                    int id = Integer.parseInt(parseQueryParams(query).get("id"));
                    Task epic = taskManager.getTaskById(id);

                    if (Objects.isNull(epic) || !epic.getClass().equals(Epic.class)) {
                        exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, 0);
                        out.write(String.format("Эпик c id %d не найден", id).getBytes(StandardCharsets.UTF_8));
                        break;
                    }
                    taskManager.removeById(id);
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
                    out.write(String.format("Эпик c id %d удален", id).getBytes(StandardCharsets.UTF_8));
                    break;

                default:
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_METHOD, 0);
                    out.write("Некорректный метод запроса".getBytes(StandardCharsets.UTF_8));
            }
        }
    }
}
