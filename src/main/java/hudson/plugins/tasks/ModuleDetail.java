package hudson.plugins.tasks;

import hudson.plugins.tasks.Task.Priority;
import hudson.plugins.tasks.util.ChartBuilder;
import hudson.util.ChartUtil;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import org.jfree.chart.JFreeChart;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Represents the details of a maven module.
 */
public class ModuleDetail extends AbstractTasksResult {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -3743047168363581305L;
    /** The selected module to show. */
    private final MavenModule module;
    /** The root of the tasks results. */
    private final AbstractTasksResult root;

    /**
     * Creates a new instance of <code>PackageDetail</code>.
     *
     * @param root
     *            the root result object that is used to get the available tasks
     * @param module
     *            the selected module to show
     */
    public ModuleDetail(final AbstractTasksResult root, final MavenModule module) {
        super(root);
        this.root = root;
        this.module = module;
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
     * Returns the number of tasks with the specified priority in this module.
     *
     * @param  priority the priority
     *
     * @return the number of tasks with the specified priority in this project.
     */
    @Override
    public int getNumberOfTasks(final Priority priority) {
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
     * Returns the dynamic result of this module detail view, which is either a
     * task detail object for a single workspace file or a package detail
     * object.
     *
     * @param link
     *            the link containing the path to the selected workspace file
     *            (or package)
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @return the dynamic result of the FindBugs analysis (detail page for a
     *         package).
     * @see #isSinglePackageModule()
     */
    public Object getDynamic(final String link, final StaplerRequest request, final StaplerResponse response) {
        if (isSinglePackageModule()) {
            return new TaskDetail(getOwner(), link);
        }
        else {
            return new PackageDetail(root, module.getPackage(link));
        }
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
