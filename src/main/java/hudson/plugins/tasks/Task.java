package hudson.plugins.tasks;

import hudson.plugins.tasks.util.FileAnnotation;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

/**
 * Java Bean class representing an open task.
 */
public class Task implements Serializable, FileAnnotation, Comparable<Task> {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 5171661552905752370L;
    /** Defines the priority of a task. */
    enum Priority { HIGH, NORMAL, LOW }
    /** The message of this task. */
    private final String message;
    /** The priority of this task. */
    private final Priority priority;
    /** Line number of the task in the corresponding file. */
    private final int lineNumber;
    /** Unique key of this task. */
    private int key;
    /** Filename of this task. */
    private String fileName;

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
     * Returns the detail message of the task (the text after the task keyword).
     *
     * @return the detail message of the task
     */
    public String getDetailMessage() {
        return message;
    }

    /** {@inheritDoc} */
    public String getMessage() {
        return StringUtils.EMPTY;
    }

    /** {@inheritDoc} */
    public String getToolTip() {
        return "Priority: " + priority.name();
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

    /** {@inheritDoc} */
    public boolean isLineAnnotation() {
        return true;
    }

    /**
     * Sets the unique key of this task.
     *
     * @param key the key
     */
    public void setKey(final int key) {
        this.key = key;
    }

    /**
     * Returns the key of this task.
     *
     * @return the key
     */
    public int getKey() {
        return key;
    }

    /**
     * Sets the file name for this task.
     *
     * @param fileName the file name for this task
     */
    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    /** {@inheritDoc} */
    public String getFileName() {
        return fileName;
    }

    /** {@inheritDoc} */
    public int compareTo(final Task otherTask) {
        return key - otherTask.key;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + key;
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Task other = (Task)obj;
        if (key != other.key) {
            return false;
        }
        return true;
    }
}

