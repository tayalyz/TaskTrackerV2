package ManagerTest;

import client.KVTaskClient;
import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.HttpTaskServer;
import server.KVServer;
import service.HttpTaskManager;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpTaskManagerTest {

    private HttpTaskServer httpTaskServer;
    private KVServer kvServer;
    private KVTaskClient kvTaskClient;
    private HttpTaskManager<Task> taskManager;

    @BeforeEach
    public void createManager() {
        String url = "localhost";

        try {
            kvServer = new KVServer();
            kvServer.start();
            httpTaskServer = new HttpTaskServer();
            httpTaskServer.start();
        } catch (IOException e) {
            throw new RuntimeException();
        }

        kvTaskClient = new KVTaskClient(url);
        taskManager = new HttpTaskManager<>(url);

    }

    @AfterEach
    public void stopServer() {
        taskManager.removeAllTasks();
        kvServer.stop();
        httpTaskServer.stop();
    }

    @Test
    public void saveAdd() {
        Task task = new Task("tt1", "dd1");
        taskManager.add(task);

        Epic epic = new Epic("e1", "ed1");
        taskManager.add(epic);

        Subtask subtask = new Subtask("s1", "sd1", epic.getId());
        taskManager.add(subtask);

        assertEquals("[{\"id\":1,\"title\":\"tt1\",\"description\":\"dd1\",\"status\":\"NEW\",\"type\":\"TASK\",\"duration\":0}]",
                kvTaskClient.load("tasks"));

        assertEquals("[{\"parentId\":2,\"id\":3,\"title\":\"s1\",\"description\":\"sd1\",\"status\":\"NEW\",\"type\":\"SUBTASK\",\"duration\":0}]",
                kvTaskClient.load("subtasks"));

        assertEquals("[{\"subtasks\":{\"3\":{\"parentId\":2,\"id\":3,\"title\":\"s1\",\"description\":\"sd1\",\"status\":\"NEW\",\"type\":\"SUBTASK\",\"duration\":0}},\"id\":2,\"title\":\"e1\",\"description\":\"ed1\",\"status\":\"NEW\",\"type\":\"EPIC\",\"duration\":0}]",
                kvTaskClient.load("epics"));
    }

}
