package ManagerTest;

import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.HistoryManager;
import service.TaskManager;
import utils.Managers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class HistoryManagerTest {

    protected TaskManager<Task> taskManager;
    protected HistoryManager<Task> historyManager;

    @BeforeEach
    public void createManager() {
        taskManager = Managers.getDefault();
        historyManager = Managers.getDefaultHistory();
    }

    @Test
    public void testGetHistory() {
        Task task1 = new Task("title4", "desc4");
        Epic epic = new Epic("title4", "desc4");
        Subtask subtask = new Subtask("title6", "desc6", epic.getId());
        Task task3 = new Task("title5", "desc5");

        taskManager.add(task1);
        taskManager.add(epic);
        taskManager.add(subtask);
        taskManager.add(task3);

        historyManager.add(task1);
        historyManager.add(epic);
        historyManager.add(subtask);

        final List<Task> tasks = historyManager.getHistory();

        assertTrue(tasks.contains(task1));
        assertTrue(tasks.contains(epic));
        assertTrue(tasks.contains(subtask));
        assertFalse(tasks.contains(task3));
        assertEquals( 3, tasks.size(), "история неправильно заполнилась");
    }

    @Test
    public void removeTaskFromHistory() {
        Task task = new Task("", "");
        taskManager.add(task);
        historyManager.add(task);
        historyManager.remove(task.getId());

        final List<Task> tasks = historyManager.getHistory();

        assertEquals( 0, tasks.size(), "задача не была удалена из истории");
    }

    @Test
    public void removeEpicFromHistory() {
        Epic epic = new Epic("", "");
        Subtask subtask = new Subtask("", "", epic.getId());

        taskManager.add(epic);
        taskManager.add(subtask);

        historyManager.add(epic);
        historyManager.add(subtask);

        historyManager.remove(epic.getId());

        final List<Task> tasks = historyManager.getHistory();

        assertFalse(tasks.contains(epic), "эпик не был удален из истории");
        assertTrue(tasks.contains(subtask));
        assertEquals( 1, tasks.size(), "эпик не был удален из истории");
    }

    @Test
    public void removeSubtaskFromHistory() {
        Epic epic = new Epic("", "");
        Subtask subtask = new Subtask("", "", epic.getId());

        taskManager.add(epic);
        taskManager.add(subtask);

        historyManager.add(epic);
        historyManager.add(subtask);

        historyManager.remove(subtask.getId());

        final List<Task> tasks = historyManager.getHistory();

        assertFalse(tasks.contains(subtask), "подзадача не была удалена из истории");
        assertTrue(tasks.contains(epic));
        assertEquals( 1, tasks.size(), "подзадача не была удалена из истории");
    }

    @Test
    public void addTaskToHistory() {
        Task task = new Task("", "");
        taskManager.add(task);
        historyManager.add(task);

        final List<Task> tasks = historyManager.getHistory();

        assertTrue(tasks.contains(task), "задачи нет в истории");
        assertEquals( 1, tasks.size(), "история пуста");
    }

    @Test
    public void addEpicToHistory() {
        Epic epic = new Epic("", "");
        Subtask subtask = new Subtask("", "", epic.getId());

        taskManager.add(epic);
        taskManager.add(subtask);

        historyManager.add(epic);

        final List<Task> tasks = historyManager.getHistory();

        assertTrue(tasks.contains(epic), "эпика нет в истории");
        assertFalse(tasks.contains(subtask), "подзадача добавилась в историю вместе с эпиком");
        assertEquals( 1, tasks.size(), "история пуста");
    }

    @Test
    public void addSubtaskToHistory() {
        Epic epic = new Epic("", "");
        Subtask subtask = new Subtask("", "", epic.getId());

        taskManager.add(epic);
        taskManager.add(subtask);

        historyManager.add(subtask);

        final List<Task> tasks = historyManager.getHistory();

        assertTrue(tasks.contains(subtask), "сабтаска нет в истории");
        assertFalse(tasks.contains(epic), "эпик добавился в историю вместе с подзадачей");
        assertEquals( 1, tasks.size(), "история пуста");
    }
}
