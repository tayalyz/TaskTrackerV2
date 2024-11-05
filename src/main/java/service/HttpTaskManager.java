package service;

import client.KVTaskClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import model.Epic;
import model.Subtask;
import model.Task;
import server.HttpTaskServer;
import server.KVServer;
import utils.LocalDateTimeAdapter;
import utils.Managers;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class HttpTaskManager<T extends Task> extends FileBackedTasksManager<T> {
    private final KVTaskClient kvTaskClient;
    private final Gson gson;

    public HttpTaskManager(String url) {
        super(url);
        this.kvTaskClient = new KVTaskClient(url);
        this.gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();
        loadDataFromServer();
    }

    public KVTaskClient getKvTaskClient() {
        return kvTaskClient;
    }

    @Override
    protected void save() {
        List<T> subtasks = getAllTasks().stream()
                .filter(t -> t.getClass().equals(Subtask.class))
                .collect(Collectors.toList());

        kvTaskClient.put("subtasks", gson.toJson(subtasks));

        List<T> epics = getAllTasks().stream()
                .filter(t -> t.getClass().equals(Epic.class))
                .collect(Collectors.toList());

        kvTaskClient.put("epics", gson.toJson(epics));

        List<T> tasks = getAllTasks().stream()
                .filter(t -> t.getClass().equals(Task.class))
                .collect(Collectors.toList());

        kvTaskClient.put("tasks", gson.toJson(tasks));

        List<Integer> history = getHistory();
        kvTaskClient.put("history", gson.toJson(history));
    }

    private void loadDataFromServer() {
        List<T> tasks = gson.fromJson(kvTaskClient.load("tasks"), new TypeToken<List<Task>>() {
        }.getType());
        List<T> subtasks = gson.fromJson(kvTaskClient.load("subtasks"), new TypeToken<List<Subtask>>() {
        }.getType());
        List<T> epics = gson.fromJson(kvTaskClient.load("epics"), new TypeToken<List<Epic>>() {
        }.getType());
        List<T> history = gson.fromJson(kvTaskClient.load("history"), new TypeToken<List<Task>>() {
        }.getType());

        if (Objects.nonNull(tasks)) {
            tasks.forEach(this::add);
        }
        if (Objects.nonNull(subtasks)) {
            subtasks.forEach(this::add);
        }
        if (Objects.nonNull(epics)) {
            epics.forEach(this::add);
        }
        if (Objects.nonNull(history)) {
            history.forEach(historyManager::add);
        }
    }

    public static void main(String[] args) {
        try {
            new KVServer().start();
            new HttpTaskServer().start();
        } catch (IOException e) {
            throw new RuntimeException();
        }

        TaskManager<Task> httpTaskManager = Managers.getDefault();
        Task task = new Task("t1", "mskmfp", LocalDateTime.now());
        httpTaskManager.add(task);

        Task task1 = new Task("t2", "mskmfp");
        httpTaskManager.add(task1);

        Task task2 = new Task("t3", "mskmfp");
        httpTaskManager.add(task2);

        Epic epic = new Epic("epic", "epic");
        Subtask subtask = new Subtask("sub1", "lpju", epic.getId());
        httpTaskManager.add(epic);
        httpTaskManager.add(subtask);
        System.out.println(httpTaskManager.getTaskById(task.getId()));
        System.out.println(httpTaskManager.getAllTasks());

        Gson gson1 = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();
        System.out.println(gson1.toJson(httpTaskManager.getTaskById(task.getId())) + "\n");
        System.out.println(gson1.toJson(subtask) + "\n");
        System.out.println(gson1.toJson(epic));
    }
}
