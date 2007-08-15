package hudson.plugins.tasks;

import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.Build;
import hudson.model.HealthReport;
import hudson.model.HealthReportingAction;
import hudson.plugins.util.HealthReportBuilder;
import hudson.util.ChartUtil;
import hudson.util.ColorPalette;
import hudson.util.DataSetBuilder;
import hudson.util.ShiftedCategoryAxis;
import hudson.util.StackedAreaRenderer2;
import hudson.util.ChartUtil.NumberOnlyBuildLabel;

import java.awt.Color;
import java.io.IOException;
import java.util.NoSuchElementException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.StackedAreaRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.RectangleInsets;
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
     * @param isHealthyReportEnabled
     *            Determines whether to use the provided healthy thresholds.
     * @param healthy
     *            Report health as 100% when the number of warnings is less than
     *            this value
     * @param unHealthy
     *            Report health as 0% when the number of warnings is greater
     *            than this value
     */
    public TasksResultAction(final Build<?, ?> owner, final TasksResult result, final boolean isHealthyReportEnabled, final int healthy, final int unHealthy) {
        this.owner = owner;
        this.result = result;
        healthReportBuilder = new HealthReportBuilder("Task Scanner", "open task", isHealthyReportEnabled, healthy, unHealthy);
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
        return "taskResult";
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
     * Sets the FindBugs result for this build. The specified result will be persisted in the build folder
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

        if (request.checkIfModified(owner.getTimestamp(), response)) {
            return;
        }
        ChartUtil.generateGraph(request, response, createChart(request), WIDTH, HEIGHT);
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
        if (request.checkIfModified(owner.getTimestamp(), response)) {
            return;
        }
        ChartUtil.generateClickableMap(request, response, createChart(request), WIDTH, HEIGHT);
    }

    /**
     * Returns the data set that represents the result. For each build, the
     * number of tasks is used as result value.
     *
     * @param request
     *            Stapler request
     * @return the data set
     */
    private CategoryDataset buildDataSet(final StaplerRequest request) {
        DataSetBuilder<String, NumberOnlyBuildLabel> builder = new DataSetBuilder<String, NumberOnlyBuildLabel>();

        for (TasksResultAction action = this; action != null; action = action.getPreviousBuild()) {
            TasksResult taskResult = action.getResult();
            builder.add(taskResult.getNumberOfLowPriorityTasks(), "0", new NumberOnlyBuildLabel(action.owner));
            builder.add(taskResult.getNumberOfNormalPriorityTasks(), "1", new NumberOnlyBuildLabel(action.owner));
            builder.add(taskResult.getNumberOfHighPriorityTasks(), "2", new NumberOnlyBuildLabel(action.owner));
        }
        return builder.build();
    }

    /**
     * Creates the actual chart for the open tasks trend.
     *
     * @param request
     *            Stapler Request
     * @return the chart
     */
    private JFreeChart createChart(final StaplerRequest request) {
        CategoryDataset dataset = buildDataSet(request);
        JFreeChart chart = ChartFactory.createStackedAreaChart(
            null,                     // chart title
            null,                     // unused
            "count",                  // range axis label
            dataset,                  // data
            PlotOrientation.VERTICAL, // orientation
            false,                    // include legend
            true,                     // tooltips
            false                     // urls
        );

        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...

        chart.setBackgroundPaint(Color.white);

        final CategoryPlot plot = chart.getCategoryPlot();

        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(null);
        plot.setForegroundAlpha(0.8f);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.black);

        CategoryAxis domainAxis = new ShiftedCategoryAxis(null);
        plot.setDomainAxis(domainAxis);
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
        domainAxis.setLowerMargin(0.0);
        domainAxis.setUpperMargin(0.0);
        domainAxis.setCategoryMargin(0.0);

        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        StackedAreaRenderer renderer = new AreaRenderer();
        plot.setRenderer(renderer);
        renderer.setSeriesPaint(0, ColorPalette.BLUE);
        renderer.setSeriesPaint(1, new Color(240, 240, 10));
        renderer.setSeriesPaint(2, ColorPalette.RED);

        // crop extra space around the graph
        plot.setInsets(new RectangleInsets(0, 0, 0, 5.0));

        return chart;
    }

    /**
     * Renderer that provides access to the individual open tasks results.
     */
    static final class AreaRenderer extends StackedAreaRenderer2 {
        /** Unique identifier of this class. */
        private static final long serialVersionUID = -4683951507836348304L;

        /** {@inheritDoc} */
        @Override
        public String generateURL(final CategoryDataset dataset, final int row, final int column) {
            NumberOnlyBuildLabel label = (NumberOnlyBuildLabel)dataset.getColumnKey(column);
            TasksResultAction action = label.build.getAction(TasksResultAction.class);

            if (action.getResult().getNumberOfTasks() > 0) {
                return label.build.getNumber() + "/tasksResult/";
            }
            else {
                return null;
            }
        }

        /** {@inheritDoc} */
        @Override
        public String generateToolTip(final CategoryDataset dataset, final int row, final int column) {
            NumberOnlyBuildLabel label = (NumberOnlyBuildLabel) dataset.getColumnKey(column);
            TasksResultAction action = label.build.getAction(TasksResultAction.class);
            if (row == 2) {
                return Util.combine(action.getResult().getNumberOfHighPriorityTasks(), "high priority task");
            }
            else if (row == 1) {
                return Util.combine(action.getResult().getNumberOfNormalPriorityTasks(), "normal task");
            }
            else {
                return Util.combine(action.getResult().getNumberOfLowPriorityTasks(), "low priority task");
            }
        }
    }
}
