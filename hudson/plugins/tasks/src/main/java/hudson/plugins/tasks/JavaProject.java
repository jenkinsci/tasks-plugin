package hudson.plugins.tasks;

import hudson.plugins.tasks.Task.Priority;

import java.util.ArrayList;
import java.util.List;

/**
 * Java Bean class representing a java project.
 */
public class JavaProject {
    /** Files with open tasks in this project. */
    private final List<JavaFile> files = new ArrayList<JavaFile>();

    /**
     * Adds a new file to this project.
     *
     * @param javaFile the file to add
     */
    public void addFile(final JavaFile javaFile) {
        files.add(javaFile);
    }

    /**
     * Returns the total number of tasks in this project.
     *
     * @return total number of tasks in this project.
     */
    public int getNumberOfTasks() {
        int numberOfTasks  = 0;
        for (JavaFile file : files) {
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
        for (JavaFile file : files) {
            numberOfTasks += file.getNumberOfTasks(priority);
        }
        return numberOfTasks;
    }
}

