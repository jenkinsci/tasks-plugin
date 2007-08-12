package hudson.plugins.tasks;

import hudson.model.Build;
import hudson.model.ModelObject;
import hudson.plugins.tasks.Task.Priority;

import java.io.Serializable;
import java.lang.ref.WeakReference;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * Represents the results of the task  scanner. One instance of this class is persisted for
 * each build via an XML file.
 *
 * @author Ulli Hafner
 */
public class TasksResult implements ModelObject, Serializable {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -344808345805935004L;
    /** The current build as owner of this action. */
    @SuppressWarnings("Se")
    private final Build<?, ?> owner;
    /** The parsed FindBugs result. */
    @SuppressWarnings("Se")
    private transient WeakReference<JavaProject> project;
    /** The number of tasks in this build. */
    private final int numberOfTasks;
    /** Difference between this and the previous build. */
    private final int delta;
    /** The number of high priority tasks in this build. */
    private final int highPriorityTasks;
    /** The number of low priority tasks in this build. */
    private final int lowPriorityTasks;
    /** The number of normal priority tasks in this build. */
    private final int normalPriorityTasks;

    /**
     * Creates a new instance of <code>FindBugsResult</code>.
     *
     * @param build
     *            the current build as owner of this action
     * @param project
     *            the parsed FindBugs result
     */
    public TasksResult(final Build<?, ?> build, final JavaProject project) {
        this(build, project, project.getNumberOfTasks());
    }

    /**
     * Creates a new instance of <code>FindBugsResult</code>.
     *
     * @param build the current build as owner of this action
     * @param project the parsed FindBugs result
     * @param previousNumberOfTasks the previous number of open tasks
     */
    public TasksResult(final Build<?, ?> build, final JavaProject project, final int previousNumberOfTasks) {
        owner = build;
        highPriorityTasks = project.getNumberOfTasks(Priority.HIGH);
        lowPriorityTasks = project.getNumberOfTasks(Priority.LOW);
        normalPriorityTasks = project.getNumberOfTasks(Priority.NORMAL);
        numberOfTasks = project.getNumberOfTasks();
        this.project = new WeakReference<JavaProject>(project);
        delta = numberOfTasks - previousNumberOfTasks;
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return "Open Tasks";
    }

    /**
     * Returns the owner.
     *
     * @return the owner
     */
    public Build<?, ?> getOwner() {
        return owner;
    }

    /**
     * Gets the number of tasks.
     *
     * @return the number of tasks
     */
    public int getNumberOfTasks() {
        return numberOfTasks;
    }

    /**
     * Returns the highPriorityTasks.
     *
     * @return the highPriorityTasks
     */
    public int getNumberOfHighPriorityTasks() {
        return highPriorityTasks;
    }

    /**
     * Returns the lowPriorityTasks.
     *
     * @return the lowPriorityTasks
     */
    public int getNumberOfLowPriorityTasks() {
        return lowPriorityTasks;
    }

    /**
     * Returns the normalPriorityTasks.
     *
     * @return the normalPriorityTasks
     */
    public int getNumberOfNormalPriorityTasks() {
        return normalPriorityTasks;
    }

    /**
     * Returns the delta.
     *
     * @return the delta
     */
    public int getDelta() {
        return delta;
    }

    /**
     * Returns the associated project of this result.
     *
     * @return the associated project of this result.
     */
    public JavaProject getProject() {
        if (project == null) {
            loadResult();
        }
        JavaProject result = project.get();
        if (result == null) {
            loadResult();
        }
        return project.get();
    }

    /**
     * Loads the FindBugs results and wraps them in a weak reference that might
     * get removed by the garbage collector.
     */
    private void loadResult() {
        JavaProject result = new JavaProject();
        project = new WeakReference<JavaProject>(result);
        // FIXME not implemented yet
    }
}
