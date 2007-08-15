package hudson.plugins.tasks;

/**
 * Java Bean class representing an  open task.
 */
public class Task {
    /** Defines the priority of a task. */
    enum Priority { HIGH, NORMAL, LOW }
    /** The message of this task. */
    private final String message;
    /** The priority of this task. */
    private final Priority priority;
    /** Line number of the task in the corresponding file. */
    private final int lineNumber;

    /**
     * Creates a new instance of <code>Task</code>.
     * @param priority the priority
     * @param lineNumber
     *            the line number of the task in the corresponding file
     * @param message
     *            the message of the task (the text after the task keyword)
     */
    public Task(final Priority priority, final int lineNumber, final String message) {
        this.priority = priority;
        this.message = message;
        this.lineNumber = lineNumber;
    }

    /**
     * Returns the message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the priority.
     *
     * @return the priority
     */
    public Priority getPriority() {
        return priority;
    }

    /**
     * Returns the lineNumber.
     *
     * @return the lineNumber
     */
    public int getLineNumber() {
        return lineNumber;
    }
}

