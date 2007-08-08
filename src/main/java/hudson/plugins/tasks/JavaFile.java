package hudson.plugins.tasks;

import hudson.plugins.tasks.Task.Priority;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Java Bean class representing a java file.
 */
public class JavaFile {
    /** The tasks found in this file. Tasks are grouped by priority. */
    private final Map<Priority, List<Task>> tasks = new HashMap<Priority, List<Task>>();
    /** All tasks of this file. */
    private final List<Task> allTasks = new ArrayList<Task>();

    /**
     * Creates a new instance of <code>JavaFile</code>.
     */
    public JavaFile() {
        for (Priority priority : Priority.values()) {
            tasks.put(priority, new ArrayList<Task>());
        }
    }

    /**
     * Returns the total number of tasks in this file.
     *
     * @return total number of tasks in this file.
     */
    public int getNumberOfTasks() {
        int numberOfTasks = 0;
        for (Priority priority : Priority.values()) {
            numberOfTasks += tasks.get(priority).size();
        }
        return numberOfTasks;
    }

    /**
     * Adds a new task to this file.
     *
     * @param priority the task priority
     * @param lineNumber the line number of this task
     * @param message the message of this task
     */
    public void addTask(final Priority priority, final int lineNumber, final String message) {
        Task task = new Task(priority, lineNumber, message);

        tasks.get(priority).add(task);
        allTasks.add(task);
    }

    /**
     * Returns all open tasks in this file.
     *
     * @return open tasks in this file.
     */
    public List<Task> getTasks() {
        return Collections.unmodifiableList(allTasks);
    }

    /**
     * Returns whether this file has some open tasks.
     *
     * @return <code>true</code> if this file has some open tasks.
     */
    public boolean hasTasks() {
        return !allTasks.isEmpty();
    }

    /**
     * Returns the number of tasks with the specified priority in this file.
     *
     * @param  priority the priority
     *
     * @return the number of tasks with the specified priority in this file.
     */
    public int getNumberOfTasks(final Priority priority) {
        return tasks.get(priority).size();
    }
}

