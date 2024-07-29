import exception.TaskNotFoundException;
import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import service.TaskManager;
import utils.Managers;

import java.util.List;
import static org.junit.Assert.*;


public class InMemoryTaskManagerTest {
    private TaskManager<Task> inMemoryTaskManager;

    @Before
    public void setUp() {
        inMemoryTaskManager = Managers.getDefault();

        Task task1 = new Task("title1", "desc1");
        Task task2 = new Task("title2", "desc2");
        Task task3 = new Task("title3", "desc3");

        inMemoryTaskManager.add(task1);
        inMemoryTaskManager.add(task2);
        inMemoryTaskManager.add(task3);
    }

    @After
    public void clean() {
        inMemoryTaskManager.removeAllTasks();
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
        Task task = inMemoryTaskManager.getTaskById(33);
        assertEquals(33, task.getId());
    }

    @Test(expected = TaskNotFoundException.class)
    public void testGetByIdFailure() {
        inMemoryTaskManager.getTaskById(170000);
    }

    @Test
    public void testGetAllTasks() {
        assertEquals(3, inMemoryTaskManager.getAllTasks().size());
    }

    @Test(expected = TaskNotFoundException.class)
    public void testGetAllTasksFailure() {
        inMemoryTaskManager.removeAllTasks();
        inMemoryTaskManager.getAllTasks();
    }

    @Test  //(expected = TaskNotFoundException.class)
    public void testRemoveAll() {
        inMemoryTaskManager.removeAllTasks();
        assertThrows(TaskNotFoundException.class, () -> inMemoryTaskManager.getAllTasks());
    }

    @Test
    public void testRemoveById() {
        inMemoryTaskManager.getAllTasks();
        inMemoryTaskManager.removeById(47);
        assertEquals(2, inMemoryTaskManager.getAllTasks().size());
    }

    @Test(expected = TaskNotFoundException.class)
    public void testRemoveByIdFailure() {
        inMemoryTaskManager.removeById(170000);
    }

    @Test
    public void testUpdate() {
        inMemoryTaskManager.getAllTasks();
        Task task = inMemoryTaskManager.getTaskById(63);
        task.setTitle("abs");
        task.setDescription("abs");
        inMemoryTaskManager.update(task);
        assertEquals("abs", task.getTitle());
        assertEquals("abs", task.getDescription());
    }

    @Test(expected = TaskNotFoundException.class)
    public void testUpdateFailure() {
        inMemoryTaskManager.getAllTasks();
        Task task = new Task("", "");

        task.setTitle("abs");
        task.setDescription("abs");
        inMemoryTaskManager.update(task);
    }

    @Test
    public void testGetAllEpics() {
        Epic epic1 = new Epic("epic1", "desc1");
        Epic epic2 = new Epic("epic2", "desc2");
        Epic epic3 = new Epic("epic3", "desc3");
        inMemoryTaskManager.add(epic1);
        inMemoryTaskManager.add(epic2);
        inMemoryTaskManager.add(epic3);
        assertEquals(3, inMemoryTaskManager.getAllEpics().size());
    }

    @Test(expected = TaskNotFoundException.class)
    public void testGetAllEpicsFailure() {
        inMemoryTaskManager.getAllEpics();
    }

    @Test
    public void testAddSubtask() {
        Epic epic = new Epic("ep", "des");
        inMemoryTaskManager.add(epic);
        assertEquals(1, inMemoryTaskManager.getAllEpics().size());
    }

    @Test
    public void testGetSubtasks() {
        Epic epic = new Epic("ep", "des");
        inMemoryTaskManager.add(epic);

        Subtask subtask1 = new Subtask("", "", epic);
        Subtask subtask2 = new Subtask("", "", epic);
        inMemoryTaskManager.addSubtask(subtask1, epic);
        inMemoryTaskManager.addSubtask(subtask2, epic);

        assertEquals(2, inMemoryTaskManager.getSubTasks(epic).size());
    }

    @Test
    public void testGetSubtasksFailure() {
        Epic epic = new Epic("ep", "des");
        assertThrows(TaskNotFoundException.class, ()-> inMemoryTaskManager.getSubTasks(epic));
    }

    @Test
    public void testGetAllSubtasks() {
        Epic epic = new Epic("ep", "des");
        inMemoryTaskManager.add(epic);

        Epic epic1 = new Epic("ep1", "des");
        inMemoryTaskManager.add(epic1);

        Subtask subtask1 = new Subtask("", "", epic1);
        Subtask subtask2 = new Subtask("", "", epic1);
        inMemoryTaskManager.addSubtask(subtask1, epic1);
        inMemoryTaskManager.addSubtask(subtask2, epic1);

        Epic epic2 = new Epic("ep2", "des");
        inMemoryTaskManager.add(epic2);

        Subtask subtask3 = new Subtask("", "", epic2);
        Subtask subtask4 = new Subtask("", "", epic2);
        inMemoryTaskManager.addSubtask(subtask3, epic2);
        inMemoryTaskManager.addSubtask(subtask4, epic2);

        assertEquals(4, inMemoryTaskManager.getAllSubtasks().size());
    }

    @Test(expected = TaskNotFoundException.class)
    public void testGetAllSubtasksFailure() {
        inMemoryTaskManager.getAllSubtasks();
    }
}
