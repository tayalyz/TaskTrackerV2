import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        InMemoryTaskManagerTest.class,
        InMemoryHistoryManagerTest.class })

public class TestRunner {

}
