package hudson.plugins.tasks;

import hudson.plugins.tasks.Task.Priority;

/**
 * Provides a tasks count for an object.
 */
public interface TasksProvider {
    /**
     * Returns the total number of tasks in this project.
     *
     * @return total number of tasks in this project.
     */
    int getNumberOfTasks();

    /**
     * Returns the number of tasks with the specified priority in this project.
     *
     * @param priority the priority
     *
     * @return the number of tasks with the specified priority in this project.
     */
    int getNumberOfTasks(final Priority priority);
}
