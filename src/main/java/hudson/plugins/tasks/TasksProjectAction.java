package hudson.plugins.tasks;

import hudson.model.AbstractProject;
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

    /**
     * Instantiates a new tasks project action.
     *
     * @param project
     *            the project that owns this action
     */
    public TasksProjectAction(final AbstractProject<?, ?> project) {
        super(project, TasksResultAction.class, TasksDescriptor.ACTION_ICON, TasksDescriptor.PLUGIN_NAME);
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return Messages.Tasks_ProjectAction_Name();
    }

    /** {@inheritDoc} */
    public String getUrlName() {
        return "tasks";
    }

    /** {@inheritDoc} */
    @Override
    public String getCookieName() {
        return "Tasks_displayMode";
    }

    /** {@inheritDoc} */
    @Override
    public String getTrendName() {
        return Messages.Tasks_Trend_Name();
    }
}

