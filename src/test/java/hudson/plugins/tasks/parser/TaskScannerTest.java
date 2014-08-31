package hudson.plugins.tasks.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

import static org.junit.Assert.*;

import hudson.plugins.analysis.core.ParserResult;
import hudson.plugins.analysis.util.model.AnnotationContainer;
import hudson.plugins.analysis.util.model.JavaProject;
import hudson.plugins.analysis.util.model.Priority;

/**
 * Tests the class {@link TaskScanner}.
 */
public class TaskScannerTest {
    /** Fixme tags. */
    private static final String FIXME = "FIXME";
    /** Filename for tests. */
    private static final String TEST_FILE = "tasks-case-test.txt";
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
     * Parses a warning log with characters in different locale.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-22744">Issue 22744</a>
     */
    @Test
    public void issue22744() throws IOException {
        InputStream file = TaskScannerTest.class.getResourceAsStream("issue22744.java");

        Collection<Task> result = new TaskScanner("FIXME", "TODO", "", false).scan(new InputStreamReader(file, "windows-1251"));
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 2, result.size());

        Iterator<Task> warnings = result.iterator();
        Task task = warnings.next();
        verifyTask(task, Priority.HIGH, "FIXME", 4, "\u0442\u0435\u0441\u0442\u0438\u0440\u043e\u0432\u0430\u043d\u0438\u0435 Jenkins");
        task = warnings.next();
        verifyTask(task, Priority.NORMAL, "TODO", 5, "\u043f\u0440\u0438\u043c\u0435\u0440 \u043a\u043e\u043c\u043c\u0435\u043d\u0442\u0430\u0440\u0438\u044f \u043d\u0430 \u0440\u0443\u0441\u0441\u043a\u043e\u043c");
    }

    private void verifyTask(final Task task, final Priority priority, final String tag, final int line, final String message) {
        assertEquals("Wrong priority detected: ", priority, task.getPriority());
        assertEquals("Wrong tag detected: ", tag, task.getType());
        assertEquals("Wrong line detected: ", line, task.getPrimaryLineNumber());
        assertEquals("Wrong message detected: ", message, task.getDetailMessage());
    }

    /**
     * Parses a warning log with !!! and !!!! warnings.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-12782">Issue 12782</a>
     */
    @Test
    public void issue12782() throws IOException {
        InputStream file = TaskScannerTest.class.getResourceAsStream("issue12782.txt");

        Collection<Task> result = new TaskScanner("!!!!!", "!!!", "", false).scan(new InputStreamReader(file));
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 3, result.size());
    }

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
        verifyOneTaskWhenCheckingCase("todo", 25);
        verifyOneTaskWhenCheckingCase("ToDo", 27);
    }

    private void verifyOneTaskWhenCheckingCase(final String tag, final int lineNumber) throws IOException {
        InputStream file = TaskScannerTest.class.getResourceAsStream(TEST_FILE);
        Collection<Task> result = new TaskScanner(null, tag, null, false).scan(new InputStreamReader(file));
        assignProperties(result);
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 1, result.size());
        Task task = result.iterator().next();
        verifyTask(task, Priority.NORMAL, tag, lineNumber, "");
    }

    /**
     * Checks case insensitivity.
     *
     * @throws IOException if we can't read the file
     */
    @Test
    public void testCaseInsensitive() throws IOException {
        InputStream file = TaskScannerTest.class.getResourceAsStream(TEST_FILE);

        Collection<Task> result = new TaskScanner(null, "todo", null, true).scan(new InputStreamReader(file));
        assignProperties(result);
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 9, result.size());
        for (Task task : result) {
            assertEquals("Tag name should be case insensitive", "TODO", task.getType());
        }
    }

    /**
     * Checks case insensitivity.
     *
     * @throws IOException if we can't read the file
     */
    @Test
    public void testCaseInsensitive2() throws IOException {
        InputStream file = TaskScannerTest.class.getResourceAsStream(TEST_FILE);

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

        Collection<Task> result = new TaskScanner(FIXME, null, null, false).scan(new InputStreamReader(file));
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
     * Checks whether we set the type of the task to the actual tag.
     *
     * @throws IOException if we can't read the file
     */
    @Test
    public void testTagsIdentification() throws IOException {
        String text = "FIXME: this is a fixme";
        Collection<Task> result = new TaskScanner("FIXME,TODO", null, null, false).scan(new StringReader(text));
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 1, result.size());
        Task task = result.iterator().next();
        assertEquals("Type is not the found token", FIXME, task.getType());

        result = new TaskScanner(null, "XXX, HELP, FIXME, TODO", null, false).scan(new StringReader(text));
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 1, result.size());

        task = result.iterator().next();
        assertEquals("Type is not the found token", FIXME, task.getType());
    }

    /**
     * Checks whether we find all priority task in the test file.
     *
     * @throws IOException if we can't read the file
     */
    @Test
    public void testAllPriorities() throws IOException {
        InputStream file = TaskScannerTest.class.getResourceAsStream(FILE_WITH_TASKS);

        Collection<Task> result = new TaskScanner(FIXME, "FIXME,TODO", "TODO", false).scan(new InputStreamReader(file));
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

