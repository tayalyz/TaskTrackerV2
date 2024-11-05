package service;

import exception.FailedToConvertException;
import exception.ManagerSaveException;
import model.*;
import utils.Managers;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

public class FileBackedTasksManager<T extends Task> extends InMemoryTaskManager<T> {
    private final String fileName;

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

    public String readFile() {
        try {
            return Files.readString(Path.of(fileName));
        } catch (IOException e) {
            throw new ManagerSaveException("Failed to read");
        }
    }

    protected void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write("id, " + "type, " + "title, " + "description, " + "status, " + "duration, " +
                    "startTime, " + "parentId" + "\n");

            for (T task : getAllTasks()) {
                writer.write(toString(task));
            }
            writer.write("\n");
            writer.write(historyToString());

        } catch (IOException e) {
            throw new ManagerSaveException("Failed to save file");
        }
    }

    public static FileBackedTasksManager<Task> loadFromFile(String file) {
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

    private String historyToString() {
        List<String> history = new ArrayList<>();
        historyManager.getHistory().forEach(task -> history.add(String.valueOf(task.getId())));

        return String.join(", ", history);
    }

    private void historyFromString(String value) {
        if (value == null) {
            throw new FailedToConvertException("Failed to convert history from string");
        }

        Arrays.stream(value.split(",\\s*"))
                .mapToInt(Integer::parseInt)
                .forEach(this::getTaskById);
    }

    private String toString(T task) {
        if (task instanceof Epic) {
            return task.getId() + ", " + task.getType() + ", " + task.getTitle() + ", " + task.getDescription()
                    + ", " + task.getStatus() + ", " + task.getDuration()
                    + ", " + (task.getStartTime() != null ? task.getStartTime() : "0") + "\n";
        } else if (task instanceof Subtask) {
            return task.getId() + ", " + task.getType() + ", " + task.getTitle() + ", " + task.getDescription()
                    + ", " + task.getStatus() + ", " + task.getDuration()
                    + ", " + (task.getStartTime() != null ? task.getStartTime() + ", " : "0, ")
                    + ((Subtask) task).getParentId() + "\n";
        } else {
            return task.getId() + ", " + task.getType() + ", " + task.getTitle() + ", " + task.getDescription()
                    + ", " + task.getStatus() + ", " + task.getDuration()
                    + ", " + (task.getStartTime() != null ? task.getStartTime() : "0") + "\n";
        }
    }

    private void fromString(String value) {
        String[] parts = value.split(",\\s*");

        if (!value.isBlank() && Type.valueOfType(parts[1]) != null) {
            switch (Type.valueOfType(parts[1])) {
                case EPIC:
                    Epic epic = new Epic(Integer.parseInt(parts[0]),
                            Type.valueOf(parts[1]), parts[2], parts[3],
                            Status.valueOf(parts[4]), Integer.parseInt(parts[5]),
                            !parts[6].equals("0") ? LocalDateTime.parse(parts[6]) : null);
                    add((T) epic);
                    break;
                case SUBTASK:
                    Subtask subtask = new Subtask(Integer.parseInt(parts[0]),
                            Type.valueOf(parts[1]), parts[2], parts[3],
                            Status.valueOf(parts[4]), Integer.parseInt(parts[5]),
                            !parts[6].equals("0") ? LocalDateTime.parse(parts[6]) : null,
                            getTaskById(Integer.parseInt(parts[7])).getId());
                    historyManager.remove(subtask.getParentId());
                    add((T) subtask);
                    break;
                case TASK:
                    Task task = new Task(Integer.parseInt(parts[0]),
                            Type.valueOf(parts[1]), parts[2], parts[3],
                            Status.valueOf(parts[4]), Integer.parseInt(parts[5]),
                            !parts[6].equals("0") ? LocalDateTime.parse(parts[6]) : null);
                    add((T) task);
                    break;
                default:
                    throw new FailedToConvertException("Failed to convert task from string");
            }
        }
    }

    public static void main(String[] args) {
        FileBackedTasksManager<Task> fileBackedTasksManager = loadFromFile("data.csv");
        HistoryManager<Task> historyManager = Managers.getDefaultHistory();
        System.out.println(fileBackedTasksManager.readFile());
        System.out.println(fileBackedTasksManager.getAllTasks());

        Task task = fileBackedTasksManager.getTaskById(2);

        System.out.println("history:" + historyManager.getHistory());
        task.setTitle("aaaaa");
        fileBackedTasksManager.update(task);
        fileBackedTasksManager.getTaskById(5);
        fileBackedTasksManager.getTaskById(6);
        System.out.println("history:" + historyManager.getHistory());
        System.out.println(fileBackedTasksManager.getAllTasks());
        System.out.println(fileBackedTasksManager.readFile());
    }
}
