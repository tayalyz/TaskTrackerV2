package ManagerTest;

import exception.ManagerSaveException;
import model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FileBackedTaskManagerTest extends TaskManagerTest {
    private final String fileName = "test.csv";

    @BeforeEach
    public void createManager() {
        taskManager = new FileBackedTasksManager<>(fileName);
    }

    @AfterEach
    public void cleanManager() {
        taskManager.removeAllTasks();
    }

    @Test
    public void emptyFile() {
        assertEquals("""
                id, type, title, description, status, duration, startTime, parentId
                
                """, readFile());
    }

    @Test
    public void saveGetById() {
        Task task = new Task("task1", "desc task1");
        taskManager.add(task);
        taskManager.getTaskById(task.getId());

        assertEquals(String.format("""
                id, type, title, description, status, duration, startTime, parentId
                %d, TASK, task1, desc task1, NEW, 0, 0
                
                %d""", task.getId(), task.getId()), readFile());
    }

    @Test
    public void saveAdd() {
        Task task = new Task("task1", "desc task1");
        taskManager.add(task);

        assertEquals(String.format("""
                id, type, title, description, status, duration, startTime, parentId
                %d, TASK, task1, desc task1, NEW, 0, 0
                
                """, task.getId()), readFile());
    }

    @Test
    public void saveRemoveById() {
        Task task = new Task("task1", "desc task1");
        taskManager.add(task);
        taskManager.getTaskById(task.getId());
        taskManager.removeById(task.getId());

        assertEquals("""
                id, type, title, description, status, duration, startTime, parentId
                
                """, readFile());
    }

    @Test
    public void saveRemoveAll() {
        Task task1 = new Task("task1", "desc task1");
        taskManager.add(task1);
        Task task2 = new Task("task2", "desc tas2");
        taskManager.add(task2);
        Task task3 = new Task("task3", "desc task3");
        taskManager.add(task3);

        taskManager.removeAllTasks();

        assertEquals("""
                id, type, title, description, status, duration, startTime, parentId
                
                """, readFile());
    }

    @Test
    public void saveUpdate() {
        Task task1 = new Task("task1", "desc task1");
        taskManager.add(task1);
        Task task2 = new Task("title UPDATED", "desc UPDATED");
        task2.setId(task1.getId());

        taskManager.update(task2);

        assertEquals(String.format("""
                id, type, title, description, status, duration, startTime, parentId
                %d, TASK, title UPDATED, desc UPDATED, NEW, 0, 0
                
                """, task1.getId()), readFile());
    }

    private String readFile() {
        try {
            return Files.readString(Path.of(fileName));
        } catch (IOException e) {
            throw new ManagerSaveException("Failed to read");
        }
    }
}
