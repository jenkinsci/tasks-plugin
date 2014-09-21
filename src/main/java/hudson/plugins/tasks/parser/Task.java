package hudson.plugins.tasks.parser;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.export.Exported;

import hudson.plugins.analysis.util.model.AbstractAnnotation;
import hudson.plugins.analysis.util.model.Priority;

/**
 * A serializable Java Bean class representing an open task.
 *
 * @author Ulli Hafner
 */
public class Task extends AbstractAnnotation {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 5171662552905752370L;
    /** Origin of the annotation. */
    public static final String ORIGIN = "tasks";

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

        setOrigin(ORIGIN);
    }

    /**
     * Returns the detail message of the task (the text after the task keyword).
     *
     * @return the detail message of the task
     */
    @Exported
    public String getDetailMessage() {
        return super.getMessage();
    }

    @Override
    public String getMessage() {
        return StringUtils.EMPTY;
    }

    @Override
    public String getToolTip() {
        return getPriority().getLongLocalizedString();
    }

    /**
     * Gets the matching text of a tasks including the tag.
     *
     * @return the match
     */
    public String getMatch() {
        if (StringUtils.isEmpty(getType())) {
            return getDetailMessage();
        }
        else {
            return getType() + ": " + getDetailMessage();
        }
    }

    @Override
    public String toString() {
        return super.toString() + getDetailMessage();
    }
}

