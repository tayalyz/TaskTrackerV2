package service;

import exception.ManagerSaveException;
import model.*;
import utils.Managers;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class FileBackedTasksManager<T extends Task> extends InMemoryTaskManager<T> {
    private String fileName;

    public FileBackedTasksManager(String fileName) {
        this.fileName = fileName;
        loadFromFile(new File(fileName));
    }

    @Override
    public T getTaskById(int id) {
        T newTask = super.getTaskById(id);
        save();
        return newTask;
    }

    @Override
    public Subtask addSubtask(Subtask subtask, Epic epic) {
        Subtask newTask = super.addSubtask(subtask, epic);
        save();
        return newTask;
    }

    @Override
    public T add(T task) {
        T newTask = super.add(task);
        save();
        return newTask;
    }

    @Override
    public void removeAllTasks() {
        super.removeAllTasks();
        save();
    }

    @Override
    public void removeById(int id) {
        super.removeById(id);
        save();
    }

    @Override
    public Optional<T> update(T task) {
        Optional<T> newTask = super.update(task);
        save();
        return newTask;
    }

    // Создайте enum с типами задач.
    //Напишите метод сохранения задачи в строку String toString(Task task) или переопределите базовый.
    //Напишите метод создания задачи из строки Task fromString(String value).
    //Напишите статические методы static String historyToString(HistoryManager manager)
    // и static List<Integer> historyFromString(String value) для сохранения и восстановления менеджера истории из CSV.

    public String readFile() {
        try {
            return Files.readString(Path.of(fileName));
        } catch (IOException e) {
            throw new ManagerSaveException("Failed to read");
        }
    }

    private void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write("id, " + "type, " + "title, "+ "description, " + "status, " + "\n");

            for (T task : getAllTasks()) {
                writer.write(toString(task));
            }
            writer.write("\n");
            writer.write(historyToString(historyManager));

        } catch (IOException e) {
            throw new ManagerSaveException("Failed to save");
        }
    }

    private FileBackedTasksManager<T> loadFromFile(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                fromString(line);
                if (line.isBlank()) {
                    line = reader.readLine();
                    historyFromString(line);
                    break;
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Failed to load file");
        }
        return this;
    }

    private String historyToString(HistoryManager<Task> manager) {
        List<String> line1 = new ArrayList<>();
        for (Task task : Managers.getDefaultHistory().getHistory()) {
            line1.add(String.valueOf(task.getId()));
        }
        return String.join(", ", line1);
    }

    public List<Integer> historyFromString(String value) {
        if (value == null) {
            throw new ManagerSaveException("Failed to convert history from string");
        }
        String[] parts = value.split(",\\s*");
        for (String part : parts) {
            getTaskById(Integer.parseInt(part));
        }
        return null;
    }

    private String toString(T task) {
        if (task instanceof Epic) {
            return task.getId() + ", " + task.getType() + ", " + task.getTitle()+ ", " + task.getDescription()
                    + ", " + task.getStatus() + ", " + ((Epic) task).getSubtasks().values().stream()
                    .map(i -> i.getId() + ", " + i.getType() + ", " + i.getTitle()+ ", " + i.getDescription() + ", " + i.getStatus() +
                            ", " + i.getParent().getId())
                    .collect(Collectors.joining(", ")) + "\n";
        } else if (task instanceof Subtask) {
            return task.getId() + ", " + task.getType() + ", " + task.getTitle()+ ", " + task.getDescription()
                    + ", " + task.getStatus() + ", " + ((Subtask) task).getParent().getId() + "\n";
        } else {
            return task.getId() + ", " + task.getType() + ", " + task.getTitle()+ ", " + task.getDescription()
                    + ", " + task.getStatus() + "\n";
        }
    }

    private T fromString(String value) {
        if (value == null) {
            throw new ManagerSaveException("Failed to convert task from string");
        }
        if (value.isEmpty()) {
            return null;
        }
        String[] parts = value.split(",\\s*");    //TODO
        if (Objects.equals(parts[1], "EPIC")) {
            Epic epic = new Epic(parts[2], parts[3]);
            epic.setId(Integer.parseInt(parts[0]));
            epic.setStatus(Status.valueOf(parts[4]));

            if (parts.length > 6) {
                for (int i = 6; i < parts.length; i++) {
                    if (parts[i].equals("SUBTASK")) {
                        Subtask subtask = new Subtask(parts[i + 1], parts[i + 2], epic);
                        subtask.setId(Integer.parseInt(parts[i - 1]));
                        subtask.setStatus(Status.valueOf(parts[i + 3]));
                        addSubtask(subtask, epic);
                    }
                }
            }
            return add((T) epic);    //TODO
        }
//        } else if (Objects.equals(parts[1], "SUBTASK")){
//            Subtask subtask = new Subtask(parts[1], parts[2], (Epic) getTaskById(Integer.parseInt(parts[5]))); //TODO ?
//            subtask.setId(Integer.parseInt(parts[4]));
//            subtask.setStatus(Status.valueOf(parts[3]));
//            return add((T) subtask);

         if (Objects.equals(parts[1], "TASK") || Objects.equals(parts[1], "SUBTASK")) {
            Task task = new Task(parts[2], parts[3]);
            task.setId(Integer.parseInt(parts[0]));
            task.setStatus(Status.valueOf(parts[4]));
            return add((T) task);
        }
        return null;
    }

    public static void main(String[] args) {
        FileBackedTasksManager<Task> fileBackedTasksManager = new FileBackedTasksManager<>("data.csv");
        HistoryManager<Task> historyManager1 = Managers.getDefaultHistory();
        System.out.println(fileBackedTasksManager.readFile());
        System.out.println(fileBackedTasksManager.getAllTasks());
        Task task = fileBackedTasksManager.getTaskById(2);

        System.out.println("history:" + historyManager1.getHistory());
        task.setTitle("aaaaa");
        fileBackedTasksManager.update(task);
        fileBackedTasksManager.getTaskById(5);
        fileBackedTasksManager.getTaskById(6);

        System.out.println(fileBackedTasksManager.getAllTasks());
    }
}
