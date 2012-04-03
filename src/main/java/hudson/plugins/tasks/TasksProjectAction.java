package hudson.plugins.tasks;

import hudson.model.AbstractProject;
import hudson.plugins.analysis.core.ResultAction;
import hudson.plugins.analysis.core.AbstractProjectAction;

/**
 * Entry point to visualize the task scanner trend graph. Drawing of the graph is
 * delegated to the associated {@link TasksResultAction}.
 *
 * @author Ulli Hafner
 */
public class TasksProjectAction extends AbstractProjectAction<ResultAction<TasksResult>> {
    /**
     * Instantiates a new {@link TasksProjectAction}.
     *
     * @param project
     *            the project that owns this action
     */
    public TasksProjectAction(final AbstractProject<?, ?> project) {
        this(project, TasksResultAction.class);
    }

    /**
     * Instantiates a new {@link TasksProjectAction}.
     *
     * @param project
     *            the project that owns this action
     * @param type
     *            the result action type
     */
    public TasksProjectAction(final AbstractProject<?, ?> project,
            final Class<? extends ResultAction<TasksResult>> type) {
        super(project, type, Messages._Tasks_ProjectAction_Name(), Messages._Tasks_Trend_Name(),
                TasksDescriptor.PLUGIN_ID, TasksDescriptor.ICON_URL, TasksDescriptor.RESULT_URL);
    }
}

