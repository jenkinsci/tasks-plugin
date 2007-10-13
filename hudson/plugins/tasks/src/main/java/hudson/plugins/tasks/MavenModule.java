package hudson.plugins.tasks;

import hudson.plugins.tasks.Task.Priority;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

/**
 * A maven module.
 */
public class MavenModule implements Serializable, TasksProvider {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 5467122420572804130L;
    /** Name of the module. */
    private final String moduleName;
    /** Files with task in this module. */
    private final Set<WorkspaceFile> files = new HashSet<WorkspaceFile>();
    /** Files with open tasks in this project. */
    private final Map<String, JavaPackage> filesPerPackage;

    /**
     * Creates a new instance of <code>MavenModule</code>.
     *
     * @param moduleName
     *            name of the module
     */
    public MavenModule(final String moduleName) {
        this.moduleName = moduleName;
        filesPerPackage = new HashMap<String, JavaPackage>();
    }

    /**
     * Adds the specified file to the maven module.
     *
     * @param workspaceFile the workspace file
     */
    public void add(final WorkspaceFile workspaceFile) {
        files.add(workspaceFile);
        String packageName = StringUtils.defaultIfEmpty(workspaceFile.getPackageName(), "Dummy");
        if (packageName != null) {
            if (!filesPerPackage.containsKey(packageName)) {
                filesPerPackage.put(packageName, new JavaPackage(packageName));
            }
            filesPerPackage.get(packageName).add(workspaceFile);
        }
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
     * Gets the modules of this project that have open tasks.
     *
     * @return the modules
     */
    public Collection<JavaPackage> getPackages() {
        return Collections.unmodifiableCollection(filesPerPackage.values());
    }

    /**
     * Gets the packages of this project that have open tasks.
     *
     * @param name
     *            the name of the package
     * @return the modules
     */
    public JavaPackage getPackage(final String name) {
        return filesPerPackage.get(name);
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

    /**
     * Gets the maximum number of tasks in a package of this module.
     *
     * @return the maximum number of tasks
     */
    public int getTaskBound() {
        int tasks = 0;
        for (JavaPackage javaPackage : filesPerPackage.values()) {
            tasks = Math.max(tasks, javaPackage.getNumberOfTasks());
        }
        return tasks;
    }
}

