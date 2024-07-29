import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import service.HistoryManager;
import service.TaskManager;
import utils.Managers;


public class Main {
    public static void main(String[] args) {
        TaskManager<Task> taskManager = Managers.getDefault();
        HistoryManager<Task> historyManager = Managers.getDefaultHistory();
        Task task1 = new Task("111", "111");
        Task task2 = new Task("222", "222");
        Task task3 = new Task("234", "updated");
        Task task4 = new Task("234", "updated");

        Epic epic1 = new Epic("1epic", "1epic");
        Epic epic2 = new Epic("2epic", "2epic");


        Subtask subtask1 = new Subtask("1sub", "1sub", epic1);
        Subtask subtask2 = new Subtask("2sub", "2sub", epic1);
        Subtask subtask3 = new Subtask("3sub", "3sub", epic2);

        create(taskManager, task1, task2, epic1, epic2, subtask1, subtask2, subtask3);

        taskManager.add(task3);
        taskManager.add(task4);

        get(taskManager, epic1);
        taskManager.getTaskById(1);
        taskManager.getTaskById(8);
        taskManager.getTaskById(9);
        taskManager.getTaskById(2);
        taskManager.getTaskById(1);
        taskManager.getTaskById(3);

        System.out.println();
        System.out.println("History: " + historyManager.getHistory());
        System.out.println();

        update(task1, taskManager);

        subtask1.setStatus(Status.IN_PROGRESS);
        taskManager.update(subtask1);

        System.out.println("Updated task: " + task1 + "\nUpdated epic: " + epic1);
        System.out.println(taskManager.getAllTasks());

        delete(taskManager);
        taskManager.getAllTasks();
    }

    private static void update(Task task1, TaskManager<Task> manager) {
        task1.setTitle("updatedTitle");
        task1.setStatus(Status.DONE);
        manager.update(task1);
    }

    private static void delete(TaskManager<Task> manager) {
        manager.removeById(3);
        manager.removeAllTasks();
        System.out.println("All tasks removed");
    }

    private static void create(TaskManager<Task> manager, Task task1, Task task2, Epic epic1, Epic epic2, Subtask subtask1, Subtask subtask2, Subtask subtask3) {
        manager.add(task1);
        manager.add(task2);

        manager.add(epic1);
        manager.addSubtask(subtask1, epic1);
        manager.addSubtask(subtask2, epic1);

        manager.add(epic2);
        manager.addSubtask(subtask3, epic2);

        System.out.println("Created tasks: " + manager.getAllTasks());
    }

    private static void get(TaskManager<Task> manager, Epic epic1) {
        //manager.getTaskById(1);
        System.out.println("Subs of epic1: " + manager.getSubTasks(epic1));
        System.out.println();
        System.out.println("All tasks:" + manager.getAllTasks());
        System.out.println("All Subs: " + manager.getAllSubtasks());
        System.out.println("All Epics: " + manager.getAllEpics());
    }

    private static void mod() {
        /* Модификаторы доступа
            public - видимость во всем проекте
            private - видимость в рамках класса
            protected - видимость в рамках класса и дочерних классов
            default - видимость только в пакете
        * */


        /*
            Epic Status зависит от subTasks:
                1) New -> New
                2) Done -> Done
                3) In_progress -> in_progress
                4) New && Done -> in_progress
                5) In_progress && Done -> in_progress
                6) In_progress && New -> in_progress
         */
    }

}