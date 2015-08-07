package hudson.plugins.tasks;

import hudson.model.AbstractBuild;
import hudson.model.Run;

import hudson.plugins.analysis.core.HealthDescriptor;
import hudson.plugins.analysis.core.PluginDescriptor;
import hudson.plugins.analysis.core.AbstractResultAction;

/**
 * Controls the live cycle of the task scanner results. This action persists the
 * results of the task scanner of a build and displays the results on the
 * build page. The actual visualization of the results is defined in the
 * matching <code>summary.jelly</code> file.
 * <p>
 * Moreover, this class renders the tasks scanner result trend.
 * </p>
 *
 * @author Ulli Hafner
 */
public class TasksResultAction extends AbstractResultAction<TasksResult>  {

    /**
     * Creates a new instance of <code>TasksResultAction</code>.
     *
     * @param owner
     *            the associated owner of this action
     * @param healthDescriptor
     *            health descriptor to use
     * @param result
     *            the result in this build
     */
    public TasksResultAction(final Run<?, ?> owner, final HealthDescriptor healthDescriptor, final TasksResult result) {
        super(owner, new TasksHealthDescriptor(healthDescriptor), result);
    }

    @Override
    public String getDisplayName() {
        return Messages.Tasks_ProjectAction_Name();
    }

    @Override
    protected PluginDescriptor getDescriptor() {
        return new TasksDescriptor();
    }

    @Override
    public String getMultipleItemsTooltip(final int numberOfItems) {
        return Messages.Tasks_ResultAction_MultipleWarnings(numberOfItems);
    }

    @Override
    public String getSingleItemTooltip() {
        return Messages.Tasks_ResultAction_OneWarning();
    }

    /**
     * Creates a new instance of <code>TasksResultAction</code>.
     *
     * @param owner
     *            the associated build of this action
     * @param healthDescriptor
     *            health descriptor to use
     * @param result
     *            the result in this build
     * @deprecated use {@link #TasksResultAction(Run, HealthDescriptor, TasksResult)} instead
     */
    @Deprecated
    public TasksResultAction(final AbstractBuild<?, ?> owner, final HealthDescriptor healthDescriptor, final TasksResult result) {
        this((Run<?, ?>) owner, healthDescriptor, result);
    }
}
