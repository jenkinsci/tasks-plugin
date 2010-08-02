package hudson.plugins.tasks;

import hudson.model.AbstractProject;
import hudson.plugins.analysis.core.AbstractProjectAction;

/**
 * Entry point to visualize the task scanner trend graph. Drawing of the graph is
 * delegated to the associated {@link TasksResultAction}.
 *
 * @author Ulli Hafner
 */
public class TasksProjectAction extends AbstractProjectAction<TasksResultAction> {
    /**
     * Instantiates a new tasks project action.
     *
     * @param project
     *            the project that owns this action
     */
    public TasksProjectAction(final AbstractProject<?, ?> project) {
        super(project, TasksResultAction.class, new TasksDescriptor());
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return Messages.Tasks_ProjectAction_Name();
    }

    /** {@inheritDoc} */
    @Override
    public String getTrendName() {
        return Messages.Tasks_Trend_Name();
    }
}

