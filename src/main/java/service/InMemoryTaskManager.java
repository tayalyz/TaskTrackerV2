package service;//a. Получение списка всех задач.
//b. Удаление всех задач.
//c. Получение по идентификатору.
//d. Создание. Сам объект должен передаваться в качестве параметра.
//e. Обновление. Новая версия объекта с верным идентификатором передаётся в виде параметра.
//f. Удаление по идентификатору.
//Дополнительные методы:
//a. Получение списка всех подзадач определённого эпика.

import exception.TaskNotFoundException;
import model.Epic;
import model.Subtask;
import model.Task;
import utils.Managers;

import java.util.*;

public class InMemoryTaskManager<T extends Task> implements TaskManager<T> {
    private Map<Integer, T> tasks;
    protected final HistoryManager<Task> historyManager;

    public InMemoryTaskManager() {
        this.tasks = new HashMap<>();
        this.historyManager = Managers.getDefaultHistory();
    }

    @Override
    public T getTaskById(int id) {
        T task = Optional.ofNullable(tasks.get(id))
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));
        historyManager.add(task);
        return task;
    }

    @Override
    public Subtask addSubtask(Subtask subtask, Epic epic) {
        tasks.put(subtask.getId(), (T) subtask);
        return epic.addSubtask(subtask);
    }

    @Override
    public T add(T task) {
        return tasks.put(task.getId(), task);
    }

    @Override
    public void removeAllTasks() {
        if (!tasks.isEmpty()) {
            for (T task : tasks.values()) {
                historyManager.remove(task.getId());
            }
            tasks.clear();
        }
        System.out.println("Все задачи удалены");
    }

    @Override
    public void removeById(int id) {
        if (tasks.containsKey(id)) {
            T task = getTaskById(id);
            if (task instanceof Epic && !((Epic)task).getSubtasks().isEmpty()) {
                List<Integer> subtasksIds = ((Epic) task).getSubtasksIds();

                for (int subtaskId: subtasksIds){
                    removeById(subtaskId);
                }
            } else if (task instanceof Subtask) { // TODO проверить если сабтаск и удалить его у epic
                List<Epic> list = getAllEpics();
                for (Epic epic : list) {
                    if (epic.getSubtasksIds().contains(task.getId())) {
                        epic.removeSubtaskById(task.getId());
                    }
                }
            }
            historyManager.remove(id);
            tasks.remove(id);
        } else {
            System.err.println("Task does not exist");
        }
    }

    @Override
    public Optional<T> update(T task) {     // TODO
        if (tasks.containsKey(task.getId()) || getAllSubtasks().stream()
                .anyMatch(subtask -> subtask.getId() == task.getId())) {
            if (task instanceof Subtask) {
                ((Subtask) task).getParent().updateStatus();
            }
            tasks.put(task.getId(), task);
            return Optional.of(task);
        } else {
            System.err.println("Task does not exist");
        }
        return Optional.empty();
    }

    @Override
    public List<Subtask> getSubTasks(Epic epic) {
        List<Subtask> subtasks = new ArrayList<>();
        if (epic.getSubtasks().isEmpty()) {
            System.out.println("This epic does not have subtasks");
        }
        if (getAllEpics().contains(epic)) {
            for (Map.Entry<Integer, Subtask> set : epic.getSubtasks().entrySet()) {
                subtasks.add(set.getValue());
            }
        } else {
            System.err.println("No tasks found");
        }
        return subtasks;
    }

    @Override
    public List<T> getAllTasks() {
        if (!tasks.isEmpty()) {
            return new ArrayList<>(tasks.values());
        } else {
            System.err.println("No tasks found");
        }
        return List.of();
    }

    @Override
    public List<Epic> getAllEpics() {
        List<Epic> epics = new ArrayList<>();
        for (T task : tasks.values()) {
            if (task instanceof Epic) {
                epics.add((Epic) task);
            }
        }

        if (!epics.isEmpty()) {
            return epics;
        } else {
            System.err.println("No epics found");
        }
        return epics;
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        List<Subtask> sub = new ArrayList<>();
        for (Epic epic : getAllEpics()) {
            sub.addAll(getSubTasks(epic));
        }
        if (sub.isEmpty()) {
            System.err.println("No subtasks found");
        }
        return sub;
    }
}
