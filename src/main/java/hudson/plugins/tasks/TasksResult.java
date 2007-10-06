package hudson.plugins.tasks;

import hudson.XmlFile;
import hudson.model.Build;
import hudson.plugins.tasks.Task.Priority;
import hudson.plugins.tasks.util.ChartBuilder;
import hudson.util.ChartUtil;
import hudson.util.StringConverter2;
import hudson.util.XStream2;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jfree.chart.JFreeChart;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import com.thoughtworks.xstream.XStream;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * Represents the results of the task  scanner. One instance of this class is persisted for
 * each build via an XML file.
 *
 * @author Ulli Hafner
 */
public class TasksResult extends AbstractTasksResult {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -344808345805935004L;
    /** Error logger. */
    private static final Logger LOGGER = Logger.getLogger(TasksResult.class.getName());
    /** Serialization provider. */
    private static final XStream XSTREAM = new XStream2();

    static {
        XSTREAM.alias("project", JavaProject.class);
        XSTREAM.registerConverter(new StringConverter2(), 100);
    }

    /** The parsed FindBugs result. */
    @SuppressWarnings("Se")
    private transient WeakReference<JavaProject> project;
    /** The number of tasks in this build. */
    private final int numberOfTasks;
    /** Difference between this and the previous build. */
    private final int delta;
    /** The number of high priority tasks in this build. */
    private final int highPriorityTasks;
    /** The number of low priority tasks in this build. */
    private final int lowPriorityTasks;
    /** The number of normal priority tasks in this build. */
    private final int normalPriorityTasks;

    /**
     * Creates a new instance of <code>TasksResult</code>.
     *
     * @param build
     *            the current build as owner of this action
     * @param project
     *            the parsed FindBugs result
     * @param high
     *            tag identifiers indicating high priority
     * @param normal
     *            tag identifiers indicating normal priority
     * @param low
     *            tag identifiers indicating low priority
     */
    public TasksResult(final Build<?, ?> build, final JavaProject project, final String high, final String normal, final String low) {
        this(build, project, project.getNumberOfTasks(), high, normal, low);
    }

    /**
     * Creates a new instance of <code>TasksResult</code>.
     *
     * @param build the current build as owner of this action
     * @param project the parsed FindBugs result
     * @param previousNumberOfTasks the previous number of open tasks
     * @param high
     *            tag identifiers indicating high priority
     * @param normal
     *            tag identifiers indicating normal priority
     * @param low
     *            tag identifiers indicating low priority
     */
    public TasksResult(final Build<?, ?> build, final JavaProject project, final int previousNumberOfTasks, final String high, final String normal, final String low) {
        super(build, high, normal, low);
        highPriorityTasks = project.getNumberOfTasks(Priority.HIGH);
        lowPriorityTasks = project.getNumberOfTasks(Priority.LOW);
        normalPriorityTasks = project.getNumberOfTasks(Priority.NORMAL);
        numberOfTasks = project.getNumberOfTasks();
        this.project = new WeakReference<JavaProject>(project);
        delta = numberOfTasks - previousNumberOfTasks;

        try {
            getDataFile().write(project);
        }
        catch (IOException exception) {
            LOGGER.log(Level.WARNING, "Failed to serialize the open tasks result.", exception);
        }
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return "Open Tasks";
    }

    /**
     * Returns the number of tasks with the specified priority.
     *
     * @param  priority the priority
     *
     * @return the number of tasks with the specified priority
     */
    @Override
    public int getNumberOfTasks(final Priority priority) {
        if (priority == Priority.HIGH) {
            return highPriorityTasks;
        }
        else if (priority == Priority.NORMAL) {
            return normalPriorityTasks;
        }
        else {
            return lowPriorityTasks;
        }
    }

    /**
     * Returns the delta.
     *
     * @return the delta
     */
    public int getDelta() {
        return delta;
    }

    /**
     * Returns the packages in this project.
     *
     * @return the packages in this project
     */
    public Collection<JavaPackage> getPackages () {
        return getProject().getPackages();
    }

    /**
     * Returns the associated project of this result.
     *
     * @return the associated project of this result.
     */
    public JavaProject getProject() {
        if (project == null) {
            loadResult();
        }
        JavaProject result = project.get();
        if (result == null) {
            loadResult();
        }
        return project.get();
    }

    /**
     * Loads the FindBugs results and wraps them in a weak reference that might
     * get removed by the garbage collector.
     */
    private void loadResult() {
        JavaProject result;
        try {
            result = (JavaProject)getDataFile().read();
        }
        catch (IOException exception) {
            LOGGER.log(Level.WARNING, "Failed to load " + getDataFile(), exception);
            result = new JavaProject();
        }
        project = new WeakReference<JavaProject>(result);
    }

    /**
     * Returns the serialization file.
     *
     * @return the serialization file.
     */
    private XmlFile getDataFile() {
        return new XmlFile(XSTREAM, new File(getOwner().getRootDir(), "openTasks.xml"));
    }

    /**
     * Returns the dynamic result of this tasks detail view. Depending on the
     * number of modules and packages, one of the following detail objects is
     * returned:
     * <ul>
     * <li>A task detail object for a single workspace file (if the project
     * contains only one package and one module).</li>
     * <li>A package detail object for a specified package (if the project
     * contains only one module).</li>
     * <li>A module detail object for a specified module (in any other case).</li>
     * </ul>
     *
     * @param link
     *            the link to the source code
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @return the dynamic result of the FindBugs analysis (detail page for a
     *         package).
     */
    public Object getDynamic(final String link, final StaplerRequest request, final StaplerResponse response) {
        if (isSingleModuleProject()) {
            if (isSinglePackageProject()) {
                return new TaskDetail(getOwner(), link);
            }
            else {
                return new PackageDetail(this, getProject().getPackage(link));
            }
        }
        else {
            return new ModuleDetail(this, getProject().getModule(link));
        }
    }

    /**
     * Returns whether we only have a single module. In this case the module
     * statistics are suppressed and only the package statistics are shown.
     *
     * @return <code>true</code> for single module projects
     */
    public boolean isSingleModuleProject() {
        return getProject().getModules().size() == 1;
    }

    /**
     * Returns whether we only have a single package. In this case the module
     * and package statistics are suppressed and only the tasks are shown.
     *
     * @return <code>true</code> for single module projects
     */
    public boolean isSinglePackageProject() {
        return isSingleModuleProject() && getProject().getPackages().size() == 1;
    }

    /**
     * Generates a PNG image for high/normal/low distribution of a maven module.
     *
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @throws IOException
     *             in case of an error
     */
    public final void doModuleStatistics(final StaplerRequest request,
            final StaplerResponse response) throws IOException {
        if (ChartUtil.awtProblem) {
            response.sendRedirect2(request.getContextPath() + "/images/headless.png");
            return;
        }
        String moduleName = request.getParameter("module");
        MavenModule module = getProject().getModule(moduleName);

        ChartBuilder chartBuilder = new ChartBuilder();
        JFreeChart chart = chartBuilder.createHighNormalLowChart(
                module.getNumberOfTasks(Priority.HIGH),
                module.getNumberOfTasks(Priority.NORMAL),
                module.getNumberOfTasks(Priority.LOW), getProject().getTaskBound());
        ChartUtil.generateGraph(request, response, chart, 500, 20);
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
    public final void doPackageStatistics(final StaplerRequest request,
            final StaplerResponse response) throws IOException {
        if (ChartUtil.awtProblem) {
            response.sendRedirect2(request.getContextPath() + "/images/headless.png");
            return;
        }
        String packageName = request.getParameter("package");
        JavaPackage javaPackage = getProject().getPackage(packageName);

        ChartBuilder chartBuilder = new ChartBuilder();
        JFreeChart chart = chartBuilder.createHighNormalLowChart(
                javaPackage.getNumberOfTasks(Priority.HIGH),
                javaPackage.getNumberOfTasks(Priority.NORMAL),
                javaPackage.getNumberOfTasks(Priority.LOW), getProject().getModules().iterator().next().getTaskBound());
        ChartUtil.generateGraph(request, response, chart, 500, 20);
    }
}
