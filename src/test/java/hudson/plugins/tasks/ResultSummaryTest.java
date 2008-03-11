package hudson.plugins.tasks;

import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.*;

import java.util.Locale;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Tests the class {@link ResultSummary}.
 */
public class ResultSummaryTest {
    /**
     * Checks the text for no warnings in 1 file. The delta is > 0.
     */
    @Test
    public void test0WarningsIn1FileAndPositiveDelta() {
        checkSummaryText(0, 1, 10, "Task Scanner: 0 open tasks in 1 workspace file (+10).");
    }

    /**
     * Checks the text for no warnings in 1 file. The delta is < 0.
     */
    @Test
    public void test0WarningsIn1FileAndNegativeDelta() {
        checkSummaryText(0, 1, -5, "Task Scanner: 0 open tasks in 1 workspace file (-5).");
    }

    /**
     * Checks the text for no warnings in 1 file. The delta is 0.
     */
    @Test
    public void test0WarningsIn1File() {
        checkSummaryText(0, 1, 0, "Task Scanner: 0 open tasks in 1 workspace file (±0).");
    }

    /**
     * Checks the text for no warnings in 1 file. The delta is 0.
     */
    @Test
    public void test0WarningsIn5Files() {
        checkSummaryText(0, 5, 0, "Task Scanner: 0 open tasks in 5 workspace files (±0).");
    }

    /**
     * Checks the text for no warnings in 1 file. The delta is 0.
     */
    @Test
    public void test1WarningIn2Files() {
        checkSummaryText(1, 2, 0, "Task Scanner: <a href=\"tasksResult\">1 open task</a> in 2 workspace files (±0).");
    }

    /**
     * Checks the text for no warnings in 1 file. The delta is 0.
     */
    @Test
    public void test5WarningsIn1File() {
        checkSummaryText(5, 1, 0, "Task Scanner: <a href=\"tasksResult\">5 open tasks</a> in 1 workspace file (±0).");
    }

    /**
     * Parameterized test case to check the message text for the specified
     * number of warnings and files.
     *
     * @param numberOfWarnings
     *            the number of warnings
     * @param numberOfFiles
     *            the number of files
     * @param delta
     *            delta between the last run
     * @param expectedMessage
     *            the expected message
     */
    private void checkSummaryText(final int numberOfWarnings, final int numberOfFiles, final int delta, final String expectedMessage) {
        TasksResult result = createMock(TasksResult.class);
        expect(result.getNumberOfAnnotations()).andReturn(numberOfWarnings).anyTimes();
        expect(result.getNumberOfFiles()).andReturn(numberOfFiles).anyTimes();
        expect(result.getDelta()).andReturn(delta).anyTimes();

        replay(result);

        Locale.setDefault(Locale.ENGLISH);
        Assert.assertEquals("Wrong summary message created.", expectedMessage, ResultSummary.createSummary(result));

        verify(result);
    }
}

