package hudson.plugins.tasks;


import hudson.plugins.tasks.Task.Priority;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        String moduleName = workspaceFile.getModuleName();
        if (moduleName != null) {
            if (!filesPerModule.containsKey(moduleName)) {
                filesPerModule.put(moduleName, new MavenModule(moduleName));
            }
            filesPerModule.get(moduleName).add(workspaceFile);
        }
    }

    /**
     * Returns the files.
     *
     * @return the files
     */
    public List<WorkspaceFile> getFiles() {
        return files;
    }

    public Collection<MavenModule> getModules() {
        return Collections.unmodifiableCollection(filesPerModule.values());
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
}

