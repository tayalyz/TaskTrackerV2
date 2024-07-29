import model.Task;
import org.junit.Before;
import org.junit.Test;
import service.HistoryManager;
import service.TaskManager;
import utils.Managers;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class InMemoryHistoryManagerTest {
    private HistoryManager<Task> inMemoryHistoryManager;
    private TaskManager<Task> inMemoryTaskManager;

    @Before
    public void setUp() {
        inMemoryHistoryManager = Managers.getDefaultHistory();
        inMemoryTaskManager = Managers.getDefault();
    }

    @Test
    public void testGetHistory() {
        Task task1 = new Task("title1", "desc1");
        Task task2 = new Task("title2", "desc2");
        Task task3 = new Task("title3", "desc3");
        inMemoryTaskManager.add(task1);
        inMemoryTaskManager.add(task2);
        inMemoryTaskManager.add(task3);

        inMemoryTaskManager.getAllTasks();
        inMemoryTaskManager.getTaskById(67);
        inMemoryTaskManager.getTaskById(68);
        inMemoryTaskManager.getTaskById(69);

        List<Task> tasks = inMemoryHistoryManager.getHistory();

        assertTrue(tasks.contains(task1));
        assertTrue(tasks.contains(task2));
        assertTrue(tasks.contains(task3));
        assertEquals( 5, tasks.size());
    }
}
