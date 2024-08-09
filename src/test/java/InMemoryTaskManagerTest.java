import exception.TaskNotFoundException;
import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.InMemoryTaskManager;
import service.TaskManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class InMemoryTaskManagerTest {
    private TaskManager<Task> inMemoryTaskManager;

    @BeforeEach
    public void setUp() {
        inMemoryTaskManager = new InMemoryTaskManager<>();

        Task task1 = new Task("title1", "desc1");
        Task task2 = new Task("title2", "desc2");
        Task task3 = new Task("title3", "desc3");
        task1.setId(1);
        task2.setId(2);
        task3.setId(3);

        inMemoryTaskManager.add(task1);
        inMemoryTaskManager.add(task2);
        inMemoryTaskManager.add(task3);
    }

    @Test
    public void testAdd() {
        Task task = new Task("title4", "desc4");
        inMemoryTaskManager.add(task);

        List<Task> taskList = inMemoryTaskManager.getAllTasks();
        assertTrue(taskList.contains(task));
        assertEquals(4, taskList.size());
    }

    @Test
    public void testGetById() {
        inMemoryTaskManager.getAllTasks();
        Task task = inMemoryTaskManager.getTaskById(3);
        assertEquals(3, task.getId());
    }

    @Test
    public void testGetByIdFailure() {
        assertThrows(TaskNotFoundException.class, () -> inMemoryTaskManager.getTaskById(17000));
    }

    @Test
    public void testGetAllTasks() {
        assertEquals(3, inMemoryTaskManager.getAllTasks().size());
    }

    @Test
    public void testRemoveById() {
        inMemoryTaskManager.removeById(2);
        assertEquals(2, inMemoryTaskManager.getAllTasks().size());
    }

    @Test
    public void testUpdate() {
        Task task = inMemoryTaskManager.getTaskById(3);
        task.setTitle("abs");
        task.setDescription("abs");
        inMemoryTaskManager.update(task);
        assertEquals("abs", task.getTitle());
        assertEquals("abs", task.getDescription());
    }

    @Test
    public void testStatus() {
        Epic epic1 = new Epic("ep1", "des");
        inMemoryTaskManager.add(epic1);

        Subtask subtask1 = new Subtask("", "", epic1);
        Subtask subtask2 = new Subtask("", "", epic1);
        inMemoryTaskManager.add(subtask1);
        inMemoryTaskManager.add(subtask2);
        assertSame(epic1.getStatus(), Status.NEW);

        subtask1.setStatus(Status.DONE);
        subtask2.setStatus(Status.DONE);
        inMemoryTaskManager.update(subtask1);
        inMemoryTaskManager.update(subtask2);
        assertSame(epic1.getStatus(), Status.DONE);

        subtask1.setStatus(Status.IN_PROGRESS);
        inMemoryTaskManager.update(subtask1);
        assertSame(epic1.getStatus(), Status.IN_PROGRESS);
    }
}
