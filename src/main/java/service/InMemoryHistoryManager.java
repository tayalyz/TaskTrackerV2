package service;

import model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager<T extends Task> implements HistoryManager<T> {
    private Node<T> head;
    private Node<T> tail;
    private Map<Integer, Node<T>> nodes;
    private int size;

    public InMemoryHistoryManager() {
        this.head = null;
        this.tail = null;
        this.nodes = new HashMap<>();
        this.size = 0;
    }

    @Override
    public void add(T task) {
        remove(task.getId());
        linkLast(task);
    }

    private List<T> getTasks(){
        List<T> tasks = new ArrayList<>();
        Node<T> node = head;

        while (node != null) {
            tasks.add(node.getData());
            node = node.getNext();
        }
        return tasks;
    }

    @Override
    public List<T> getHistory() {
        if (getTasks().isEmpty()) {
            System.out.println("History is empty");
        }
        return getTasks();
    }

    public void linkLast(T task) {
        Node<T> node = new Node<>(task);

        if (size == 0) {
            head = node;
        } else {
            node.setPrev(tail);
            tail.setNext(node);
        }

        tail = node;
        nodes.put(task.getId(), node);
        size++;
    }

    @Override
    public void remove(int id) {
        if (nodes.containsKey(id)) {
            removeNode(nodes.get(id));
            nodes.remove(id);
        }
    }

    private void removeNode(Node<T> node) {
        if (node != head) {
            if (node.getPrev() != null) {
                node.getPrev().setNext(node.getNext());
            }
        } else {
            head=node.getNext();
        }

        if (node == tail) {
            tail = node.getPrev();
        } else {
            node.getNext().setPrev(node.getPrev());
        }

        size--;
    }
}
