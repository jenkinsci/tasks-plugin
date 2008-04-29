package hudson.plugins.tasks;

import hudson.model.AbstractBuild;
import hudson.plugins.tasks.util.AbstractResultAction;
import hudson.plugins.tasks.util.HealthReportBuilder;

import java.util.NoSuchElementException;

import org.apache.commons.lang.StringUtils;
import org.jfree.chart.JFreeChart;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

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
    /** URL to results. */
    private static final String TASKS_RESULT_URL = "tasksResult";

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
    public String getIconUrl() {
        return TasksDescriptor.ACTION_ICON;
    }

    /** {@inheritDoc} */
    public String getUrlName() {
        return TASKS_RESULT_URL;
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

    /**
     * Creates the chart for this action.
     *
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @return the chart for this action.
     */
    @Override
    protected JFreeChart createChart(final StaplerRequest request, final StaplerResponse response) {
        String parameter = request.getParameter("useHealthBuilder");
        boolean useHealthBuilder = Boolean.valueOf(StringUtils.defaultIfEmpty(parameter, "true"));
        return getHealthReportBuilder().createGraph(useHealthBuilder, TASKS_RESULT_URL, buildDataSet(useHealthBuilder),
                Messages.Tasks_ResultAction_OneWarning(),
                Messages.Tasks_ResultAction_MultipleWarnings("%d"));
    }
}
