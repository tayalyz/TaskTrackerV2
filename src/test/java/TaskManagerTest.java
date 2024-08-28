import exception.ManagerSaveException;
import exception.TaskNotFoundException;
import exception.WrongTimeIntervalException;
import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.TaskManager;
import utils.Managers;
import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


abstract class TaskManagerTest {

    protected TaskManager<Task> taskManager;

    @BeforeEach
    public void createManager() {
        taskManager = Managers.getDefault();
    }

    @Test
    public void addNewEpic() {
        Epic epic = new Epic("покупки в магазине", "нужно сходить до 15:00");
        taskManager.add(epic);

        final Epic savedEpic = (Epic) taskManager.getTaskById(epic.getId());

        assertNotNull(savedEpic, "задача не найдена");
        assertEquals(epic,savedEpic, "задачи не совпадают");

        final List<Task> tasks = taskManager.getAllTasks();

        assertEquals(1,tasks.size(), "неверное кол-во задач");
    }

    @Test
    public void addNewTask(){
        Task task = new Task("покупки в магазине", "нужно сходить до 15:00");
        taskManager.add(task);

        final Task savedTask = taskManager.getTaskById(task.getId());

        assertNotNull(savedTask, "задача не найдена");
        assertEquals(task,savedTask, "задачи не совпадают");

        final List<Task> tasks = taskManager.getAllTasks();

        assertEquals(1,tasks.size(), "неверное кол-во задач");
    }

    @Test
    public void addSubtaskToEpic(){
        Epic epic = new Epic("покупки в магазине", "нужно сходить до 15:00");
        taskManager.add(epic);

        Subtask subtask = new Subtask("покупки в магазине", "нужно сходить до 15:00", epic);
        taskManager.add(subtask);
        final Subtask savedSubtask = (Subtask) taskManager.getTaskById(subtask.getId());

        assertNotNull(savedSubtask, "задача не найдена");
        assertEquals(subtask,savedSubtask, "задачи не совпадают");

        final List<Task> tasks = taskManager.getAllTasks();
        final Map<Integer, Subtask> map = epic.getSubtasks();

        assertEquals(1,map.size(), "неверное кол-во подзадач");
        assertEquals(2,tasks.size(), "неверное кол-во задач");
    }

    @Test
    public void getSubtasksByEpic() {
        Epic epic = new Epic("покупки в магазине", "нужно сходить до 15:00");
        taskManager.add(epic);

        Subtask subtask1 = new Subtask("покупки в магазине", "нужно сходить до 15:00", epic);
        taskManager.add(subtask1);

        Subtask subtask2 = new Subtask("покупки в магазине", "нужно сходить до 15:00", epic);
        taskManager.add(subtask2);

        final Map<Integer, Subtask> map = epic.getSubtasks();
        final List<Integer> list = epic.getSubtasksIds();
        assertEquals(2,map.size(), "неверное кол-во подзадач");
        assertEquals(2,list.size(), "неверное кол-во подзадач");
    }

    @Test
    public void statusOfEpicTest() {
        Epic epic1 = new Epic("ep1", "des");
        taskManager.add(epic1);
        assertSame(epic1.getStatus(), Status.NEW, "статус эпика без подзадач неверный");

        Subtask subtask1 = new Subtask("", "", epic1);
        Subtask subtask2 = new Subtask("", "", epic1);
        taskManager.add(subtask1);
        taskManager.add(subtask2);
        assertSame(epic1.getStatus(), Status.NEW, "статус эпика с подзадачами неверный");

        subtask1.setStatus(Status.DONE);
        subtask2.setStatus(Status.DONE);
        taskManager.update(subtask1);
        taskManager.update(subtask2);
        assertSame(epic1.getStatus(), Status.DONE, "статус эпика с завершенными подзадачами неверный");

        subtask1.setStatus(Status.IN_PROGRESS);
        taskManager.update(subtask1);
        assertSame(epic1.getStatus(), Status.IN_PROGRESS, "статус эпика с подзадачами в прогрессе неверный");
    }

    @Test
    public void statusOfTaskTest() {
        Task task = new Task("t1", "des");
        taskManager.add(task);
        assertSame(task.getStatus(), Status.NEW, "статус неверный");

        task.setStatus(Status.IN_PROGRESS);
        assertSame(task.getStatus(), Status.IN_PROGRESS, "статус неверный");

        task.setStatus(Status.DONE);
        assertSame(task.getStatus(), Status.DONE, "статус неверный");
    }

    @Test
    void getTaskById() {
        Task task = new Task("покупки в магазине", "нужно сходить до 15:00");
        taskManager.add(task);

        final Task savedTask = taskManager.getTaskById(task.getId());

        assertNotNull(savedTask, "задача не найдена");
        assertEquals(task, savedTask, "задачи не совпадают");
    }

    @Test
    void getEpicById() {
        Epic epic = new Epic("покупки в магазине", "нужно сходить до 15:00");
        taskManager.add(epic);

        final Epic savedEpic = (Epic) taskManager.getTaskById(epic.getId());

        assertNotNull(savedEpic, "эпик не найден");
        assertEquals(epic, savedEpic, "эпики не совпадают");
    }
    @Test
    void getSubtaskById(){
        Epic epic = new Epic("покупки в магазине", "нужно сходить до 15:00");
        taskManager.add(epic);

        Subtask subtask = new Subtask("покупки в магазине", "нужно сходить до 15:00", epic);
        taskManager.add(subtask);

        final Subtask savedSubtask = (Subtask) taskManager.getTaskById(subtask.getId());

        assertNotNull(savedSubtask, "подзадача не найдена");
        assertEquals(subtask,savedSubtask, "подзадачи не совпадают");
    }

    @Test
    public void testGetByIdFailure() {
        assertThrows(TaskNotFoundException.class, () -> taskManager.getTaskById(17000));
    }

    @Test
    public void getAllTasks() {
        Task task = new Task("покупки в магазине", "нужно сходить до 15:00");
        taskManager.add(task);

        Epic epic = new Epic("покупки в магазине", "нужно сходить до 15:00");
        taskManager.add(epic);

        Subtask subtask = new Subtask("покупки в магазине", "нужно сходить до 15:00", epic);
        taskManager.add(subtask);

        final List<Task> tasks = taskManager.getAllTasks();

        assertEquals(3,tasks.size(), "неверное кол-во задач");
    }

    @Test
    public void removeAllTasks() {
        Task task = new Task("покупки в магазине", "нужно сходить до 15:00");
        taskManager.add(task);

        Epic epic = new Epic("покупки в магазине", "нужно сходить до 15:00");
        taskManager.add(epic);

        Subtask subtask = new Subtask("покупки в магазине", "нужно сходить до 15:00", epic);
        taskManager.add(subtask);

        taskManager.removeAllTasks();
        final List<Task> tasks = taskManager.getAllTasks();

        assertEquals(0,tasks.size(), "все задачи не были удалены");
    }

    @Test
    public void removeTaskById() {
        Task task1 = new Task("покупки в магазине", "нужно сходить до 15:00");
        taskManager.add(task1);

        Task task2 = new Task("покупки в магазине", "нужно сходить до 15:00");
        taskManager.add(task2);

        taskManager.removeById(task1.getId());
        final List<Task> tasks = taskManager.getAllTasks();

        assertEquals(1, tasks.size(), "задача не была удалена");
    }

    @Test
    public void removeEpicByIdWithSubtask() {
        Epic epic = new Epic("покупки в магазине", "нужно сходить до 15:00");
        taskManager.add(epic);

        Subtask subtask = new Subtask("покупки в магазине", "нужно сходить до 15:00", epic);
        taskManager.add(subtask);

        taskManager.removeById(epic.getId());
        final List<Task> tasks = taskManager.getAllTasks();

        assertEquals(0, tasks.size(), "задача не была удалена");
    }

    @Test
    public void removeEpicByIdWithoutSubtask() {
        Epic epic = new Epic("покупки в магазине", "нужно сходить до 15:00");
        taskManager.add(epic);

        taskManager.removeById(epic.getId());
        final List<Task> tasks = taskManager.getAllTasks();

        assertEquals(0, tasks.size(), "задача не была удалена");
    }

    @Test
    public void removeSubtaskById() {
        Epic epic = new Epic("покупки в магазине", "нужно сходить до 15:00");
        taskManager.add(epic);

        Subtask subtask1 = new Subtask("покупки в магазине", "нужно сходить до 15:00", epic);
        taskManager.add(subtask1);

        Subtask subtask2 = new Subtask("покупки в магазине", "нужно сходить до 15:00", epic);
        taskManager.add(subtask2);

        taskManager.removeById(subtask1.getId());

        final List<Task> list = taskManager.getAllTasks();
        final List<Integer> ids = epic.getSubtasksIds();

        assertEquals(2,list.size(), "неверное кол-во задач");
        assertEquals(1,ids.size(), "неверное кол-во подзадач у эпика");
    }

    @Test
    public void updateTask() {
        Task task1 = new Task("покупки в магазине", "нужно сходить до 15:00");
        taskManager.add(task1);

        Task task2 = new Task("покупки в магазине", "нужно сходить до 15:00");
        taskManager.add(task2);

        task1.setTitle("покупки");
        taskManager.update(task1);

        task2.setDescription("завтра");
        taskManager.update(task2);

        assertEquals("покупки", task1.getTitle(), "название задачи не обновилось");
        assertEquals("завтра", task2.getDescription(), "описание задачи не обновилось");
    }

    @Test
    public void updateEpic() {
        Epic epic = new Epic("покупки в магазине", "нужно сходить до 15:00");
        taskManager.add(epic);

        epic.setTitle("покупки");
        taskManager.update(epic);

        assertEquals("покупки", epic.getTitle(), "название эпика не обновилось");
    }

    @Test
    public void updateSubtask() {
        Epic epic = new Epic("покупки в магазине", "нужно сходить до 15:00");
        taskManager.add(epic);

        Subtask subtask = new Subtask("покупки в магазине", "нужно сходить до 15:00", epic);
        taskManager.add(subtask);

        subtask.setTitle("покупки");
        taskManager.update(subtask);

        assertEquals("покупки", subtask.getTitle(), "название подзадачи не обновилось");
    }

    @Test
    public void getPrioritizedTasks() {
        Task task1 = new Task(1000, Type.TASK, "task1", "task1", Status.IN_PROGRESS, 10, LocalDateTime.of (2016, 1, 4, 16, 30));
        taskManager.add(task1);

        Epic epic = new Epic(2000, Type.EPIC, "1epic", "1epic", Status.IN_PROGRESS, 0, LocalDateTime.of (2016, 1, 4, 11, 30));
        taskManager.add(epic);

        epic.setStartTime(LocalDateTime.of ( 2016, 1, 4, 17, 0));
        taskManager.update(epic);

        Subtask subtask = new Subtask("покупки в магазине", "нужно сходить до 15:00", epic);
        taskManager.add(subtask);

        subtask.setStartTime(LocalDateTime.of ( 2018, 1, 4, 16, 30));
        taskManager.update(subtask);

        Task task2 = new Task("покупки в магазине", "нужно сходить до 15:00");
        taskManager.add(task2);

        final Set<Task> tasks = taskManager.getPrioritizedTasks();
        assertThat(tasks).containsExactly(taskManager.getTaskById(task1.getId()), taskManager.getTaskById(subtask.getId()), taskManager.getTaskById(epic.getId()), taskManager.getTaskById(task2.getId()));
    }

    @Test()
    public void checkValidTimeInterval() {
        Task task1 = new Task("task1", "task1", LocalDateTime.of(2016, 1, 4, 16, 30));
        task1.setDuration(60);
        taskManager.add(task1);

        Task task2 = new Task("222", "222", LocalDateTime.of(2016, 1, 4, 16, 31));
        assertThrows(WrongTimeIntervalException.class, () -> taskManager.add(task2));

        Epic epic = new Epic("1epic", "1epic");
        epic.setStartTime(LocalDateTime.of(2016, 1, 4, 17, 31));
        assertDoesNotThrow(() -> taskManager.add(epic));

        Subtask subtask1 = new Subtask("sub1", "sub1", epic);
        taskManager.add(subtask1);
        subtask1.setStartTime(LocalDateTime.of(2016, 1, 4, 17, 31));
        subtask1.setDuration(120);
        assertDoesNotThrow(() -> taskManager.update(subtask1));

        Subtask subtask2 = new Subtask("sub2", "sub2", epic);
        taskManager.add(subtask2);
        subtask2.setStartTime(LocalDateTime.of(2016, 1, 4, 18, 50));
        assertThrows(WrongTimeIntervalException.class, () -> taskManager.update(subtask2));

        // duration эпика 2 часа
        Task task3 = new Task("task3", "task3", LocalDateTime.of(2016, 1, 4, 20, 0));
        task1.setDuration(60);
        assertDoesNotThrow(() -> taskManager.add(task3));

        subtask2.setStartTime(LocalDateTime.of(2016, 1, 4, 20, 1));
        assertDoesNotThrow(() -> taskManager.update(subtask2));
    }

    @Test
    public void checkEpicValidTimeInterval() {
        Epic epic = new Epic("1epic", "1epic");
        taskManager.add(epic);

        Subtask subtask1 = new Subtask("sub1", "sub1", epic);
        taskManager.add(subtask1);
        subtask1.setStartTime(LocalDateTime.of (2016, 1, 4, 17, 30));
        subtask1.setDuration(30);
        taskManager.update(subtask1);

        Subtask subtask2 = new Subtask("sub1", "sub1", epic);
        taskManager.add(subtask2);
        subtask2.setStartTime(LocalDateTime.of (2016, 1, 4, 19, 0));
        subtask2.setDuration(30);
        taskManager.update(subtask2);

        assertEquals(LocalDateTime.of (2016, 1, 4, 19, 30), epic.getEndTime());
    }

    // todo EPIC SUBTASK (интервал и новые поля)
    @Test()
    public void checkValidTimeIntervalAddTaskFailure() {
        Task task1 = new Task("task1", "task1", LocalDateTime.of (2016, 1, 4, 16, 30));
        taskManager.add(task1);

        Task task2 = new Task("222", "222", LocalDateTime.of (2016, 1, 4, 16, 30));
        assertThrows(WrongTimeIntervalException.class, () -> taskManager.add(task2));
    }

    @Test()
    public void checkValidTimeIntervalUpdateTaskFailure() {
        Task task1 = new Task("task1", "task1", LocalDateTime.of (2016, 1, 4, 16, 30));
        taskManager.add(task1);

        Task task2 = new Task("222", "222", LocalDateTime.of (2016, 1, 4, 17, 30));
        taskManager.add(task2);
        task2.setStartTime(LocalDateTime.of (2016, 1, 4, 16, 30));

        assertThrows(WrongTimeIntervalException.class, () -> taskManager.update(task2));
    }

    @Test
    public void checkEpicStartTime() {
        Epic epic = new Epic("1epic", "1epic");
        taskManager.add(epic);

        Subtask subtask1 = new Subtask("sub1", "sub1", epic);
        taskManager.add(subtask1);

        Subtask subtask2 = new Subtask("sub2", "sub2", epic);
        taskManager.add(subtask2);

        subtask1.setStartTime(LocalDateTime.of (2016, 1, 4, 14, 30));
        subtask1.setDuration(30);
        taskManager.update(subtask1);

        assertEquals(LocalDateTime.of (2016, 1, 4, 14, 30), epic.getStartTime());

        subtask2.setStartTime(LocalDateTime.of (2016, 1, 4, 12, 30));
        subtask2.setDuration(30);
        taskManager.update(subtask2);

        assertEquals(LocalDateTime.of (2016, 1, 4, 12, 30), epic.getStartTime());
    }

    @Test
    public void checkEpicEndTimeAndDuration() {
        Epic epic = new Epic("1epic", "1epic");
        epic.setDuration(60);
        taskManager.add(epic);
        assertEquals(60, epic.getDuration());   // todo должно ли duration затираться?

        Subtask subtask1 = new Subtask("sub1", "sub1", epic);
        taskManager.add(subtask1);
        subtask1.setStartTime(LocalDateTime.of (2016, 1, 4, 17, 30));
        subtask1.setDuration(30);

        Subtask subtask2 = new Subtask("sub2", "sub2", epic);
        taskManager.add(subtask2);
        subtask2.setStartTime(LocalDateTime.of (2016, 1, 4, 18, 30));
        subtask2.setDuration(15);

        taskManager.update(subtask1);
        taskManager.update(subtask2);

        assertEquals(30, subtask1.getDuration());
        assertEquals(45, epic.getDuration());    // todo ?

        assertEquals(LocalDateTime.of (2016, 1, 4, 18, 45), epic.getEndTime());
    }

    @Test
    public void checkTaskStartTime() {
        Task task1 = new Task("task1", "task1", LocalDateTime.of (2016, 1, 4, 16, 30));
        taskManager.add(task1);

        Task task2 = new Task("222", "222", LocalDateTime.of (2016, 1, 4, 17, 30));
        taskManager.add(task2);
        task2.setStartTime(LocalDateTime.of (2016, 1, 4, 16, 30));

        assertThrows(WrongTimeIntervalException.class, () -> taskManager.update(task2));
    }
}
