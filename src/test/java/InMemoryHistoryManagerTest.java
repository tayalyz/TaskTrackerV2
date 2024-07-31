import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.HistoryManager;
import service.InMemoryTaskManager;
import service.TaskManager;
import utils.Managers;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryHistoryManagerTest {
    private HistoryManager<Task> inMemoryHistoryManager;
    private TaskManager<Task> inMemoryTaskManager;

    @BeforeEach
    public void setUp() {
        inMemoryHistoryManager = Managers.getDefaultHistory();
        inMemoryTaskManager = new InMemoryTaskManager<>();
        InMemoryTaskManager.setTaskId(new AtomicInteger(0));
        Task task1 = new Task("title1", "desc1");
        Task task2 = new Task("title2", "desc2");
        Task task3 = new Task("title3", "desc3");

        inMemoryTaskManager.add(task1);
        inMemoryTaskManager.add(task2);
        inMemoryTaskManager.add(task3);
    }

    @Test
    public void testGetHistory() {
        Task task1 = new Task("title4", "desc4");
        Task task2 = new Task("title6", "desc6");
        Task task3 = new Task("title5", "desc5");
        inMemoryTaskManager.add(task1);
        inMemoryTaskManager.add(task2);
        inMemoryTaskManager.add(task3);

        inMemoryTaskManager.getTaskById(3);
        inMemoryTaskManager.getTaskById(5);
        inMemoryTaskManager.getTaskById(4);

        List<Task> tasks = inMemoryHistoryManager.getHistory();

        assertTrue(tasks.contains(task1));
        assertTrue(tasks.contains(task2));
        assertFalse(tasks.contains(task3));
        assertEquals( 3, tasks.size());
    }

    @Test
    public void testRemoveTaskFromHistory() {
        Task task = new Task("", "");
        inMemoryTaskManager.add(task);
        inMemoryTaskManager.getTaskById(1);
        inMemoryHistoryManager.remove(1);

        List<Task> tasks = inMemoryHistoryManager.getHistory();

        assertFalse(tasks.contains(task));
        assertEquals( 1, tasks.size());
    }

    @Test
    public void testAddToHistory() {
        Task task = new Task("", "");
        inMemoryTaskManager.add(task);
        inMemoryTaskManager.getTaskById(4);

        List<Task> tasks = inMemoryHistoryManager.getHistory();

        assertTrue(tasks.contains(task));
        assertEquals( 1, tasks.size());
    }
}
