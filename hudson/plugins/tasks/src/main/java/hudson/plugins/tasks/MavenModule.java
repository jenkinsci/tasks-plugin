package hudson.plugins.tasks;

import hudson.plugins.tasks.Task.Priority;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

/**
 * A maven module.
 */
public class MavenModule implements Serializable {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 5467122420572804130L;
    /** Name of the module. */
    private final String moduleName;
    /** Files with task in this module. */
    private final Set<WorkspaceFile> files = new HashSet<WorkspaceFile>();

    /**
     * Creates a new instance of <code>MavenModule</code>.
     *
     * @param moduleName
     *            name of the module
     */
    public MavenModule(final String moduleName) {
        this.moduleName = moduleName;
    }

    /**
     * Adds the specified file to the maven module.
     *
     * @param workspaceFile the workspace file
     */
    public void add(final WorkspaceFile workspaceFile) {
        files.add(workspaceFile);
    }

    /**
     * Returns the module name.
     *
     * @return the module name
     */
    public String getName() {
        return moduleName;
    }

    /**
     * Returns the files of this module.
     *
     * @return the files of this module
     */
    public Set<WorkspaceFile> getFiles() {
        return Collections.unmodifiableSet(files);
    }

    /**
     * Returns the total number of tasks in this project.
     *
     * @return total number of tasks in this project.
     */
    public int getNumberOfTasks() {
        int numberOfTasks  = 0;
        for (WorkspaceFile file : files) {
            numberOfTasks += file.getNumberOfTasks();
        }
        return numberOfTasks;
    }

    /**
     * Returns the number of tasks with the specified priority in this project.
     *
     * @param  priority the priority
     *
     * @return the number of tasks with the specified priority in this project.
     */
    public int getNumberOfTasks(final String priority) {
        return getNumberOfTasks(Priority.valueOf(StringUtils.upperCase(priority)));
    }

    /**
     * Returns the number of tasks with the specified priority in this project.
     *
     * @param  priority the priority
     *
     * @return the number of tasks with the specified priority in this project.
     */
    public int getNumberOfTasks(final Priority priority) {
        int numberOfTasks  = 0;
        for (WorkspaceFile file : files) {
            numberOfTasks += file.getNumberOfTasks(priority);
        }
        return numberOfTasks;
    }
}

