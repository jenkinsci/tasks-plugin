package hudson.plugins.tasks;

import static org.mockito.Mockito.*;
import hudson.plugins.analysis.test.AbstractEnglishLocaleTest;
import org.junit.Assert;

import org.junit.Test;

/**
 * Tests the class {@link ResultSummary}.
 */
public class ResultSummaryTest extends AbstractEnglishLocaleTest {
    /**
     * Checks the text for no open tasks in 1 file. The delta is > 0.
     */
    @Test
    public void test0WarningsIn1FileAndPositiveDelta() {
        checkSummaryText(0, 1, 10, "Task Scanner: 0 open tasks in 1 workspace file.");
    }

    /**
     * Checks the text for no open tasks in 1 file. The delta is < 0.
     */
    @Test
    public void test0WarningsIn1FileAndNegativeDelta() {
        checkSummaryText(0, 1, -5, "Task Scanner: 0 open tasks in 1 workspace file.");
    }

    /**
     * Checks the text for no open tasks in 1 file. The delta is 0.
     */
    @Test
    public void test0WarningsIn1File() {
        checkSummaryText(0, 1, 0, "Task Scanner: 0 open tasks in 1 workspace file.");
    }

    /**
     * Checks the text for no open tasks in 1 file. The delta is 0.
     */
    @Test
    public void test0WarningsIn5Files() {
        checkSummaryText(0, 5, 0, "Task Scanner: 0 open tasks in 5 workspace files.");
    }

    /**
     * Checks the text for no open tasks in 1 file. The delta is 0.
     */
    @Test
    public void test1WarningIn2Files() {
        checkSummaryText(1, 2, 0, "Task Scanner: <a href=\"tasksResult\">1 open task</a> in 2 workspace files.");
    }

    /**
     * Checks the text for no open tasks in 1 file. The delta is 0.
     */
    @Test
    public void test5WarningsIn1File() {
        checkSummaryText(5, 1, 0, "Task Scanner: <a href=\"tasksResult\">5 open tasks</a> in 1 workspace file.");
    }

    /**
     * Parameterized test case to check the message text for the specified
     * number of open tasks and files.
     *
     * @param numberOfWarnings
     *            the number of open tasks
     * @param numberOfFiles
     *            the number of files
     * @param delta
     *            delta between the last run
     * @param expectedMessage
     *            the expected message
     */
    private void checkSummaryText(final int numberOfWarnings, final int numberOfFiles, final int delta, final String expectedMessage) {
        TasksResult result = mock(TasksResult.class);
        when(result.getNumberOfAnnotations()).thenReturn(numberOfWarnings);
        when(result.getNumberOfFiles()).thenReturn(numberOfFiles);
        when(result.getDelta()).thenReturn(delta);

        Assert.assertEquals("Wrong summary message created.", expectedMessage, ResultSummary.createSummary(result));
    }
    /**
     * Checks the delta message for no new and no closed tasks.
     */
    @Test
    public void testNoDelta() {
        checkDeltaText(0, 0, "");
    }

    /**
     * Checks the delta message for 1 new and no closed tasks.
     */
    @Test
    public void testOnly1New() {
        checkDeltaText(0, 1, "<li><a href=\"tasksResult/new\">1 new open task</a></li>");
    }

    /**
     * Checks the delta message for 5 new and no closed tasks.
     */
    @Test
    public void testOnly5New() {
        checkDeltaText(0, 5, "<li><a href=\"tasksResult/new\">5 new open tasks</a></li>");
    }

    /**
     * Checks the delta message for 1 fixed and no new open tasks.
     */
    @Test
    public void testOnly1Fixed() {
        checkDeltaText(1, 0, "<li><a href=\"tasksResult/fixed\">1 closed task</a></li>");
    }

    /**
     * Checks the delta message for 5 fixed and no new open tasks.
     */
    @Test
    public void testOnly5Fixed() {
        checkDeltaText(5, 0, "<li><a href=\"tasksResult/fixed\">5 closed tasks</a></li>");
    }

    /**
     * Checks the delta message for 5 fixed and 5 new open tasks.
     */
    @Test
    public void test5New5Fixed() {
        checkDeltaText(5, 5,
                "<li><a href=\"tasksResult/new\">5 new open tasks</a></li>"
                + "<li><a href=\"tasksResult/fixed\">5 closed tasks</a></li>");
    }

    /**
     * Checks the delta message for 5 fixed and 5 new open tasks.
     */
    @Test
    public void test5New1Fixed() {
        checkDeltaText(1, 5,
        "<li><a href=\"tasksResult/new\">5 new open tasks</a></li>"
        + "<li><a href=\"tasksResult/fixed\">1 closed task</a></li>");
    }

    /**
     * Checks the delta message for 5 fixed and 5 new open tasks.
     */
    @Test
    public void test1New5Fixed() {
        checkDeltaText(5, 1,
                "<li><a href=\"tasksResult/new\">1 new open task</a></li>"
                + "<li><a href=\"tasksResult/fixed\">5 closed tasks</a></li>");
    }

    /**
     * Checks the delta message for 5 fixed and 5 new open tasks.
     */
    @Test
    public void test1New1Fixed() {
        checkDeltaText(1, 1,
                "<li><a href=\"tasksResult/new\">1 new open task</a></li>"
                + "<li><a href=\"tasksResult/fixed\">1 closed task</a></li>");
    }

    /**
     * Parameterized test case to check the message text for the specified
     * number of open tasks and files.
     *
     * @param numberOfFixedWarnings
     *            the number of closed tasks
     * @param numberOfNewWarnings
     *            the number of new open tasks
     * @param expectedMessage
     *            the expected message
     */
    private void checkDeltaText(final int numberOfFixedWarnings, final int numberOfNewWarnings, final String expectedMessage) {
        TasksResult result = mock(TasksResult.class);
        when(result.getNumberOfFixedWarnings()).thenReturn(numberOfFixedWarnings);
        when(result.getNumberOfNewWarnings()).thenReturn(numberOfNewWarnings);

        Assert.assertEquals("Wrong delta message created.", expectedMessage, ResultSummary.createDeltaMessage(result));
    }
}

