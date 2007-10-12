package hudson.plugins.tasks;

import hudson.model.AbstractBuild;
import hudson.model.Build;
import hudson.plugins.tasks.Task.Priority;
import hudson.plugins.tasks.util.AbstractResultAction;
import hudson.plugins.tasks.util.HealthReportBuilder;
import hudson.plugins.tasks.util.ResultAction;
import hudson.util.DataSetBuilder;
import hudson.util.ChartUtil.NumberOnlyBuildLabel;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.lang.StringUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.CategoryDataset;
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
public class TasksResultAction extends AbstractResultAction implements ResultAction<TasksResult> {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -3936658973355672416L;
    /** URL to results. */
    private static final String TASKS_RESULT_URL = "tasksResult";
    /** The actual result of the FindBugs analysis. */
    private TasksResult result;

    /**
     * Creates a new instance of <code>FindBugsBuildAction</code>.
     *
     * @param owner
     *            the associated build of this action
     * @param result
     *            the result in this build
     * @param healthReportBuilder
     *            health builder to use
     */
    public TasksResultAction(final Build<?, ?> owner, final TasksResult result, final HealthReportBuilder healthReportBuilder) {
        super(owner, healthReportBuilder);
        this.result = result;
    }

    /** {@inheritDoc} */
    public Object getTarget() {
        return getResult();
    }

    /**
     * Returns the FindBugs result.
     *
     * @return the FindBugs result
     */
    public TasksResult getResult() {
        return result;
    }

    /** {@inheritDoc} */
    @Override
    protected int getHealthCounter() {
        return getResult().getNumberOfTasks();
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return "Open Tasks";
    }

    /** {@inheritDoc} */
    public String getIconFileName() {
        if (result.getNumberOfTasks() > 0) {
            return TasksDescriptor.TASKS_ACTION_LOGO;
        }
        else {
            return null;
        }
    }

    /** {@inheritDoc} */
    public String getUrlName() {
        return TASKS_RESULT_URL;
    }

    /**
     * Returns the URL for the results of the last build.
     *
     * @return URL for the results of the last build
     */
    public static String getLatestUrl() {
        return "../lastBuild/" + TASKS_RESULT_URL;
    }

    /**
     * Gets the FindBugs result of the previous build.
     *
     * @return the FindBugs result of the previous build.
     * @throws NoSuchElementException if there is no previous build for this action
     */
    public TasksResultAction getPreviousResultAction() {
        TasksResultAction previousBuild = getPreviousBuild();
        if (previousBuild == null) {
            throw new NoSuchElementException("There is no previous build for action " + this);
        }
        return previousBuild;
    }

    /**
     * Gets the test result of a previous build, if it's recorded, or <code>null</code> if not.
     *
     * @return the test result of a previous build, or <code>null</code>
     */
    private TasksResultAction getPreviousBuild() {
        AbstractBuild<?, ?> build = getOwner();
        while (true) {
            build = build.getPreviousBuild();
            if (build == null) {
                return null;
            }
            TasksResultAction action = build.getAction(TasksResultAction.class);
            if (action != null) {
                return action;
            }
        }
    }

    /**
     * Returns whether a previous build already did run with FindBugs.
     *
     * @return <code>true</code> if a previous build already did run with
     *         FindBugs.
     */
    public boolean hasPreviousResultAction() {
        return getPreviousBuild() != null;
    }

    /**
     * Sets the Tasks result for this build. The specified result will be persisted in the build folder
     * as an XML file.
     *
     * @param result the result to set
     */
    public void setResult(final TasksResult result) {
        this.result = result;
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
        return getHealthReportBuilder().createGraph(useHealthBuilder, TASKS_RESULT_URL, buildDataSet(useHealthBuilder));
    }

    /**
     * Returns the data set that represents the result. For each build, the
     * number of warnings is used as result value.
     *
     * @param useHealthBuilder
     *            determines whether the health builder should be used to create
     *            the data set
     * @return the data set
     */
    private CategoryDataset buildDataSet(final boolean useHealthBuilder) {
        DataSetBuilder<Integer, NumberOnlyBuildLabel> builder = new DataSetBuilder<Integer, NumberOnlyBuildLabel>();
        for (TasksResultAction action = this; action != null; action = action.getPreviousBuild()) {
            TasksResult current = action.getResult();
            if (current != null) {
                List<Integer> series;
                if (useHealthBuilder && getHealthReportBuilder().isEnabled()) {
                    series = getHealthReportBuilder().createSeries(current.getNumberOfTasks());
                }
                else {
                    series = new ArrayList<Integer>();
                    series.add(current.getNumberOfTasks(Priority.LOW));
                    series.add(current.getNumberOfTasks(Priority.NORMAL));
                    series.add(current.getNumberOfTasks(Priority.HIGH));
                }
                int level = 0;
                for (Integer integer : series) {
                    builder.add(integer, level, new NumberOnlyBuildLabel(action.getOwner()));
                    level++;
                }
            }
        }
        return builder.build();
    }
}
