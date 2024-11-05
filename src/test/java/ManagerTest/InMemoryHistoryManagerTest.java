package ManagerTest;

import org.junit.jupiter.api.BeforeEach;
import service.InMemoryHistoryManager;
import service.InMemoryTaskManager;

public class InMemoryHistoryManagerTest extends HistoryManagerTest {

    @BeforeEach
    public void createManager() {
        taskManager = new InMemoryTaskManager<>();
        historyManager = new InMemoryHistoryManager<>();
    }
}
