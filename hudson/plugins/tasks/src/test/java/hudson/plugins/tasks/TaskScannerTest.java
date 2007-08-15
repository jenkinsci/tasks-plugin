package hudson.plugins.tasks;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.Test;

public class TaskScannerTest {
    @Test
    public void scanFileWithTasksAndDefaults() throws IOException {
        InputStream file = TaskScannerTest.class.getResourceAsStream("file-with-tasks.txt");

        JavaFile result = new TaskScanner().scan(file);
        assertEquals(2, result.getNumberOfTasks());
        List<Task> tasks = result.getTasks();
        assertEquals(2, tasks.size());
        assertEquals("here we have a task with priority NORMAL", tasks.get(0).getMessage());
        assertEquals("here another task with priority HIGH", tasks.get(1).getMessage());
    }

    @Test
    public void testPriorities() throws IOException {
        InputStream file = TaskScannerTest.class.getResourceAsStream("file-with-tasks.txt");

        JavaFile result = new TaskScanner().scan(file);

        assertEquals(1, result.getNumberOfTasks(Task.Priority.HIGH));
        assertEquals(1, result.getNumberOfTasks(Task.Priority.NORMAL));
        assertEquals(0, result.getNumberOfTasks(Task.Priority.LOW));
    }

    @Test
    public void testHighPriority() throws IOException {
        InputStream file = TaskScannerTest.class.getResourceAsStream("file-with-tasks.txt");

        JavaFile result = new TaskScanner("FIXME", null, null).scan(file);

        assertEquals(1, result.getNumberOfTasks(Task.Priority.HIGH));
        assertEquals(0, result.getNumberOfTasks(Task.Priority.NORMAL));
        assertEquals(0, result.getNumberOfTasks(Task.Priority.LOW));
    }

    @Test
    public void testTwoItemsWithWhiteSpaceAndHighPriority() throws IOException {
        InputStream file = TaskScannerTest.class.getResourceAsStream("file-with-tasks.txt");

        JavaFile result = new TaskScanner(" FIXME , TODO ", null, null).scan(file);

        assertEquals(2, result.getNumberOfTasks(Task.Priority.HIGH));
        assertEquals(0, result.getNumberOfTasks(Task.Priority.NORMAL));
        assertEquals(0, result.getNumberOfTasks(Task.Priority.LOW));
    }

    @Test
    public void testTwoItemsWithHighPriority() throws IOException {
        InputStream file = TaskScannerTest.class.getResourceAsStream("file-with-tasks.txt");

        JavaFile result = new TaskScanner("FIXME,TODO", null, null).scan(file);

        assertEquals(2, result.getNumberOfTasks(Task.Priority.HIGH));
        assertEquals(0, result.getNumberOfTasks(Task.Priority.NORMAL));
        assertEquals(0, result.getNumberOfTasks(Task.Priority.LOW));
    }

    @Test
    public void testAllPriorities() throws IOException {
        InputStream file = TaskScannerTest.class.getResourceAsStream("file-with-tasks.txt");

        JavaFile result = new TaskScanner("FIXME", "FIXME,TODO", "TODO").scan(file);

        assertEquals(1, result.getNumberOfTasks(Task.Priority.HIGH));
        assertEquals(2, result.getNumberOfTasks(Task.Priority.NORMAL));
        assertEquals(1, result.getNumberOfTasks(Task.Priority.LOW));

    }

    @Test
    public void scanFileWithoutTasks() throws IOException {
        InputStream file = TaskScannerTest.class.getResourceAsStream("file-without-tasks.txt");

        JavaFile result = new TaskScanner().scan(file);
        assertEquals(0, result.getNumberOfTasks());

    }
}

