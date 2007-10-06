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
import java.util.Set;

import org.jfree.chart.JFreeChart;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * Represents the details of a maven module.
 */
public class ModuleDetail implements ModelObject, Serializable {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -3743047168363581305L;
    /** The current build as owner of this object. */
    @SuppressWarnings("Se")
    private final TasksResult tasksResult;
    /** The selected module to show. */
    private final MavenModule module;
    /** The current build as owner of this action. */
    @SuppressWarnings("Se")
    private final Build<?, ?> owner;

    /**
     * Creates a new instance of <code>TaskDetail</code>.
     *
     * @param owner
     *            the current build as owner of this action
     * @param tasksResult
     *            the current build as owner of this action
     * @param module
     *            the selected package to show
     */
    public ModuleDetail(final Build<?, ?> owner, final TasksResult tasksResult, final MavenModule module) {
        this.owner = owner;
        this.tasksResult = tasksResult;
        this.module = module;
    }

    /**
     * Returns the owner.
     *
     * @return the owner
     */
    public TasksResult getTasksResult() {
        return tasksResult;
    }

    /**
     * Returns the owner.
     *
     * @return the owner
     */
    public Build<?,?> getOwner() {
        return owner;
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return module.getName();
    }

    /**
     * Gets the modules of this project that have open tasks.
     *
     * @return the modules
     */
    public Collection<JavaPackage> getPackages() {
        return module.getPackages();
    }

    /**
     * Returns the files.
     *
     * @return the files
     */
    public Set<WorkspaceFile> getFiles() {
        return module.getFiles();
    }

    /**
     * Returns the defined priorities.
     *
     * @return the defined priorities.
     */
    public List<String> getPriorities() {
        List<String> priorities = new ArrayList<String>();
        for (String priority : tasksResult.getPriorities()) {
            if (getNumberOfTasks(priority) > 0) {
                priorities.add(priority);
            }
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
    public String getTags(final String priority) {
        return tasksResult.getTags(priority);
    }

    /**
     * Returns the total number of tasks in this project.
     *
     * @return total number of tasks in this project.
     */
    public int getNumberOfTasks() {
        return module.getNumberOfTasks();
    }

    /**
     * Returns the number of tasks with the specified priority in this project.
     *
     * @param  priority the priority
     *
     * @return the number of tasks with the specified priority in this project.
     */
    public int getNumberOfTasks(final String priority) {
        return module.getNumberOfTasks(priority);
    }

    /**
     * Generates a PNG image for high/normal/low distribution of a java package.
     *
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @throws IOException
     *             in case of an error
     */
    public final void doPackageStatistics(final StaplerRequest request, final StaplerResponse response) throws IOException {
        if (ChartUtil.awtProblem) {
            response.sendRedirect2(request.getContextPath() + "/images/headless.png");
            return;
        }
        String packageName = request.getParameter("package");
        JavaPackage javaPackage = module.getPackage(packageName);
        ChartBuilder chartBuilder = new ChartBuilder();
        JFreeChart chart = chartBuilder.createHighNormalLowChart(
                javaPackage.getNumberOfTasks(Priority.HIGH),
                javaPackage.getNumberOfTasks(Priority.NORMAL),
                javaPackage.getNumberOfTasks(Priority.LOW), module.getTaskBound());
        ChartUtil.generateGraph(request, response, chart, 500, 20);
    }

    /**
     * Returns the dynamic result of the FindBugs analysis (detail page for a package).
     *
     * @param link the link to the source code
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @return the dynamic result of the FindBugs analysis (detail page for a package).
     */
    public Object getDynamic(final String link, final StaplerRequest request, final StaplerResponse response) {
        if (isSinglePackageModule()) {
            return new TaskDetail(owner, link);
        }
        else {
            return new PackageDetail(owner, tasksResult, module.getPackage(link));
        }
    }

    /**
     * Returns whether this result belongs to the last build.
     *
     * @return <code>true</code> if this result belongs to the last build
     */
    public boolean isCurrent() {
        return owner.getProject().getLastBuild().number == owner.number;
    }

    /**
     * Returns whether we only have a single module. In this case the module
     * statistics are suppressed and only the package statistics are shown.
     *
     * @return <code>true</code> for single module projects
     */
    public boolean isSinglePackageModule() {
        return module.getPackages().size() == 1;
    }
}

