package hudson.plugins.tasks.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
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
     * Parses tasks using a regular expression.
     *
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-17225">Issue 17225</a>
     */
    @Test
    public void testRegularExpressionsIssue17225() {
        Collection<Task> result = scan("regexp.txt", 5, "^.*(TODO(?:[0-9]*))(.*)$", null, "", false, true);
        Iterator<Task> warnings = result.iterator();
        Task task;
        task = warnings.next();
        int line = 1;
        verifyTask(task, Priority.HIGH, "TODO1", line++, "erstes");
        task = warnings.next();
        verifyTask(task, Priority.HIGH, "TODO2", line++, "zweites");
        task = warnings.next();
        verifyTask(task, Priority.HIGH, "TODO3", line++, "drittes");
        task = warnings.next();
        verifyTask(task, Priority.HIGH, "TODO4", line++, "viertes");
        task = warnings.next();
        verifyTask(task, Priority.HIGH, "TODO20", line++, "zwanzigstes");
    }

    /**
     * Parses tasks using a regular expression.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-17225">Issue 17225</a>
     */
    @Test
    public void testRegularExpressionAsTag() throws IOException {
        Collection<Task> result = scan("regexp.txt", 5, "^.*(TODO(?:[0-9]*))(.*)$", null, "", false, true);
        Iterator<Task> warnings = result.iterator();
        Task task;
        task = warnings.next();
        int line = 1;
        verifyTask(task, Priority.HIGH, "TODO1", line++, "erstes");
        task = warnings.next();
        verifyTask(task, Priority.HIGH, "TODO2", line++, "zweites");
        task = warnings.next();
        verifyTask(task, Priority.HIGH, "TODO3", line++, "drittes");
        task = warnings.next();
        verifyTask(task, Priority.HIGH, "TODO4", line++, "viertes");
        task = warnings.next();
        verifyTask(task, Priority.HIGH, "TODO20", line++, "zwanzigstes");
    }

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
        InputStreamReader reader = new InputStreamReader(file, "windows-1251");

        Collection<Task> result = scan(reader, 2, "FIXME", "TODO", "", false, false);

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
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-12782">Issue 12782</a>
     */
    @Test
    public void issue12782() {
        scan("issue12782.txt", 3, "!!!!!", "!!!", "", false, false);
    }

    /**
     * Checks whether we find tasks at word boundaries.
     */
    @Test
    public void scanFileWithWords() {
        Collection<Task> result = scan("tasks-words-test.txt", 12, "WARNING", "TODO", "@todo", false, false);

        ParserResult parserResult = new ParserResult();
        parserResult.addAnnotations(result);

        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 0, parserResult.getNumberOfAnnotations(Priority.HIGH));
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 7, parserResult.getNumberOfAnnotations(Priority.NORMAL));
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 5, parserResult.getNumberOfAnnotations(Priority.LOW));
    }

    /**
     * Checks case sensitivity.
     */
    @Test
    public void testCaseSensitive() {
        verifyOneTaskWhenCheckingCase("todo", 25);
        verifyOneTaskWhenCheckingCase("ToDo", 27);
    }

    private void verifyOneTaskWhenCheckingCase(final String tag, final int lineNumber) {
        Collection<Task> result = scan(TEST_FILE, 1, null, tag, null, false, false);
        Task task = result.iterator().next();
        verifyTask(task, Priority.NORMAL, tag, lineNumber, "");
    }

    /**
     * Checks case insensitivity.
     */
    @Test
    public void testCaseInsensitive() {
        Collection<Task> result = scan(TEST_FILE, 9, null, "todo", null, true, false);
        for (Task task : result) {
            assertEquals("Tag name should be case insensitive", "TODO", task.getType());
        }
    }

    /**
     * Checks case insensitivity.
     */
    @Test
    public void testCaseInsensitive2() {
        Collection<Task> result = scan(TEST_FILE, 12, null, "Todo, TodoS", null, true, false);
    }

    /**
     * Checks whether we find the two task in the test file.
     */
    @Test
    public void scanFileWithTasksAndDefaults() {
        Collection<Task> result = scan(FILE_WITH_TASKS, 2);

        Iterator<Task> iterator = result.iterator();
        assertEquals(WRONG_MESSAGE_ERROR, PRIORITY_NORMAL, iterator.next().getDetailMessage());
        assertEquals(WRONG_MESSAGE_ERROR, PRIORITY_HIGH, iterator.next().getDetailMessage());

        AnnotationContainer container = createContainer(result);
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 1, container.getNumberOfAnnotations(Priority.HIGH));
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 1, container.getNumberOfAnnotations(Priority.NORMAL));
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 0, container.getNumberOfAnnotations(Priority.LOW));
    }

    /**
     * Checks whether we find one high priority task in the test file.
     */
    @Test
    public void testHighPriority() {
        Collection<Task> result = scan(FILE_WITH_TASKS, 1, FIXME, null, null, false, false);

        AnnotationContainer container = createContainer(result);
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 1, container.getNumberOfAnnotations(Priority.HIGH));
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 0, container.getNumberOfAnnotations(Priority.NORMAL));
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 0, container.getNumberOfAnnotations(Priority.LOW));
    }

    /**
     * Checks whether we correctly strip whitespace from the message.
     */
    @Test
    public void testTwoItemsWithWhiteSpaceAndHighPriority() {
        Collection<Task> result = scan(FILE_WITH_TASKS, 2, " FIXME , TODO ", null, null, false, false);

        AnnotationContainer container = createContainer(result);
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 2, container.getNumberOfAnnotations(Priority.HIGH));
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 0, container.getNumberOfAnnotations(Priority.NORMAL));
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 0, container.getNumberOfAnnotations(Priority.LOW));
    }

    /**
     * Checks whether we find two high priority tasks with different identifiers in the test file.
     */
    @Test
    public void testTwoItemsWithHighPriority() {
        Collection<Task> result = scan(FILE_WITH_TASKS, 2, "FIXME,TODO", null, null, false, false);

        AnnotationContainer container = createContainer(result);
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 2, container.getNumberOfAnnotations(Priority.HIGH));
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 0, container.getNumberOfAnnotations(Priority.NORMAL));
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 0, container.getNumberOfAnnotations(Priority.LOW));
    }

    /**
     * Checks whether we set the type of the task to the actual tag.
     */
    @Test
    public void testTagsIdentification() {
        String text = "FIXME: this is a fixme";
        Collection<Task> result = scan(new StringReader(text), 1, "FIXME,TODO", null, null, false, false);
        Task task = result.iterator().next();
        assertEquals("Type is not the found token", FIXME, task.getType());

        result = scan(new StringReader(text), 1, null, "XXX, HELP, FIXME, TODO", null, false, false);

        task = result.iterator().next();
        assertEquals("Type is not the found token", FIXME, task.getType());
    }

    /**
     * Checks whether we find all priority task in the test file.
     */
    @Test
    public void testAllPriorities() {
        Collection<Task> result = scan(FILE_WITH_TASKS, 4, FIXME, "FIXME,TODO", "TODO", false, false);

        AnnotationContainer container = createContainer(result);
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 1, container.getNumberOfAnnotations(Priority.HIGH));
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 2, container.getNumberOfAnnotations(Priority.NORMAL));
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 1, container.getNumberOfAnnotations(Priority.LOW));
    }

    /**
     * Checks whether we find no task in the test file.
     */
    @Test
    public void scanFileWithoutTasks() {
        Collection<Task> result = scan("file-without-tasks.txt", 0);

        AnnotationContainer container = createContainer(result);
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 0, container.getNumberOfAnnotations());
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 0, container.getNumberOfAnnotations(Priority.HIGH));
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 0, container.getNumberOfAnnotations(Priority.NORMAL));
        assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, 0, container.getNumberOfAnnotations(Priority.LOW));
    }

    private Collection<Task> scan(final String fileName, final int expectedNumberOfTasks,
                                  final String high, final String normal, final String low,
                                  final boolean ignoreCase, final boolean asRegexp) {
        InputStream file = TaskScannerTest.class.getResourceAsStream(fileName);
        InputStreamReader reader = new InputStreamReader(file);

        return scan(reader, expectedNumberOfTasks, high, normal, low, ignoreCase, asRegexp);
    }

    private Collection<Task> scan(final Reader reader, final int expectedNumberOfTasks,
                                  final String high, final String normal, final String low,
                                  final boolean ignoreCase, final boolean asRegexp) {
        try {
            Collection<Task> tasks = new TaskScanner(high, normal, low, ignoreCase, asRegexp).scan(reader);
            assignProperties(tasks);
            assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, expectedNumberOfTasks, tasks.size());

            return tasks;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Collection<Task> scan(final String fileName, final int expectedNumberOfTasks) {
        InputStream file = TaskScannerTest.class.getResourceAsStream(fileName);
        InputStreamReader reader = new InputStreamReader(file);

        return scan(reader, expectedNumberOfTasks);
    }

    private Collection<Task> scan(final Reader reader, final int expectedNumberOfTasks) {
        try {
            Collection<Task> tasks = new TaskScanner().scan(reader);
            assignProperties(tasks);
            assertEquals(WRONG_NUMBER_OF_TASKS_ERROR, expectedNumberOfTasks, tasks.size());
            return tasks;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
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

