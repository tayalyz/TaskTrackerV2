package service;

import exception.FailedToConvertException;
import exception.ManagerSaveException;
import model.*;
import utils.Managers;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class FileBackedTasksManager<T extends Task> extends InMemoryTaskManager<T> {
    private String fileName;

    public FileBackedTasksManager(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public T getTaskById(int id) {
        T newTask = super.getTaskById(id);
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
            writer.write("id, " + "type, " + "title, "+ "description, " + "status, " + "parentId" + "\n");

            for (T task : getAllTasks()) {
                writer.write(toString(task));
            }
            writer.write("\n");
            writer.write(historyToString(historyManager));

        } catch (IOException e) {
            throw new ManagerSaveException("Failed to save file");
        }
    }

    private static FileBackedTasksManager<Task> loadFromFile(String file) {
        FileBackedTasksManager<Task> fileBackedTasksManager = new FileBackedTasksManager<>(file);
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                fileBackedTasksManager.fromString(line);
                if (line.isEmpty()) {
                    line = reader.readLine();
                    fileBackedTasksManager.historyFromString(line);
                    break;
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Failed to load file");
        }
        return fileBackedTasksManager;
    }

    private String historyToString(HistoryManager<Task> manager) {
        List<String> history = new ArrayList<>();
        for (Task task : historyManager.getHistory()) {
            history.add(String.valueOf(task.getId()));
        }
        return String.join(", ", history);
    }

    public List<Integer> historyFromString(String value) {
        if (value == null) {
            throw new FailedToConvertException("Failed to convert history from string");
        }

        String[] parts = value.split(",\\s*");
        for (String part : parts) {
            if (!part.isBlank()) {
                getTaskById(Integer.parseInt(part));
            }
        }
        return null;
    }

    private String toString(T task) {
        if (task instanceof Epic) {
            return task.getId() + ", " + task.getType() + ", " + task.getTitle()+ ", " + task.getDescription()
                    + ", " + task.getStatus() + "\n";
        } else if (task instanceof Subtask) {
            return task.getId() + ", " + task.getType() + ", " + task.getTitle()+ ", " + task.getDescription()
                    + ", " + task.getStatus() + ", " + ((Subtask) task).getParent().getId() + "\n";
        } else {
            return task.getId() + ", " + task.getType() + ", " + task.getTitle()+ ", " + task.getDescription()
                    + ", " + task.getStatus() + "\n";
        }
    }

    private void fromString(String value) {
        String[] parts = value.split(",\\s*");

        Type type = Type.valueOfType(parts[1]);
        if (value.isBlank() && type == null) {
            throw new FailedToConvertException("Failed to convert task from string");
        }

        switch (type) {
            case EPIC:
                Epic epic = new Epic(Integer.parseInt(parts[0]), Type.valueOf(parts[1]), parts[2], parts[3], Status.valueOf(parts[4]));
                add((T) epic);
                break;
            case SUBTASK:
                Subtask subtask = new Subtask(Integer.parseInt(parts[0]), Type.valueOf(parts[1]), parts[2], parts[3],
                        Status.valueOf(parts[4]),(Epic)getTaskById(Integer.parseInt(parts[5])));
                historyManager.remove(subtask.getParent().getId());
                add((T) subtask);
                break;
            case TASK:
                Task task = new Task(Integer.parseInt(parts[0]), Type.valueOf(parts[1]), parts[2], parts[3], Status.valueOf(parts[4]));
                add((T) task);
                break;
            default:
                throw new FailedToConvertException("Failed to convert task from string"); //todo
        }
    }

    public static void main(String[] args) {
        FileBackedTasksManager<Task> fileBackedTasksManager = loadFromFile("data.csv");
        HistoryManager<Task> historyManager1 = Managers.getDefaultHistory();
        System.out.println(fileBackedTasksManager.readFile());
        System.out.println(fileBackedTasksManager.getAllTasks());
        Task task = fileBackedTasksManager.getTaskById(2);

        System.out.println("history:" + historyManager1.getHistory());
        task.setTitle("aaaaa");
        fileBackedTasksManager.update(task);
        fileBackedTasksManager.getTaskById(5);
        fileBackedTasksManager.getTaskById(6);
        System.out.println("history:" + historyManager1.getHistory());
        System.out.println(fileBackedTasksManager.getAllTasks());
        System.out.println(fileBackedTasksManager.readFile());
    }
}
