package hudson.plugins.tasks.parser;

import hudson.plugins.tasks.model.FileAnnotation;
import hudson.plugins.tasks.model.Priority;
import hudson.plugins.tasks.model.WorkspaceFile;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * A serializable Java Bean class representing an open task.
 */
public class Task implements Serializable, FileAnnotation, Comparable<Task> {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 5171661552905752370L;
    /** Current task key.  */
    private static long currentKey;
    /** The message of this task. */
    private final String message;
    /** The priority of this task. */
    private final Priority priority;
    /** Line number of the task in the corresponding file. */
    private final int lineNumber;
    /** Unique key of this task. */
    private long key;
    /** File this annotation is part of. */
    @SuppressWarnings("Se")
    private transient WorkspaceFile workspaceFile;

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
        key = currentKey++;
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

    /** {@inheritDoc} */
    public Priority getPriority() {
        return priority;
    }

    /** {@inheritDoc} */
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
    public void setKey(final long key) {
        this.key = key;
    }

    /**
     * Returns the key of this task.
     *
     * @return the key
     */
    public long getKey() {
        return key;
    }

    /**
     * Connects this annotation with the specified workspace file.
     *
     * @param workspaceFile the workspace file that contains this annotation
     */
    public void setWorkspaceFile(final WorkspaceFile workspaceFile) {
        this.workspaceFile = workspaceFile;
    }

    /** {@inheritDoc} */
    public WorkspaceFile getWorkspaceFile() {
        return workspaceFile;
    }

    /** {@inheritDoc} */
    public int compareTo(final Task otherTask) {
        if (key == otherTask.key) {
            return 0;
        }
        else if (key > otherTask.key) {
            return 1;
        }
        return -1;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return 31 + (int)(key ^ (key >>> 32));
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

