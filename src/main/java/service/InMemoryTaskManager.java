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
    public T add(T task) {
        if (task instanceof Subtask) {
            Epic epic = ((Subtask) task).getParent();
            epic.addSubtask((Subtask) task);
            epic.updateStatus();
        }
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
        System.out.println("All tasks deleted");
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
                Epic epic = ((Subtask) task).getParent();
                epic.removeSubtaskById(task.getId());
            }
            historyManager.remove(id);
            tasks.remove(id);
        } else {
            System.err.println("Task does not exist");
        }
    }

    @Override
    public Optional<T> update(T task) {     // TODO
        if (tasks.containsKey(task.getId())) {
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
    public List<T> getAllTasks() {
        if (!tasks.isEmpty()) {
            return new ArrayList<>(tasks.values());
        } else {
            System.err.println("No tasks found");
        }
        return List.of();
    }
}
