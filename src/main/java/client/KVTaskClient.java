package client;
import exception.ClientException;
import server.KVServer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

public class KVTaskClient {
    private final String baseURL;
    private final String apiToken;
    private final HttpClient client;

    public KVTaskClient(String serverURL) {
        this.baseURL = "http://" + serverURL + ":" + KVServer.PORT;
        this.client = HttpClient.newHttpClient();
        this.apiToken = register();
    }

    private String register() {
        URI uri = URI.create(baseURL + "/register");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        HttpResponse<String> response;
        try {
            response = client.send(request, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new ClientException("Failed to register");
        }
        return response.body() != null ? response.body() : null;
    }

    public void put(String key, String json) {
        URI uri = URI.create(String.format("%s/save/%s?API_TOKEN=%s", baseURL, key, apiToken));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        try {
            client.send(request, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new ClientException("Failed to save data");
        }
    }

    public String load(String key) {
        URI uri = URI.create(String.format("%s/load/%s?API_TOKEN=%s", baseURL, key, apiToken));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        HttpResponse<String> response;
        try {
            response = client.send(request, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new ClientException("Failed to load data");
        }
        return response.body() != null ? response.body() : null;
    }

    public static void main(String[] args) {
        try {
            new KVServer().start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String serverURL = "http://localhost:8078";
        KVTaskClient client = new KVTaskClient(serverURL);


        String key = "2";
        String jsonValue = "{\"tasks\":[], \"epics\":[], \"subtasks\":[]}";
        client.put(key, jsonValue);
        System.out.println("Данные сохранены");
        System.out.println("Загруженные данные: " + client.load(key));

    }
}