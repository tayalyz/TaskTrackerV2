package handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Task;
import service.HistoryManager;
import utils.LocalDateTimeAdapter;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;


public class HistoryHandler implements HttpHandler {

    private final HistoryManager<Task> historyManager;
    private final Gson gson;

    public HistoryHandler(HistoryManager<Task> historyManager) {
        this.historyManager = historyManager;
        this.gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        try (OutputStream out = exchange.getResponseBody()) {

            if (!method.equals("GET")) {
                exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_METHOD, 0);
                out.write("Некорректный метод запроса".getBytes(StandardCharsets.UTF_8));
            }

            String json = gson.toJson(historyManager.getHistory());

            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
            out.write(json.getBytes(StandardCharsets.UTF_8));
        }
    }
}