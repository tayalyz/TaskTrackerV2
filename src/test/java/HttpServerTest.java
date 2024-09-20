import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import exception.ClientException;
import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.HttpTaskServer;
import server.KVServer;
import utils.LocalDateTimeAdapter;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpServerTest {

    private static final HttpClient httpClient = HttpClient.newHttpClient();

    private static final Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();

    private HttpTaskServer server;
    private KVServer kvServer;
    private final String baseUrl = "http://localhost:" + HttpTaskServer.PORT + "/tasks";
    private final String taskUrl = baseUrl + "/task";
    private final String subtaskUrl = baseUrl + "/subtask";
    private final String epicUrl = baseUrl + "/epic";

    @BeforeEach
    public void create() throws IOException {
        kvServer = new KVServer();
        kvServer.start();
        server = new HttpTaskServer();
        server.start();
        server.getTaskManager().removeAllTasks();
        // todo очищать историю и таски
    }

    @AfterEach
    public void destroy() {
        server.stop();
        kvServer.stop();
    }

    @Test
    public void addAndGetPrioritizedTasks() {
        Task task1 = new Task("t1", "d1", LocalDateTime.now().plusMinutes(10));
        Task task2 = new Task("t2", "d2", LocalDateTime.now());
        Task task3 = new Task("t3", "d3");

        final List<Task> tasks = List.of(task1, task2, task3);
        tasks.forEach(t -> server.getTaskManager().add(t));

        URI url = URI.create(baseUrl);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = getResponse(request);

        List<Task> tasksResponse = gson.fromJson(response.body(), new TypeToken<List<Task>>() {
        }.getType());

        assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        assertEquals(task2.getId(), tasksResponse.get(0).getId(), "Порядок задач неправильный");
        assertEquals(task1.getId(), tasksResponse.get(1).getId(), "Порядок задач неправильный");
        assertEquals(task3.getId(), tasksResponse.get(2).getId(), "Порядок задач неправильный");
    }

    @Test
    public void addAndGetEmptyPrioritizedTasks() {
        URI url = URI.create(baseUrl);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = getResponse(request);

        List<Task> tasksResponse = gson.fromJson(response.body(), new TypeToken<List<Task>>() {
        }.getType());

        assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        assertEquals(0, tasksResponse.size(), "Размер списка задач неправильный");
    }

    @Test
    public void addAndGetAllTasks() {
        Task task1 = new Task("t1", "d1", LocalDateTime.now().plusMinutes(10));
        Task task2 = new Task("t2", "d2", LocalDateTime.now());
        Task task3 = new Task("t3", "d3");
        Epic epic = new Epic("e1", "e1");
        Subtask subtask = new Subtask("s1", "s1", epic.getId());

        final List<Task> tasks = List.of(epic, task1, subtask, task2, task3);
        tasks.forEach(t -> server.getTaskManager().add(t));

        URI url = URI.create(taskUrl);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = getResponse(request);

        List<Task> tasksResponse = gson.fromJson(response.body(), new TypeToken<List<Task>>() {
        }.getType());

        assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        assertEquals(3, tasksResponse.size(), "Размер списка задач неправильный");
        assertEquals(task1.getId(), tasksResponse.get(0).getId(), "Порядок задач неправильный");
        assertEquals(task2.getId(), tasksResponse.get(1).getId(), "Порядок задач неправильный");
        assertEquals(task3.getId(), tasksResponse.get(2).getId(), "Порядок задач неправильный");
    }

    @Test
    public void addAndGetAllTasksIfNoTasks() {
        Epic epic1 = new Epic("e1", "e1");
        Epic epic2 = new Epic("e2", "e2");
        Subtask subtask1 = new Subtask("s1", "s1", epic1.getId());
        Subtask subtask2 = new Subtask("s2", "s2", epic1.getId());

        final List<Task> tasks = List.of(epic1, epic2, subtask1, subtask2);
        tasks.forEach(t -> server.getTaskManager().add(t));

        URI url = URI.create(taskUrl);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = getResponse(request);

        List<Task> tasksResponse = gson.fromJson(response.body(), new TypeToken<List<Task>>() {
        }.getType());

        assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        assertEquals(0, tasksResponse.size(), "Размер списка задач неправильный");
    }

    @Test
    public void addAndGetAllSubtasks() {
        Epic epic1 = new Epic("e1", "e1");
        Subtask subtask1 = new Subtask("s1", "s1", epic1.getId());
        Subtask subtask2 = new Subtask("s2", "s2", epic1.getId());
        Task task1 = new Task("t1", "d1", LocalDateTime.now().plusMinutes(10));

        final List<Task> tasks = List.of(epic1, task1, subtask1, subtask2);
        tasks.forEach(t -> server.getTaskManager().add(t));

        URI url = URI.create(subtaskUrl);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = getResponse(request);

        List<Task> tasksResponse = gson.fromJson(response.body(), new TypeToken<List<Subtask>>() {
        }.getType());

        assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        assertEquals(2, tasksResponse.size(), "Размер списка подзадач неправильный");
        assertEquals(subtask1.getId(), tasksResponse.get(0).getId(), "Порядок задач неправильный");
        assertEquals(subtask2.getId(), tasksResponse.get(1).getId(), "Порядок задач неправильный");
    }

    @Test
    public void addAndGetAllSubtasksIfNoSubtasks() {
        Task task1 = new Task("t1", "d1", LocalDateTime.now().plusMinutes(10));
        Task task2 = new Task("t2", "d2", LocalDateTime.now());
        Task task3 = new Task("t3", "d3");


        final List<Task> tasks = List.of(task1, task2, task3);
        tasks.forEach(t -> server.getTaskManager().add(t));

        URI url = URI.create(subtaskUrl);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = getResponse(request);

        List<Task> tasksResponse = gson.fromJson(response.body(), new TypeToken<List<Task>>() {
        }.getType());

        assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        assertEquals(0, tasksResponse.size(), "Размер списка подзадач неправильный");
    }

    @Test
    public void addAndGetAllEpics() {
        Epic epic1 = new Epic("e1", "e1");
        Epic epic2 = new Epic("e1", "e1");
        Subtask subtask1 = new Subtask("s1", "s1", epic1.getId());
        Subtask subtask2 = new Subtask("s2", "s2", epic1.getId());
        Task task1 = new Task("t1", "d1", LocalDateTime.now().plusMinutes(10));

        final List<Task> tasks = List.of(epic1, epic2, task1, subtask1, subtask2);
        tasks.forEach(t -> server.getTaskManager().add(t));

        URI url = URI.create(epicUrl);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = getResponse(request);

        List<Task> tasksResponse = gson.fromJson(response.body(), new TypeToken<List<Epic>>() {
        }.getType());

        assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        assertEquals(2, tasksResponse.size(), "Размер списка эпиков неправильный");
        assertEquals(epic1.getId(), tasksResponse.get(0).getId(), "Порядок задач неправильный");  // todo почему порядок не тот???
        assertEquals(epic2.getId(), tasksResponse.get(1).getId(), "Порядок задач неправильный");
    }

    @Test
    public void addAndGetAllEpicsIfNoEpics() {
        Task task1 = new Task("t1", "d1", LocalDateTime.now().plusMinutes(10));
        Task task2 = new Task("t2", "d2", LocalDateTime.now());
        Task task3 = new Task("t3", "d3");

        final List<Task> tasks = List.of(task1, task2, task3);
        tasks.forEach(t -> server.getTaskManager().add(t));

        URI url = URI.create(epicUrl);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = getResponse(request);

        List<Task> tasksResponse = gson.fromJson(response.body(), new TypeToken<List<Task>>() {
        }.getType());

        assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        assertEquals(0, tasksResponse.size(), "Размер списка эпиков неправильный");
    }

    @Test
    public void addAndGetTaskById() {
        Task task1 = new Task("t1", "d1", LocalDateTime.now().plusMinutes(10));
        Task task2 = new Task("t2", "d2", LocalDateTime.now());
        Task task3 = new Task("t3", "d3");
        Epic epic = new Epic("e1", "e1");
        Subtask subtask = new Subtask("s1", "s1", epic.getId());

        final List<Task> tasks = List.of(epic, task1, subtask, task2, task3);
        tasks.forEach(t -> server.getTaskManager().add(t));

        URI url = URI.create(taskUrl + "?id=" + task3.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = getResponse(request);

        Task taskResponse = gson.fromJson(response.body(), Task.class);

        assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        assertEquals(task3.getId(), taskResponse.getId());
    }

    @Test
    public void addAndGetTaskByWrongId() {
        URI url = URI.create(taskUrl + "?id=" + 1000);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = getResponse(request);

        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.statusCode());
        assertEquals("Задача c id 1000 не найдена", response.body());
    }

    @Test
    public void addAndGetSubtaskById() {
        Task task1 = new Task("t1", "d1", LocalDateTime.now().plusMinutes(10));
        Task task2 = new Task("t2", "d2", LocalDateTime.now());
        Task task3 = new Task("t3", "d3");
        Epic epic = new Epic("e1", "e1");
        Subtask subtask = new Subtask("s1", "s1", epic.getId());

        final List<Task> tasks = List.of(epic, task1, subtask, task2, task3);
        tasks.forEach(t -> server.getTaskManager().add(t));

        URI url = URI.create(subtaskUrl + "?id=" + subtask.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = getResponse(request);

        Task taskResponse = gson.fromJson(response.body(), Subtask.class);

        assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        assertEquals(subtask.getId(), taskResponse.getId());
    }

    @Test
    public void addAndGetSubtaskByWrongId() {
        URI url = URI.create(subtaskUrl + "?id=" + 1000);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = getResponse(request);

        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.statusCode());
        assertEquals("Подзадача c id 1000 не найдена", response.body());
    }

    @Test
    public void addAndGetEpicById() {
        Task task1 = new Task("t1", "d1", LocalDateTime.now().plusMinutes(10));
        Task task2 = new Task("t2", "d2", LocalDateTime.now());
        Task task3 = new Task("t3", "d3");
        Epic epic = new Epic("e1", "e1");
        Subtask subtask = new Subtask("s1", "s1", epic.getId());

        final List<Task> tasks = List.of(epic, task1, subtask, task2, task3);
        tasks.forEach(t -> server.getTaskManager().add(t));

        URI url = URI.create(epicUrl + "?id=" + epic.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = getResponse(request);

        Task taskResponse = gson.fromJson(response.body(), Epic.class);

        assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        assertEquals(epic.getId(), taskResponse.getId());
    }

    @Test
    public void addAndGetEpicByWrongId() {
        URI url = URI.create(epicUrl + "?id=" + 1000);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = getResponse(request);

        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.statusCode());
        assertEquals("Эпик c id 1000 не найден", response.body());
    }

    @Test
    public void updateTask() {
        Task task = new Task("t1", "d1", LocalDateTime.now().plusMinutes(10));
        server.getTaskManager().add(task);
        Task updatedTask = new Task("updated", "updated", LocalDateTime.now().plusMinutes(10));
        updatedTask.setId(task.getId());

        URI url = URI.create(taskUrl);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(updatedTask)))
                .build();

        HttpResponse<String> response = getResponse(request);

        assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        assertEquals(String.format("Задача с id %d успешно обновлена", task.getId()), response.body(), "Задача не была обновлена");
    }

    @Test
    public void updateInvalidTask() {
        Task task = new Task("t1", "d1", LocalDateTime.now().plusMinutes(10));
        server.getTaskManager().add(task);
        Task updatedTask = new Task(null, "updated", LocalDateTime.now().plusMinutes(10));
        updatedTask.setId(task.getId());

        URI url = URI.create(taskUrl);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(updatedTask)))
                .build();

        HttpResponse<String> response = getResponse(request);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
        assertEquals("Невалидная задача", response.body(), "Невалидная задача была обновлена");
    }

    @Test
    public void updateSubtask() {
        Epic epic = new Epic("ep1", "e1");
        Subtask subtask = new Subtask("sub1", "sub1", epic.getId());
        server.getTaskManager().add(epic);
        server.getTaskManager().add(subtask);

        Subtask updatedSubtask = new Subtask("updated", "updated", epic.getId());
        updatedSubtask.setId(subtask.getId());

        URI url = URI.create(subtaskUrl);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(updatedSubtask)))
                .build();

        HttpResponse<String> response = getResponse(request);

        assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        assertEquals(String.format("Подзадача с id %d успешно обновлена", subtask.getId()), response.body(), "Подзадача не была обновлена");
    }

    @Test
    public void updateInvalidSubtask() {
        Epic epic = new Epic(null, "e1");
        Subtask subtask = new Subtask("sub1", "sub1", epic.getId());
        server.getTaskManager().add(epic);
        server.getTaskManager().add(subtask);

        Subtask updatedSubtask = new Subtask("updated", null, epic.getId());
        updatedSubtask.setId(subtask.getId());

        URI url = URI.create(subtaskUrl);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(updatedSubtask)))
                .build();

        HttpResponse<String> response = getResponse(request);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
        assertEquals("Невалидная подзадача", response.body(), "Невалидная подзадача была обновлена");
    }

    @Test
    public void updateEpic() {
        Epic epic = new Epic("e1", "e1");
        server.getTaskManager().add(epic);

        Epic updatedEpic = new Epic("updated", "e1");
        updatedEpic.setId(epic.getId());

        URI url = URI.create(epicUrl);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(updatedEpic)))
                .build();

        HttpResponse<String> response = getResponse(request);

        assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        assertEquals(String.format("Эпик с id %d успешно обновлен", epic.getId()), response.body(), "Эпик не был обновлен");
    }

    @Test
    public void updateInvalidEpic() {
        Epic epic = new Epic("e1", "e1");
        server.getTaskManager().add(epic);

        Epic updatedEpic = new Epic("updated", null);
        updatedEpic.setId(epic.getId());

        URI url = URI.create(epicUrl);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(updatedEpic)))
                .build();

        HttpResponse<String> response = getResponse(request);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
        assertEquals("Невалидный эпик", response.body(), "Невалидный эпик был обновлен");
    }

    @Test
    public void addTask() {
        Task task = new Task("t1", "d1", LocalDateTime.now().plusMinutes(10));

        URI url = URI.create(taskUrl);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task)))
                .build();

        HttpResponse<String> response = getResponse(request);

        assertEquals(1, server.getTaskManager().getAllTasks().size());
        assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        assertEquals("Задача успешно добавлена", response.body(), "Задача не была добавлена");
    }

    @Test
    public void addInvalidTask() {
        Task task = new Task("t1", null, LocalDateTime.now().plusMinutes(10));

        URI url = URI.create(taskUrl);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task)))
                .build();

        HttpResponse<String> response = getResponse(request);

        assertEquals(0, server.getTaskManager().getAllTasks().size());
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
        assertEquals("Невалидная задача", response.body(), "Невалидная задача была добавлена");
    }

    @Test
    public void addEpicAndSubtask() {
        Epic epic = new Epic("ep1", "e1");
        Subtask subtask = new Subtask("sub1", "sub1", epic.getId());

        URI urlEpic = URI.create(epicUrl);
        HttpRequest requestEpic = HttpRequest.newBuilder()
                .uri(urlEpic)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(epic)))
                .build();

        HttpResponse<String> responseEpic = getResponse(requestEpic);

        assertEquals(HttpURLConnection.HTTP_OK, responseEpic.statusCode());
        assertEquals("Эпик успешно добавлен", responseEpic.body(), "Эпик не был добавлен");

        URI urlSub = URI.create(subtaskUrl);
        HttpRequest requestSub = HttpRequest.newBuilder()
                .uri(urlSub)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(subtask)))
                .build();

        HttpResponse<String> responseSub = getResponse(requestSub);

        assertEquals(2, server.getTaskManager().getAllTasks().size());
        assertEquals(HttpURLConnection.HTTP_OK, responseSub.statusCode());
        assertEquals("Подзадача успешно добавлена", responseSub.body(), "Подзадача не была добавлена");
    }

    @Test
    public void addInvalidEpicAndSubtask() {
        Epic epic = new Epic(null, "e1");
        Subtask subtask = new Subtask(null, null, epic.getId());

        URI urlEpic = URI.create(epicUrl);
        HttpRequest requestEpic = HttpRequest.newBuilder()
                .uri(urlEpic)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(epic)))
                .build();

        HttpResponse<String> responseEpic = getResponse(requestEpic);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, responseEpic.statusCode());
        assertEquals("Невалидный эпик", responseEpic.body(), "Невалидный эпик был добавлен");

        URI urlSub = URI.create(subtaskUrl);
        HttpRequest requestSub = HttpRequest.newBuilder()
                .uri(urlSub)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(subtask)))
                .build();

        HttpResponse<String> responseSub = getResponse(requestSub);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, responseSub.statusCode());
        assertEquals("Невалидная подзадача", responseSub.body(), "Невалидная подзадача была добавлена");
    }

    @Test
    public void addAndRemoveTaskById() {
        Task task = new Task("t1", "d1", LocalDateTime.now().plusMinutes(10));

        server.getTaskManager().add(task);

        URI url = URI.create(taskUrl + "?id=" + task.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = getResponse(request);

        assertEquals(0, server.getTaskManager().getAllTasks().size());
        assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        assertEquals(String.format("Задача c id %d удалена", task.getId()), response.body());
    }

    @Test
    public void addAndRemoveTaskByWrongId() {
        URI url = URI.create(taskUrl + "?id=" + 1000);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = getResponse(request);

        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.statusCode());
        assertEquals("Задача c id 1000 не найдена", response.body());
    }

    @Test
    public void addAndRemoveTaskWithoutId() {
        URI url = URI.create(taskUrl);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = getResponse(request);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
        assertEquals("Некорректный запрос. Не передан id задачи", response.body());
    }

    @Test
    public void addAndRemoveSubtaskById() {
        Epic epic = new Epic("ep1", "e1");
        Subtask subtask = new Subtask("sub1", "sub1", epic.getId());

        final List<Task> tasks = List.of(epic, subtask);
        tasks.forEach(t -> server.getTaskManager().add(t));

        URI url = URI.create(subtaskUrl + "?id=" + subtask.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = getResponse(request);

        assertEquals(1, server.getTaskManager().getAllTasks().size());
        assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        assertEquals(String.format("Подзадача c id %d удалена", subtask.getId()), response.body());
    }

    @Test
    public void addAndRemoveSubtaskByWrongId() {
        URI url = URI.create(subtaskUrl + "?id=" + 1000);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = getResponse(request);


        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.statusCode());
        assertEquals("Подзадача c id 1000 не найдена", response.body());
    }

    @Test
    public void addAndRemoveSubtaskWithoutId() {
        URI url = URI.create(subtaskUrl);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = getResponse(request);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
        assertEquals("Некорректный запрос. Не передан id подзадачи", response.body());
    }

    @Test
    public void addAndRemoveEpicById() {
        Epic epic = new Epic("ep1", "e1");

        server.getTaskManager().add(epic);

        URI url = URI.create(epicUrl + "?id=" + epic.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = getResponse(request);

        assertEquals(0, server.getTaskManager().getAllTasks().size());
        assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        assertEquals(String.format("Эпик c id %d удален", epic.getId()), response.body());
    }

    @Test
    public void addAndRemoveEpicByWrongId() {
        URI url = URI.create(epicUrl + "?id=" + 1000);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = getResponse(request);

        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.statusCode());
        assertEquals("Эпик c id 1000 не найден", response.body());
    }

    @Test
    public void addAndRemoveEpicWithoutId() {
        URI url = URI.create(epicUrl);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = getResponse(request);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
        assertEquals("Некорректный запрос. Не передан id эпика", response.body());
    }

    @Test
    public void addAndGetHistory() {
        Task task1 = new Task("t1", "d1", LocalDateTime.now().plusMinutes(10));
        Task task2 = new Task("t2", "d2", LocalDateTime.now());
        Task task3 = new Task("t3", "d3");
        Epic epic = new Epic("e1", "e1");
        Subtask subtask = new Subtask("s1", "s1", epic.getId());

        final List<Task> tasks = List.of(epic, task1, subtask, task2, task3);
        tasks.forEach(t -> server.getTaskManager().add(t));
        tasks.forEach(t -> server.getTaskManager().getTaskById(t.getId()));

        URI url = URI.create(baseUrl + "/history");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = getResponse(request);

        List<Task> tasksResponse = gson.fromJson(response.body(), new TypeToken<List<Task>>() {
        }.getType());

        assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        assertEquals(5, tasksResponse.size(), "Размер списка задач неправильный");
        assertEquals(epic.getId(), tasksResponse.get(0).getId(), "Порядок задач неправильный");
        assertEquals(task1.getId(), tasksResponse.get(1).getId(), "Порядок задач неправильный");
        assertEquals(subtask.getId(), tasksResponse.get(2).getId(), "Порядок задач неправильный");
        assertEquals(task2.getId(), tasksResponse.get(3).getId(), "Порядок задач неправильный");
        assertEquals(task3.getId(), tasksResponse.get(4).getId(), "Порядок задач неправильный");
    }

    @Test
    public void getEmptyHistory() {
        URI url = URI.create(baseUrl + "/history");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = getResponse(request);

        List<Task> tasksResponse = gson.fromJson(response.body(), new TypeToken<List<Task>>() {
        }.getType());

        assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        assertEquals(0, tasksResponse.size(), "Размер списка задач неправильный");
    }

    private HttpResponse<String> getResponse(HttpRequest request) {
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new ClientException("Не получилось отправить запрос");
        }
    }
}
