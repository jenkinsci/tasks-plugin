package hudson.plugins.tasks;


/**
 * Represents the result summary of the FindBugs parser. This summary will be
 * shown in the summary.jelly script of the FindBugs result action.
 */
public final class ResultSummary {
    /**
     * Returns the message to show as the result summary.
     *
     * @param project
     *            the project
     * @return the message
     */
    public static String createSummary(final TasksResult project) {
        StringBuilder summary = new StringBuilder();
        int tasks = project.getNumberOfAnnotations();

        summary.append("Task Scanner: ");
        if (tasks > 0) {
            summary.append("<a href=\"tasksResult\">");
        }
        if (tasks == 1) {
            summary.append("1 open task");
        }
        else {
            summary.append(tasks + " open tasks");
        }
        if (tasks > 0) {
            summary.append("</a>");
        }
        summary.append(" ");
        if (project.getNumberOfFiles() > 1) {
            summary.append("in " + project.getNumberOfFiles() + " workspace files");
        }
        else {
            summary.append("in 1 workspace file");
        }
        if (project.getDelta() == 0) {
            summary.append(" (±0).");
        }
        else if (project.getDelta() > 0) {
            summary.append(" (+" + project.getDelta() + ").");
        }
        else {
            summary.append(" (" + project.getDelta() + ").");
        }
        return summary.toString();
    }

    /**
     * Instantiates a new result summary.
     */
    private ResultSummary() {
        // prevents instantiation
    }
}

