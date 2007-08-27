package hudson.plugins.tasks;


import hudson.plugins.tasks.Task.Priority;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Java Bean class representing a java file.
 */
public class WorkspaceFile implements Serializable {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 601361940925156719L;
    /** The tasks found in this file. Tasks are grouped by priority. */
    private transient Map<Priority, List<Task>> tasks;
    /** All tasks of this file. */
    private final List<Task> allTasks = new ArrayList<Task>();
    /** The absolute filename of this file. */
    private String name;

    /**
     * Creates a new instance of <code>JavaFile</code>.
     */
    public WorkspaceFile() {
        initializePriorityMapping();
    }

    /**
     * Returns the name of this file.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Initializes the priorities mapping.
     */
    private void initializePriorityMapping() {
        tasks = new HashMap<Priority, List<Task>>();
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
     * @param priority
     *            the task priority
     * @param lineNumber
     *            the line number of this task
     * @param message
     *            the message of this task
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
     * Returns all open tasks with the specified priority in this file.
     *
     * @param priority
     *            the priority of the tasks
     * @return open tasks in this file.
     */
    public List<Task> getTasks(final String priority) {
        return Collections.unmodifiableList(tasks.get(Priority.valueOf(priority)));
    }

    /**
     * Returns whether this file has some open tasks with the specified priority
     * in this file.
     *
     * @param priority
     *            the priority of the tasks
     * @return <code>true</code> if this file has some open tasks.
     */
    public boolean hasTasks(final String priority) {
        return !tasks.get(Priority.valueOf(priority)).isEmpty();
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

    /**
     * Sets the name of this file.
     *
     * @param name the name of this file
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Deserializes this instance. Uses the default deserialization and restores the priorities mapping.
     *
     * @param input input stream
     * @throws IOException in case of an IO error
     * @throws ClassNotFoundException in case of an class not found error
     */
    private void readObject(final ObjectInputStream input) throws IOException, ClassNotFoundException {
        input.defaultReadObject();

        initializePriorityMapping();
        for (Task task : allTasks) {
            tasks.get(task.getPriority()).add(task);
        }
    }
}

