package hudson.plugins.tasks;

import hudson.model.Project;
import hudson.plugins.tasks.util.AbstractProjectAction;

/**
 * Entry point to visualize the task scanner trend graph. Drawing of the graph is
 * delegated to the associated {@link TasksResultAction}.
 *
 * @author Ulli Hafner
 */
public class TasksProjectAction extends AbstractProjectAction<TasksResultAction> {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 2726362508120270158L;
    /** URL for this action. */
    private static final String TASKS_URL = "tasks";

    /**
     * Instantiates a new tasks project action.
     *
     * @param project
     *            the project that owns this action
     */
    public TasksProjectAction(final Project<?, ?> project) {
        super(project, TasksResultAction.class, TasksDescriptor.TASKS_ACTION_LOGO, TasksResultAction.getLatestUrl());
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return "Open Tasks";
    }

    /** {@inheritDoc} */
    public String getUrlName() {
        return TASKS_URL;
    }

    /** {@inheritDoc} */
    @Override
    protected String getCookieName() {
        return "Tasks_displayMode";
    }
}

