package hudson.plugins.tasks;

import hudson.model.Build;
import hudson.model.ModelObject;
import hudson.plugins.tasks.Task.Priority;
import hudson.plugins.tasks.util.ChartBuilder;
import hudson.util.ChartUtil;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jfree.chart.JFreeChart;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * Provides common functionality of the different kind of tasks results details.
 */
public abstract class AbstractTasksResult implements ModelObject, Serializable {
    /** The current build as owner of this action. */
    @SuppressWarnings("Se")
    private final Build<?, ?> owner;
    /** Tag identifiers indicating high priority. */
    private final String high;
    /** Tag identifiers indicating normal priority. */
    private final String normal;
    /** Tag identifiers indicating low priority. */
    private final String low;

    /**
     * Creates a new instance of <code>AbstractTasksDetail</code>.
     *
     * @param owner
     *            the current build as owner of this result object
     * @param high
     *            tag identifiers indicating high priority
     * @param normal
     *            tag identifiers indicating normal priority
     * @param low
     *            tag identifiers indicating low priority
     */
    public AbstractTasksResult(final Build<?, ?> owner, final String high, final String normal, final String low) {
        this.owner = owner;
        this.high = high;
        this.normal = normal;
        this.low = low;
    }

    /**
     * Creates a new instance of <code>AbstractTasksResult</code>.
     *
     * @param root
     *            the root result object that is used to get the available tasks
     */
    public AbstractTasksResult(final AbstractTasksResult root) {
        this(root.owner, root.high, root.normal, root.low);
    }

    /**
     * Returns the files of this result object.
     *
     * @return the files of this result object
     */
    public abstract Collection<WorkspaceFile> getFiles();

    /**
     * Returns the package category name for the scanned files. Currently, only
     * java and c# files are supported.
     *
     * @return the package category name for the scanned files
     */
    public String getPackageCategoryName() {
        if (!getFiles().isEmpty()) {
            WorkspaceFile file = getFiles().iterator().next();
            if (file.getShortName().endsWith(".cs")) {
                return "Namespace";
            }
        }
        return "Package";
    }

    /**
     * Returns the current build as owner of this result object.
     *
     * @return the owner of this details object
     */
    public final Build<?, ?> getOwner() {
        return owner;
    }

    /**
     * Returns whether this result object belongs to the last build.
     *
     * @return <code>true</code> if this result belongs to the last build
     */
    public final boolean isCurrent() {
        return getOwner().getProject().getLastBuild().number == getOwner().number;
    }

    /**
     * Returns the actually used priorities.
     *
     * @return the actually used priorities.
     */
    public List<String> getPriorities() {
        List<String> actualPriorities = new ArrayList<String>();
        for (String priority : getAvailablePriorities()) {
            if (getNumberOfTasks(priority) > 0) {
                actualPriorities.add(priority);
            }
        }
        return actualPriorities;
    }

    /**
     * Returns the defined priorities.
     *
     * @return the defined priorities.
     */
    public Collection<String> getAvailablePriorities() {
        ArrayList<String> priorities = new ArrayList<String>();
        if (StringUtils.isNotEmpty(high)) {
            priorities.add(StringUtils.capitalize(StringUtils.lowerCase(Priority.HIGH.name())));
        }
        if (StringUtils.isNotEmpty(normal)) {
            priorities.add(StringUtils.capitalize(StringUtils.lowerCase(Priority.NORMAL.name())));
        }
        if (StringUtils.isNotEmpty(low)) {
            priorities.add(StringUtils.capitalize(StringUtils.lowerCase(Priority.LOW.name())));
        }
        return priorities;
    }

    /**
     * Returns the tags for the specified priority.
     *
     * @param priority
     *            the priority
     * @return the tags for priority high
     */
    public final String getTags(final String priority) {
        Priority converted = Priority.valueOf(StringUtils.upperCase(priority));
        if (converted == Priority.HIGH) {
            return high;
        }
        else if (converted == Priority.NORMAL) {
            return normal;
        }
        else {
            return low;
        }
    }

    /**
     * Returns the total number of tasks in this project.
     *
     * @return total number of tasks in this project.
     */
    public int getNumberOfTasks() {
        int numberOfTasks = 0;
        for (Priority priority : Priority.values()) {
            numberOfTasks += getNumberOfTasks(priority);
        }
        return numberOfTasks;
    }

    /**
     * Returns the number of tasks with the specified priority for this object.
     *
     * @param priority
     *            the priority
     * @return the number of tasks with the specified priority in this project.
     */
    public abstract int getNumberOfTasks(Priority priority);

    /**
     * Returns the number of tasks with the specified priority.
     *
     * @param  priority the priority
     *
     * @return the number of tasks with the specified priority
     */
    public final int getNumberOfTasks(final String priority) {
        return getNumberOfTasks(Priority.valueOf(StringUtils.upperCase(priority)));
    }

    /**
     * Creates a detail graph for the specified detail object.
     *
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @param detailObject
     *            the detail object to compute the graph for
     * @param upperBound
     *            the upper bound of all tasks
     * @throws IOException
     *             in case of an error
     */
    protected final void createDetailGraph(final StaplerRequest request, final StaplerResponse response,
            final TasksProvider detailObject, final int upperBound) throws IOException {
        if (ChartUtil.awtProblem) {
            response.sendRedirect2(request.getContextPath() + "/images/headless.png");
            return;
        }
        ChartBuilder chartBuilder = new ChartBuilder();
        JFreeChart chart = chartBuilder.createHighNormalLowChart(
                detailObject.getNumberOfTasks(Priority.HIGH),
                detailObject.getNumberOfTasks(Priority.NORMAL),
                detailObject.getNumberOfTasks(Priority.LOW), upperBound);
        ChartUtil.generateGraph(request, response, chart, 400, 20);
    }
}
