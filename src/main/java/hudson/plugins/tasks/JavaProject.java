package hudson.plugins.tasks;


import hudson.plugins.tasks.Task.Priority;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * Java Bean class representing a java project.
 */
public class JavaProject implements Serializable {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 8556968267678442661L;
    /** Files with open tasks in this project. */
    private final List<WorkspaceFile> files;
    /** Files with open tasks in this project. */
    private final Map<String, MavenModule> filesPerModule;
    /** Path of the workspace. */
    private String workspacePath;

    /**
     * Creates a new instance of <code>JavaProject</code>.
     */
    public JavaProject() {
        filesPerModule = new HashMap<String, MavenModule>();
        files = new ArrayList<WorkspaceFile>();
    }

    /**
     * Adds a new file to this project.
     *
     * @param workspaceFile the file to add
     */
    public void addFile(final WorkspaceFile workspaceFile) {
        files.add(workspaceFile);
        String moduleName = StringUtils.defaultIfEmpty(workspaceFile.getModuleName(), "Single Module");
        if (!filesPerModule.containsKey(moduleName)) {
            filesPerModule.put(moduleName, new MavenModule(moduleName));
        }
        filesPerModule.get(moduleName).add(workspaceFile);
    }

    /**
     * Returns the files.
     *
     * @return the files
     */
    public List<WorkspaceFile> getFiles() {
        return files;
    }

    /**
     * Gets the modules of this project that have open tasks.
     *
     * @return the modules
     */
    public Collection<MavenModule> getModules() {
        return Collections.unmodifiableCollection(filesPerModule.values());
    }

    /**
     * Gets the packages of this project that have open tasks.
     *
     * @return the modules
     */
    public Collection<JavaPackage> getPackages() {
        List<JavaPackage> packages = new ArrayList<JavaPackage>();
        for (MavenModule module : filesPerModule.values()) {
            packages.addAll(module.getPackages());
        }
        return packages;
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
     * @param priority the priority
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
     * Sets the path of the workspace.
     *
     * @param workspacePath path to workspace
     */
    public void setWorkspacePath(final String workspacePath) {
        this.workspacePath = workspacePath;
    }

    /**
     * Returns the workspace path.
     *
     * @return the workspace path
     */
    public String getWorkspacePath() {
        return workspacePath;
    }

    /**
     * Returns the specified maven module.
     *
     * @param moduleName the module to get
     *
     * @return the module
     */
    public MavenModule getModule(final String moduleName) {
        return filesPerModule.get(moduleName);
    }

    /**
     * Gets the maximum number of tasks in a module.
     *
     * @return the maximum number of tasks
     */
    public int getTaskBound() {
        int tasks = 0;
        for (MavenModule module : filesPerModule.values()) {
            tasks = Math.max(tasks, module.getNumberOfTasks());
        }
        return tasks;
    }

    /**
     * Returns the package with the given name.
     *
     * @param name the package name
     * @return the package with the given name.
     */
    public JavaPackage getPackage(final String name) {
        return filesPerModule.values().iterator().next().getPackage(name);
    }
}

