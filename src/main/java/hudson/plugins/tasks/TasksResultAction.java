package hudson.plugins.tasks;

import hudson.model.AbstractBuild;
import hudson.plugins.tasks.util.AbstractResultAction;
import hudson.plugins.tasks.util.HealthReportBuilder;
import hudson.plugins.tasks.util.PluginDescriptor;

import java.util.NoSuchElementException;

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
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -3936658973355672416L;

    /**
     * Creates a new instance of <code>TasksResultAction</code>.
     *
     * @param owner
     *            the associated build of this action
     * @param result
     *            the result in this build
     * @param healthReportBuilder
     *            health builder to use
     */
    public TasksResultAction(final AbstractBuild<?, ?> owner, final TasksResult result, final HealthReportBuilder healthReportBuilder) {
        super(owner, healthReportBuilder, result);
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return Messages.Tasks_ProjectAction_Name();
    }

    /** {@inheritDoc} */
    @Override
    protected PluginDescriptor getDescriptor() {
        return TasksPublisher.TASK_SCANNER_DESCRIPTOR;
    }

    /**
     * Gets the tasks result of the previous build.
     *
     * @return the tasks result of the previous build.
     * @throws NoSuchElementException
     *             if there is no previous build for this action
     */
    public TasksResultAction getPreviousResultAction() {
        AbstractResultAction<TasksResult> previousBuild = getPreviousBuild();
        if (previousBuild instanceof TasksResultAction) {
            return (TasksResultAction)previousBuild;
        }
        throw new NoSuchElementException("There is no previous build for action " + this);
    }

    /** {@inheritDoc} */
    public String getMultipleItemsTooltip(final int numberOfItems) {
        return Messages.Tasks_ResultAction_MultipleWarnings(numberOfItems);
    }

    /** {@inheritDoc} */
    public String getSingleItemTooltip() {
        return Messages.Tasks_ResultAction_OneWarning();
    }
}
