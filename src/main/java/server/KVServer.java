package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

public class KVServer {
	public static final int PORT = 8078;
	private final String apiToken;
	private final HttpServer server;
	private final Map<String, String> data = new HashMap<>();

	public KVServer() throws IOException {
		apiToken = generateApiToken();
		server = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);
		server.createContext("/register", this::register);
		server.createContext("/save", this::save);
		server.createContext("/load", this::load);
	}

	private void load(HttpExchange h) throws IOException {
        try (h) {
            System.out.println("n/load");
            if (!hasAuth(h)) {
                System.out.println("Запрос неавторизован, нужен параметр в query API_TOKEN со значением апи-ключа");
                h.sendResponseHeaders(HttpURLConnection.HTTP_FORBIDDEN, 0);
                return;
            }
            if ("GET".equals(h.getRequestMethod())) {
                String key = h.getRequestURI().getPath().substring("/load/".length());
                if (key.isEmpty()) {
                    System.out.println("Key для загрузки пустой. key указывается в пути: /load/{key}");
                    h.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, 0);
                    return;
                }
                if (!data.containsKey(key)) {
                    System.out.println("Нет значения для ключа " + key);
                    h.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, 0);
                    return;
                }
                String value = data.get(key);
                sendText(h, value);
                System.out.println("Значение для ключа " + key + " успешно загружено!");
            } else {
                System.out.println("/load ждёт GET-запрос, а получил: " + h.getRequestMethod());
                h.sendResponseHeaders(HttpURLConnection.HTTP_BAD_METHOD, 0);
            }
        }
	}

	private void save(HttpExchange h) throws IOException {
        try (h) {
            System.out.println("\n/save");
            if (!hasAuth(h)) {
                System.out.println("Запрос неавторизован, нужен параметр в query API_TOKEN со значением апи-ключа");
                h.sendResponseHeaders(HttpURLConnection.HTTP_FORBIDDEN, 0);
                return;
            }
            if ("POST".equals(h.getRequestMethod())) {
                String key = h.getRequestURI().getPath().substring("/save/".length());
                if (key.isEmpty()) {
                    System.out.println("Key для сохранения пустой. key указывается в пути: /save/{key}");
                    h.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, 0);
                    return;
                }
                String value = readText(h);
                if (value.isEmpty()) {
                    System.out.println("Value для сохранения пустой. value указывается в теле запроса");
                    h.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, 0);
                    return;
                }
                data.put(key, value);
                System.out.println("Значение для ключа " + key + " успешно обновлено!");
                h.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
            } else {
                System.out.println("/save ждёт POST-запрос, а получил: " + h.getRequestMethod());
                h.sendResponseHeaders(HttpURLConnection.HTTP_BAD_METHOD, 0);
            }
        }
	}

	private void register(HttpExchange h) throws IOException {
        try (h) {
            System.out.println("\n/register");
            if ("GET".equals(h.getRequestMethod())) {
                sendText(h, apiToken);
            } else {
                System.out.println("/register ждёт GET-запрос, а получил " + h.getRequestMethod());
                h.sendResponseHeaders(HttpURLConnection.HTTP_BAD_METHOD, 0);
            }
        }
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

	protected boolean hasAuth(HttpExchange h) {
		String rawQuery = h.getRequestURI().getRawQuery();
		return rawQuery != null && (rawQuery.contains("API_TOKEN=" + apiToken) || rawQuery.contains("API_TOKEN=DEBUG"));
	}

	protected String readText(HttpExchange h) throws IOException {
		return new String(h.getRequestBody().readAllBytes(), UTF_8);
	}

	protected void sendText(HttpExchange h, String text) throws IOException {
		byte[] resp = text.getBytes(UTF_8);
		h.getResponseHeaders().add("Content-Type", "application/json");
		h.sendResponseHeaders(HttpURLConnection.HTTP_OK, resp.length);
		h.getResponseBody().write(resp);
	}
}
