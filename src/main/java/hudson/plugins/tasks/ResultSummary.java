package hudson.plugins.tasks;

/**
 * Represents the result summary of the open tasks parser. This summary will be
 * shown in the summary.jelly script of the warnings result action.
 *
 * @author Ulli Hafner
 */
public final class ResultSummary {
    /**
     * Returns the message to show as the result summary.
     *
     * @param result
     *            the result
     * @return the message
     */
    // CHECKSTYLE:CONSTANTS-OFF
    public static String createSummary(final TasksResult result) {
        StringBuilder summary = new StringBuilder();
        int tasks = result.getNumberOfAnnotations();

        summary.append(Messages.Tasks_ResultAction_Summary());
        summary.append(" ");
        if (tasks > 0) {
            summary.append("<a href=\"tasksResult\">");
        }
        if (tasks == 1) {
            summary.append(Messages.Tasks_ResultAction_OneWarning());
        }
        else {
            summary.append(Messages.Tasks_ResultAction_MultipleWarnings(tasks));
        }
        if (tasks > 0) {
            summary.append("</a>");
        }
        summary.append(" ");
        if (result.getNumberOfFiles() > 1) {
            summary.append(Messages.Tasks_ResultAction_MultipleFiles(result.getNumberOfFiles()));
        }
        else {
            summary.append(Messages.Tasks_ResultAction_OneFile());
        }
        summary.append(".");
        return summary.toString();
    }
    // CHECKSTYLE:CONSTANTS-ON

    /**
     * Returns the message to show as the result summary.
     *
     * @param result
     *            the result
     * @return the message
     */
    // CHECKSTYLE:CONSTANTS-OFF
    public static String createDeltaMessage(final TasksResult result) {
        StringBuilder summary = new StringBuilder();
        if (result.getNumberOfNewWarnings() > 0) {
            summary.append("<li><a href=\"tasksResult/new\">");
            if (result.getNumberOfNewWarnings() == 1) {
                summary.append(Messages.Tasks_ResultAction_OneNewWarning());
            }
            else {
                summary.append(Messages.Tasks_ResultAction_MultipleNewWarnings(result.getNumberOfNewWarnings()));
            }
            summary.append("</a></li>");
        }
        if (result.getNumberOfFixedWarnings() > 0) {
            summary.append("<li><a href=\"tasksResult/fixed\">");
            if (result.getNumberOfFixedWarnings() == 1) {
                summary.append(Messages.Tasks_ResultAction_OneFixedWarning());
            }
            else {
                summary.append(Messages.Tasks_ResultAction_MultipleFixedWarnings(result.getNumberOfFixedWarnings()));
            }
            summary.append("</a></li>");
        }

        return summary.toString();
    }
    // CHECKSTYLE:CONSTANTS-ON

    /**
     * Instantiates a new result summary.
     */
    private ResultSummary() {
        // prevents instantiation
    }
}

