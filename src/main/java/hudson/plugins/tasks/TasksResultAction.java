package hudson.plugins.tasks;

import hudson.model.AbstractBuild;
import hudson.model.Build;
import hudson.model.HealthReport;
import hudson.model.HealthReportingAction;
import hudson.plugins.tasks.util.ChartBuilder;
import hudson.plugins.tasks.util.HealthReportBuilder;
import hudson.plugins.tasks.util.PrioritiesAreaRenderer;
import hudson.plugins.tasks.util.ResultAreaRenderer;
import hudson.util.ChartUtil;
import hudson.util.DataSetBuilder;
import hudson.util.ChartUtil.NumberOnlyBuildLabel;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.category.StackedAreaRenderer;
import org.jfree.data.category.CategoryDataset;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

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
public class TasksResultAction implements StaplerProxy, HealthReportingAction {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -3936658973355672416L;
    /** URL to results. */
    private static final String TASKS_RESULT_URL = "tasksResult";
    /** Height of the graph. */
    private static final int HEIGHT = 200;
    /** Width of the graph. */
    private static final int WIDTH = 500;
    /** The associated build of this action. */
    @SuppressWarnings("Se")
    private final Build<?, ?> owner;
    /** The actual result of the FindBugs analysis. */
    private TasksResult result;
    /** Builds a health report. */
    private final HealthReportBuilder healthReportBuilder;

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
        this.owner = owner;
        this.result = result;
        this.healthReportBuilder = healthReportBuilder;
    }

    /**
     * Returns the associated build of this action.
     *
     * @return the associated build of this action
     */
    public Build<?, ?> getOwner() {
        return owner;
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
    public HealthReport getBuildHealth() {
        return healthReportBuilder.computeHealth(getResult().getNumberOfTasks());
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
     * Returns the URL for the latest tasks results.
     *
     * @return URL for the latest tasks results.
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
    public TasksResultAction getPreviousResult() {
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
        AbstractBuild<?, ?> build = owner;
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
    public boolean hasPreviousResult() {
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
     * Generates a PNG image for the result trend.
     *
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @throws IOException
     *             in case of an error in
     *             {@link TasksResultAction#doGraph(StaplerRequest, StaplerResponse)}
     */
    public void doGraph(final StaplerRequest request, final StaplerResponse response) throws IOException {
        if (ChartUtil.awtProblem) {
            response.sendRedirect2(request.getContextPath() + "/images/headless.png");
            return;
        }
        if (request.checkIfModified(owner.getTimestamp(), response) || healthReportBuilder == null) {
            return;
        }
        ChartUtil.generateGraph(request, response, createChart(), WIDTH, HEIGHT);
    }

    /**
     * Generates a PNG image for the result trend.
     *
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @throws IOException
     *             in case of an error in
     *             {@link TasksResultAction#doGraph(StaplerRequest, StaplerResponse)}
     */
    public void doGraphMap(final StaplerRequest request, final StaplerResponse response) throws IOException {
        if (request.checkIfModified(owner.getTimestamp(), response) || healthReportBuilder == null) {
            return;
        }
        ChartUtil.generateClickableMap(request, response, createChart(), WIDTH, HEIGHT);
    }

    /**
     * Creates the chart for this action.
     *
     * @return the chart for this action.
     */
    private JFreeChart createChart() {
        ChartBuilder chartBuilder = new ChartBuilder();
        StackedAreaRenderer renderer;
        if (healthReportBuilder.isHealthyReportEnabled() || healthReportBuilder.isFailureThresholdEnabled()) {
            renderer = new ResultAreaRenderer(TASKS_RESULT_URL, "open task");
        }
        else {
            renderer = new PrioritiesAreaRenderer(TASKS_RESULT_URL, "open task");
        }
        return chartBuilder.createChart(buildDataSet(), renderer, healthReportBuilder.getThreshold(),
                healthReportBuilder.isHealthyReportEnabled() || !healthReportBuilder.isFailureThresholdEnabled());
    }

    /**
     * Returns the data set that represents the result. For each build, the
     * number of warnings is used as result value.
     *
     * @return the data set
     */
    private CategoryDataset buildDataSet() {
        DataSetBuilder<Integer, NumberOnlyBuildLabel> builder = new DataSetBuilder<Integer, NumberOnlyBuildLabel>();
        for (TasksResultAction action = this; action != null; action = action.getPreviousBuild()) {
            TasksResult current = action.getResult();
            if (healthReportBuilder != null && current != null) {
                List<Integer> series = healthReportBuilder.createSeries(current.getNumberOfHighPriorityTasks(), current.getNumberOfNormalPriorityTasks(), current.getNumberOfLowPriorityTasks());
                int level = 0;
                for (Integer integer : series) {
                    builder.add(integer, level, new NumberOnlyBuildLabel(action.owner));
                    level++;
                }
            }
        }
        return builder.build();
    }
}
