package ManagerTest;

import org.junit.jupiter.api.BeforeEach;
import service.InMemoryTaskManager;

public class InMemoryTaskManagerTest extends TaskManagerTest {

    @BeforeEach
    public void createManager() {
        taskManager = new InMemoryTaskManager<>();
    }

}
