package hudson.plugins.tasks;

import hudson.XmlFile;
import hudson.model.Build;
import hudson.model.ModelObject;
import hudson.plugins.tasks.Task.Priority;
import hudson.util.StringConverter2;
import hudson.util.XStream2;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.thoughtworks.xstream.XStream;

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
    /** Error logger. */
    private static final Logger LOGGER = Logger.getLogger(TasksResult.class.getName());
    /** Serialization provider. */
    private static final XStream XSTREAM = new XStream2();

    static {
        XSTREAM.alias("project", JavaProject.class);
        XSTREAM.registerConverter(new StringConverter2(), 100);
    }

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

        try {
            getDataFile().write(project);
        }
        catch (IOException exception) {
            LOGGER.log(Level.WARNING, "Failed to serialize the open tasks result.", exception);
        }
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
        LOGGER.log(Level.WARNING, "Project loaded!!!" + getDataFile());
        JavaProject result;
        try {
            result = (JavaProject)getDataFile().read();
        }
        catch (IOException exception) {
            LOGGER.log(Level.WARNING, "Failed to load " + getDataFile(), exception);
            result = new JavaProject();
        }
        project = new WeakReference<JavaProject>(result);
    }

    /**
     * Returns the serialization file.
     *
     * @return the serialization file.
     */
    private XmlFile getDataFile() {
        return new XmlFile(XSTREAM, new File(owner.getRootDir(), "openTasks.xml"));
    }
}
