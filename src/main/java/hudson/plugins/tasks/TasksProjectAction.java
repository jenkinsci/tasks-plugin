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
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 2726362508120270158L;

    /**
     * Instantiates a new tasks project action.
     *
     * @param project
     *            the project that owns this action
     */
    public TasksProjectAction(final AbstractProject<?, ?> project) {
        super(project, TasksResultAction.class, TasksPublisher.TASK_SCANNER_DESCRIPTOR);
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

