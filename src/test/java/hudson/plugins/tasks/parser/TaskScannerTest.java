package hudson.plugins.tasks.parser;

import static org.junit.Assert.*;
import hudson.plugins.tasks.util.ParserResult;
import hudson.plugins.tasks.util.model.AnnotationContainer;
import hudson.plugins.tasks.util.model.JavaProject;
import hudson.plugins.tasks.util.model.Priority;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

/**
 * Tests the class {@link TaskScanner}.
 */
public class TaskScannerTest {
    /** High priority. */
    private static final String PRIORITY_HIGH = "here another task with priority HIGH";
    /** Normal priority. */
    private static final String PRIORITY_NORMAL = "here we have a task with priority NORMAL";
    /** Test file. */
    private static final String FILE_WITH_TASKS = "file-with-tasks.txt";
    /** Error message. */
    private static final String WRONG_MESSAGE_ERROR = "Wrong message returned.";
    /** Error message. */
    private static final String WRONG_NUMBER_OF_TASKS_ERROR = "Wrong number of tasks found.";

    /**
     * Checks whether we find tasks at word boundaries.
     *
     * @throws IOException if we can't read the file
     */
    @Test
    public void scanFileWithWords() throws IOException {
        InputStream file = TaskScannerTest.class.getResourceAsStream("tasks-words-test.txt");

        Collection<Task> result = new TaskScanner("WARNING", "TODO", "@todo", false).scan(new InputStreamReader(file));
        assignProperties(result);
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 12, result.size());

        ParserResult parserResult = new ParserResult();
        parserResult.addAnnotations(result);

        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 0, parserResult.getNumberOfAnnotations(Priority.HIGH));
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 7, parserResult.getNumberOfAnnotations(Priority.NORMAL));
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 5, parserResult.getNumberOfAnnotations(Priority.LOW));
    }

    /**
     * Checks case sensitivity.
     *
     * @throws IOException if we can't read the file
     */
    @Test
    public void testCaseSensitive() throws IOException {
        InputStream file = TaskScannerTest.class.getResourceAsStream("tasks-case-test.txt");

        Collection<Task> result = new TaskScanner(null, "todo", null, false).scan(new InputStreamReader(file));
        assignProperties(result);
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 1, result.size());
    }

    /**
     * Checks case sensitivity.
     *
     * @throws IOException if we can't read the file
     */
    @Test
    public void testCaseSensitive2() throws IOException {
        InputStream file = TaskScannerTest.class.getResourceAsStream("tasks-case-test.txt");

        Collection<Task> result = new TaskScanner(null, "ToDo", null, false).scan(new InputStreamReader(file));
        assignProperties(result);
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 1, result.size());
    }

    /**
     * Checks case insensitivity.
     *
     * @throws IOException if we can't read the file
     */
    @Test
    public void testCaseInsensitive() throws IOException {
        InputStream file = TaskScannerTest.class.getResourceAsStream("tasks-case-test.txt");

        Collection<Task> result = new TaskScanner(null, "todo", null, true).scan(new InputStreamReader(file));
        assignProperties(result);
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 9, result.size());
    }

    /**
     * Checks case insensitivity.
     *
     * @throws IOException if we can't read the file
     */
    @Test
    public void testCaseInsensitive2() throws IOException {
        InputStream file = TaskScannerTest.class.getResourceAsStream("tasks-case-test.txt");

        Collection<Task> result = new TaskScanner(null, "Todo, TodoS", null, true).scan(new InputStreamReader(file));
        assignProperties(result);
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 12, result.size());
    }

    /**
     * Checks whether we find the two task in the test file.
     *
     * @throws IOException if we can't read the file
     */
    @Test
    public void scanFileWithTasksAndDefaults() throws IOException {
        InputStream file = TaskScannerTest.class.getResourceAsStream(FILE_WITH_TASKS);

        Collection<Task> result = new TaskScanner().scan(new InputStreamReader(file));
        assignProperties(result);
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 2, result.size());

        Iterator<Task> iterator = result.iterator();
        assertEquals(WRONG_MESSAGE_ERROR, PRIORITY_NORMAL, iterator.next().getDetailMessage());
        assertEquals(WRONG_MESSAGE_ERROR, PRIORITY_HIGH, iterator.next().getDetailMessage());
    }

    /**
     * Checks whether we assign the right priorities for the test file.
     *
     * @throws IOException if we can't read the file
     */
    @Test
    public void testPriorities() throws IOException {
        InputStream file = TaskScannerTest.class.getResourceAsStream(FILE_WITH_TASKS);

        Collection<Task> result = new TaskScanner().scan(new InputStreamReader(file));
        assignProperties(result);
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 2, result.size());

        AnnotationContainer container = createContainer(result);
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 1, container.getNumberOfAnnotations(Priority.HIGH));
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 1, container.getNumberOfAnnotations(Priority.NORMAL));
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 0, container.getNumberOfAnnotations(Priority.LOW));
    }

    /**
     * Checks whether we find one high priority task in the test file.
     *
     * @throws IOException if we can't read the file
     */
    @Test
    public void testHighPriority() throws IOException {
        InputStream file = TaskScannerTest.class.getResourceAsStream(FILE_WITH_TASKS);

        Collection<Task> result = new TaskScanner("FIXME", null, null, false).scan(new InputStreamReader(file));
        assignProperties(result);
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 1, result.size());

        AnnotationContainer container = createContainer(result);
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 1, container.getNumberOfAnnotations(Priority.HIGH));
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 0, container.getNumberOfAnnotations(Priority.NORMAL));
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 0, container.getNumberOfAnnotations(Priority.LOW));
    }

    /**
     * Checks whether we correctly strip whitespace from the message.
     *
     * @throws IOException if we can't read the file
     */
    @Test
    public void testTwoItemsWithWhiteSpaceAndHighPriority() throws IOException {
        InputStream file = TaskScannerTest.class.getResourceAsStream(FILE_WITH_TASKS);

        Collection<Task> result = new TaskScanner(" FIXME , TODO ", null, null, false).scan(new InputStreamReader(file));
        assignProperties(result);
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 2, result.size());

        AnnotationContainer container = createContainer(result);
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 2, container.getNumberOfAnnotations(Priority.HIGH));
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 0, container.getNumberOfAnnotations(Priority.NORMAL));
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 0, container.getNumberOfAnnotations(Priority.LOW));
    }

    /**
     * Checks whether we find two high priority tasks with different identifiers in the test file.
     *
     * @throws IOException if we can't read the file
     */
    @Test
    public void testTwoItemsWithHighPriority() throws IOException {
        InputStream file = TaskScannerTest.class.getResourceAsStream(FILE_WITH_TASKS);

        Collection<Task> result = new TaskScanner("FIXME,TODO", null, null, false).scan(new InputStreamReader(file));
        assignProperties(result);
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 2, result.size());

        AnnotationContainer container = createContainer(result);
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 2, container.getNumberOfAnnotations(Priority.HIGH));
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 0, container.getNumberOfAnnotations(Priority.NORMAL));
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 0, container.getNumberOfAnnotations(Priority.LOW));
    }

    /**
     * Checks whether we find all priority task in the test file.
     *
     * @throws IOException if we can't read the file
     */
    @Test
    public void testAllPriorities() throws IOException {
        InputStream file = TaskScannerTest.class.getResourceAsStream(FILE_WITH_TASKS);

        Collection<Task> result = new TaskScanner("FIXME", "FIXME,TODO", "TODO", false).scan(new InputStreamReader(file));
        assignProperties(result);
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 4, result.size());

        AnnotationContainer container = createContainer(result);
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 1, container.getNumberOfAnnotations(Priority.HIGH));
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 2, container.getNumberOfAnnotations(Priority.NORMAL));
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 1, container.getNumberOfAnnotations(Priority.LOW));
    }

    /**
     * Checks whether we find no task in the test file.
     *
     * @throws IOException if we can't read the file
     */
    @Test
    public void scanFileWithoutTasks() throws IOException {
        InputStream file = TaskScannerTest.class.getResourceAsStream("file-without-tasks.txt");

        Collection<Task> result = new TaskScanner().scan(new InputStreamReader(file));
        assignProperties(result);
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 0, result.size());

        AnnotationContainer container = createContainer(result);
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 0, container.getNumberOfAnnotations());
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 0, container.getNumberOfAnnotations(Priority.HIGH));
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 0, container.getNumberOfAnnotations(Priority.NORMAL));
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 0, container.getNumberOfAnnotations(Priority.LOW));
    }

    /**
     * Assigns properties to all tasks.
     *
     * @param result
     *      the tasks to assign the properties for
     */
    private void assignProperties(final Collection<Task> result) {
        for (Task task : result) {
            task.setFileName("Path/To/TestFile");
            task.setPackageName("Package");
            task.setModuleName("Module");
        }
    }

    /**
     * Creates an annotation container to simplify tasks counting.
     *
     * @param tasks
     *            the tasks to add to the container
     * @return the annotation container
     */
    private AnnotationContainer createContainer(final Collection<Task> tasks) {
        AnnotationContainer container = new JavaProject();
        container.addAnnotations(tasks);
        return container;
    }
}

