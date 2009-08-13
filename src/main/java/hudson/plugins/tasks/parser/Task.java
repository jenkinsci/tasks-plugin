package hudson.plugins.tasks.parser;

import hudson.plugins.tasks.util.model.AbstractAnnotation;
import hudson.plugins.tasks.util.model.Priority;

import org.apache.commons.lang.StringUtils;

/**
 * A serializable Java Bean class representing an open task.
 *
 * @author Ulli Hafner
 */
public class Task extends AbstractAnnotation {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 5171662552905752370L;


    /**
     * Creates a new instance of <code>Task</code>.
     *
     * @param priority
     *            the priority
     * @param lineNumber
     *            the line number of the task in the corresponding file
     * @param taskTag
     *            the found task tag
     * @param message
     *            the message of the task (the text after the task keyword)
     */
    public Task(final Priority priority, final int lineNumber, final String taskTag, final String message) {
        super(priority, message, lineNumber, lineNumber, StringUtils.EMPTY, taskTag);
    }

    /**
     * Returns the detail message of the task (the text after the task keyword).
     *
     * @return the detail message of the task
     */
    public String getDetailMessage() {
        return super.getMessage();
    }

    /** {@inheritDoc} */
    @Override
    public String getMessage() {
        return StringUtils.EMPTY;
    }

    /** {@inheritDoc} */
    public String getToolTip() {
        return getPriority().getLongLocalizedString();
    }
}

