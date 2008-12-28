package hudson.plugins.tasks; // NOPMD

import hudson.model.AbstractBuild;
import hudson.plugins.tasks.parser.Task;
import hudson.plugins.tasks.parser.TasksParserResult;
import hudson.plugins.tasks.util.BuildResult;
import hudson.plugins.tasks.util.model.AnnotationContainer;
import hudson.plugins.tasks.util.model.AnnotationProvider;
import hudson.plugins.tasks.util.model.FileAnnotation;
import hudson.plugins.tasks.util.model.JavaPackage;
import hudson.plugins.tasks.util.model.JavaProject;
import hudson.plugins.tasks.util.model.MavenModule;
import hudson.plugins.tasks.util.model.Priority;
import hudson.plugins.tasks.util.model.WorkspaceFile;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * Represents the results of the task  scanner. One instance of this class is persisted for
 * each build via an XML file.
 *
 * @author Ulli Hafner
 */
//CHECKSTYLE:COUPLING-OFF
@SuppressWarnings("PMD.TooManyFields")
public class TasksResult extends BuildResult  {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -344808345805935004L;
    /** Error logger. */
    private static final Logger LOGGER = Logger.getLogger(TasksResult.class.getName());
    static {
        XSTREAM.alias("task", Task.class);
    }

    /** The parsed project with annotations. */
    @SuppressWarnings("Se")
    private transient WeakReference<JavaProject> project;

    /** Tag identifiers indicating high priority. */
    private final String high;
    /** Tag identifiers indicating normal priority. */
    private final String normal;
    /** Tag identifiers indicating low priority. */
    private final String low;

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
    /** The number of scanned files in the project. */
    private final int numberOfFiles;

    /**
     * Creates a new instance of <code>TasksResult</code>.
     *
     * @param build
     *            the current build as owner of this action
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param project
     *            the parsed annotations
     * @param high
     *            tag identifiers indicating high priority
     * @param normal
     *            tag identifiers indicating normal priority
     * @param low
     *            tag identifiers indicating low priority
     */
    public TasksResult(final AbstractBuild<?, ?> build, final String defaultEncoding, final TasksParserResult project, final String high, final String normal, final String low) {
        this(build, defaultEncoding, project, project.getNumberOfAnnotations(), high, normal, low);
    }


    /**
     * Creates a new instance of <code>TasksResult</code>.
     *
     * @param build
     *            the current build as owner of this action
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param project
     *            the parsed FindBugs result
     * @param previousNumberOfTasks
     *            the previous number of open tasks
     * @param high
     *            tag identifiers indicating high priority
     * @param normal
     *            tag identifiers indicating normal priority
     * @param low
     *            tag identifiers indicating low priority
     */
    public TasksResult(final AbstractBuild<?, ?> build, final String defaultEncoding, final TasksParserResult project, final int previousNumberOfTasks, final String high, final String normal, final String low) {
        super(build, project.getModules(), project.getErrorMessages(), defaultEncoding);

        highPriorityTasks = project.getNumberOfAnnotations(Priority.HIGH);
        lowPriorityTasks = project.getNumberOfAnnotations(Priority.LOW);
        normalPriorityTasks = project.getNumberOfAnnotations(Priority.NORMAL);

        this.high = high;
        this.normal = normal;
        this.low = low;

        numberOfTasks = project.getNumberOfAnnotations();
        delta = numberOfTasks - previousNumberOfTasks;

        numberOfFiles = project.getNumberOfScannedFiles();

        try {
            Collection<FileAnnotation> files = project.getAnnotations();
            getDataFile().write(files.toArray(new FileAnnotation[files.size()]));
        }
        catch (IOException exception) {
            LOGGER.log(Level.WARNING, "Failed to serialize the open tasks result.", exception);
        }
    }

    /**
     * Returns a summary message for the summary.jelly file.
     *
     * @return the summary message
     */
    public String getSummary() {
        return ResultSummary.createSummary(this);
    }

    /**
     * Returns the number of scanned files in this project.
     *
     * @return the number of scanned files in this project
     */
    public int getNumberOfFiles() {
        return numberOfFiles;
    }

    /**
     * Gets the number of tasks.
     *
     * @return the number of tasks
     */
    public int getNumberOfAnnotations() {
        return numberOfTasks;
    }

    /**
     * Returns the total number of tasks of the specified priority for
     * this object.
     *
     * @param priority
     *            the priority
     * @return total number of tasks of the specified priority for this
     *         object
     */
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

    /**
     * Returns the display name (bread crumb name) of this result.
     *
     * @return the display name of this result.
     */
    public String getDisplayName() {
        return Messages.Tasks_ProjectAction_Name();
    }

    /**
     * Returns the delta between this build and the previous build.
     *
     * @return the delta between this build and the previous build
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
    public synchronized JavaProject getProject() {
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
            FileAnnotation[] annotations = (FileAnnotation[])getDataFile().read();
            newProject.addAnnotations(annotations);
            LOGGER.log(Level.INFO, "Loaded tasks data file " + getDataFile() + " for build " + getOwner().getNumber());
            result = newProject;
        }
        catch (IOException exception) {
            LOGGER.log(Level.WARNING, "Failed to load " + getDataFile(), exception);
            result = new JavaProject();
        }
        project = new WeakReference<JavaProject>(result);
    }

    /** {@inheritDoc} */
    @Override
    protected String getSerializationFileName() {
        return "open-tasks.xml";
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
        return new TasksDetailBuilder().getDynamic(link, getOwner(), getContainer(), getDefaultEncoding(), getDisplayName(),
                getTags(Priority.HIGH), getTags(Priority.NORMAL), getTags(Priority.LOW));
    }

    /**
     * Gets the annotation container.
     *
     * @return the container
     */
    @Override
    public AnnotationContainer getContainer() {
        return getProject();
    }

    /**
     * Returns the module with the specified name.
     *
     * @param name
     *            the module to get
     * @return the module
     */
    public MavenModule getModule(final String name) {
        return getProject().getModule(name);
    }

    /**
     * Returns the actually used priorities.
     *
     * @return the actually used priorities.
     */
    public List<String> getPriorities() {
        List<String> actualPriorities = new ArrayList<String>();
        for (String priority : getAvailablePriorities()) {
            if (getNumberOfAnnotations(priority) > 0) {
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
     * Returns a localized priority name.
     *
     * @param priorityName
     *            priority as String value
     * @return localized priority name
     */
    public String getLocalizedPriority(final String priorityName) {
        return Priority.fromString(priorityName).getLongLocalizedString();
    }

    /**
     * Returns the tags for the specified priority.
     *
     * @param priority the priority
     *
     * @return the tags for the specified priority
     */
    public final String getTags(final String priority) {
        return getTags(Priority.fromString(priority));
    }

    /**
     * Returns the tags for the specified priority.
     *
     * @param priority
     *            the priority
     * @return the tags for the specified priority
     */
    public final String getTags(final Priority priority) {
        if (priority == Priority.HIGH) {
            return high;
        }
        else if (priority == Priority.NORMAL) {
            return normal;
        }
        else {
            return low;
        }
    }

    /**
     * Returns the package category name for the scanned files. Currently, only
     * java and c# files are supported.
     *
     * @return the package category name for the scanned files
     */
    public String getPackageCategoryName() {
        if (hasAnnotations()) {
            String fileName = getAnnotations().iterator().next().getFileName();
            if (fileName.endsWith(".cs")) {
                return hudson.plugins.tasks.util.Messages.NamespaceDetail_header();
            }
        }
        return hudson.plugins.tasks.util.Messages.PackageDetail_header();
    }

    // Delegates to TasksProject
    // CHECKSTYLE:OFF

    /**
     * Returns the files of the project.
     *
     * @return the files of the project
     */
    public Collection<WorkspaceFile> getFiles() {
        return getProject().getFiles();
    }
}