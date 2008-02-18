package hudson.plugins.tasks;

import hudson.XmlFile;
import hudson.model.AbstractBuild;
import hudson.plugins.tasks.model.AnnotationProvider;
import hudson.plugins.tasks.model.AnnotationStream;
import hudson.plugins.tasks.model.JavaPackage;
import hudson.plugins.tasks.model.JavaProject;
import hudson.plugins.tasks.model.Priority;
import hudson.plugins.tasks.model.WorkspaceFile;
import hudson.plugins.tasks.parser.Task;
import hudson.plugins.tasks.util.SourceDetail;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
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
    private static final XStream XSTREAM = new AnnotationStream();

    static {
        XSTREAM.alias("task", Task.class);
    }

    /** The parsed project with annotations. */
    @SuppressWarnings("Se")
    private transient WeakReference<JavaProject> project;

    /** The total number of tasks. */
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
    public TasksResult(final AbstractBuild<?, ?> build, final JavaProject project, final String high, final String normal, final String low) {
        this(build, project, project.getNumberOfAnnotations(), high, normal, low);
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
    public TasksResult(final AbstractBuild<?, ?> build, final JavaProject project, final int previousNumberOfTasks, final String high, final String normal, final String low) {
        super(build, high, normal, low, project.getAnnotations());

        highPriorityTasks = project.getNumberOfAnnotations(Priority.HIGH);
        lowPriorityTasks = project.getNumberOfAnnotations(Priority.LOW);
        normalPriorityTasks = project.getNumberOfAnnotations(Priority.NORMAL);
        numberOfTasks = project.getNumberOfAnnotations();
        delta = numberOfTasks - previousNumberOfTasks;

        this.project = new WeakReference<JavaProject>(project);

        try {
            Collection<WorkspaceFile> files = project.getFiles();
            getDataFile().write(files.toArray(new WorkspaceFile[files.size()]));
        }
        catch (IOException exception) {
            LOGGER.log(Level.WARNING, "Failed to serialize the open tasks result.", exception);
        }
    }

    /** {@inheritDoc} */
    @Override
    public int getNumberOfAnnotations() {
        return numberOfTasks;
    }

    /** {@inheritDoc} */
    @Override
    public int getNumberOfAnnotations(final Priority priority) {
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

    /** {@inheritDoc} */
    @Override
    public int getNumberOfAnnotations(final String priority) {
        return getNumberOfAnnotations(Priority.valueOf(StringUtils.upperCase(priority)));
    }

    /**
     * Returns the display name (bread crumb name) of this result.
     *
     * @return the display name (bread crumb name) of this result.
     */
    public String getDisplayName() {
        return "Open Tasks";
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
    public Collection<JavaPackage> getPackages() {
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
        AnnotationProvider result = project.get();
        if (result == null) {
            loadResult();
        }
        return project.get();
    }

    /**
     * Loads the tasks results and wraps them in a weak reference that might
     * get removed by the garbage collector.
     */
    private void loadResult() {
        JavaProject result;
        try {
            JavaProject newProject = new JavaProject();
            WorkspaceFile[] files = (WorkspaceFile[])getDataFile().read();
            for (WorkspaceFile workspaceFile : files) {
                newProject.addAnnotations(workspaceFile.getAnnotations());
            }
            result = newProject;
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
        return new XmlFile(XSTREAM, new File(getOwner().getRootDir(), "open-tasks.xml"));
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
                return new SourceDetail(getOwner(), getAnnotation(link));
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
     * Returns a tooltip showing the distribution of priorities for the selected
     * package.
     *
     * @param name
     *            the package to show the distribution for
     * @return a tooltip showing the distribution of priorities
     */
    public String getToolTip(final String name) {
        if (isSingleModuleProject()) {
            return getProject().getModules().iterator().next().getPackage(name).getToolTip();
        }
        else {
            return getProject().getModule(name).getToolTip();
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
    public final void doModuleStatistics(final StaplerRequest request, final StaplerResponse response) throws IOException {
        createDetailGraph(request, response, getProject().getModule(request.getParameter("module")),
                getProject().getAnnotationBound());
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
        createDetailGraph(request, response, getProject().getPackage(request.getParameter("package")),
                getProject().getModules().iterator().next().getAnnotationBound());
    }
}