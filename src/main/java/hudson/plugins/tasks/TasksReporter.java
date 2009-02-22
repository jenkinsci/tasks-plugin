package hudson.plugins.tasks;

import hudson.FilePath;
import hudson.maven.MavenBuild;
import hudson.maven.MavenBuildProxy;
import hudson.maven.MavenModule;
import hudson.maven.MavenReporterDescriptor;
import hudson.maven.MojoInfo;
import hudson.model.Action;
import hudson.plugins.tasks.parser.TasksParserResult;
import hudson.plugins.tasks.parser.WorkspaceScanner;
import hudson.plugins.tasks.util.BuildResult;
import hudson.plugins.tasks.util.HealthAwareMavenReporter;
import hudson.plugins.tasks.util.ParserResult;
import hudson.plugins.tasks.util.PluginLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.model.Resource;
import org.apache.maven.project.MavenProject;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Publishes the results of the task scanner (maven 2 project type).
 *
 * @author Ulli Hafner
 */
// CHECKSTYLE:COUPLING-OFF
public class TasksReporter extends HealthAwareMavenReporter {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -4159947472293502606L;
    /** Descriptor of this publisher. */
    public static final TasksReporterDescriptor TASK_SCANNER_DESCRIPTOR = new TasksReporterDescriptor(TasksPublisher.TASK_SCANNER_DESCRIPTOR);
    /** Default files pattern. */
    private static final String DEFAULT_PATTERN = "**/*.java";
    /** Ant file-set pattern of files to scan for open tasks in. */
    private final String pattern;
    /** Ant file-set pattern of files to exclude from scan. */
    private final String excludePattern;
    /** Tag identifiers indicating high priority. */
    private final String high;
    /** Tag identifiers indicating normal priority. */
    private final String normal;
    /** Tag identifiers indicating low priority. */
    private final String low;
    /** Tag identifiers indicating case sensitivity. */
    private final boolean ignoreCase;

    /**
     * Creates a new instance of <code>TasksReporter</code>.
     *
     * @param pattern
     *            Ant file-set pattern of files to scan for open tasks in
     * @param excludePattern
     *            Ant file-set pattern of files to exclude from scan
     * @param threshold
     *            Annotation threshold to be reached if a build should be considered as
     *            unstable.
     * @param newThreshold
     *            New annotations threshold to be reached if a build should be
     *            considered as unstable.
     * @param failureThreshold
     *            Annotation threshold to be reached if a build should be considered as
     *            failure.
     * @param newFailureThreshold
     *            New annotations threshold to be reached if a build should be
     *            considered as failure.
     * @param healthy
     *            Report health as 100% when the number of open tasks is less
     *            than this value
     * @param unHealthy
     *            Report health as 0% when the number of open tasks is greater
     *            than this value
     * @param height
     *            the height of the trend graph
     * @param thresholdLimit
     *            determines which warning priorities should be considered when
     *            evaluating the build stability and health
     * @param high
     *            tag identifiers indicating high priority
     * @param normal
     *            tag identifiers indicating normal priority
     * @param low
     *            tag identifiers indicating low priority
     * @param ignoreCase
     *            if case should be ignored during matching
     */
    // CHECKSTYLE:OFF
    @SuppressWarnings("PMD.ExcessiveParameterList")
    @DataBoundConstructor
    public TasksReporter(final String pattern, final String excludePattern,
            final String threshold, final String newThreshold,
            final String failureThreshold, final String newFailureThreshold,
            final String healthy, final String unHealthy,
            final String height, final String thresholdLimit,
            final String high, final String normal, final String low,
            final boolean ignoreCase) {
        super(threshold, newThreshold, failureThreshold, newFailureThreshold, healthy, unHealthy, height, thresholdLimit, "TASKS");
        this.pattern = pattern;
        this.excludePattern = excludePattern;
        this.high = high;
        this.normal = normal;
        this.low = low;
        this.ignoreCase = ignoreCase;
    }
    // CHECKSTYLE:ON

    /**
     * Returns the Ant file-set pattern to the workspace files.
     *
     * @return ant file-set pattern to the workspace files.
     */
    public String getPattern() {
        return pattern;
    }

     /**
     * Returns the Ant file-set pattern of files to exclude from work.
     *
     * @return Ant file-set pattern of files to exclude from work.
     */
    public String getExcludePattern() {
        return excludePattern;
    }

    /**
     * Returns the high priority task identifiers.
     *
     * @return the high priority task identifiers
     */
    public String getHigh() {
        return high;
    }

    /**
     * Returns the normal priority task identifiers.
     *
     * @return the normal priority task identifiers
     */
    public String getNormal() {
        return normal;
    }

    /**
     * Returns the low priority task identifiers.
     *
     * @return the low priority task identifiers
     */
    public String getLow() {
        return low;
    }

    /**
     * Returns whether case should be ignored during the scanning.
     *
     * @return <code>true</code> if case should be ignored during the scanning
     */
    public boolean getIgnoreCase() {
        return ignoreCase;
    }

    /** {@inheritDoc} */
    @Override
    protected boolean acceptGoal(final String goal) {
        return true;
    }

    /** {@inheritDoc} */
    @SuppressWarnings({"unchecked", "PMD.AvoidFinalLocalVariable"})
    @Override
    public TasksParserResult perform(final MavenBuildProxy build, final MavenProject pom, final MojoInfo mojo, final PluginLogger logger) throws InterruptedException, IOException {
        List<String> foldersToScan = new ArrayList<String>(pom.getCompileSourceRoots());
        List<Resource> resources = pom.getResources();
        for (Resource resource : resources) {
            foldersToScan.add(resource.getDirectory());
        }
        FilePath basedir = new FilePath(pom.getBasedir());
        final TasksParserResult project = new TasksParserResult();
        for (String sourcePath : foldersToScan) {
            if (StringUtils.isEmpty(sourcePath)) {
                continue;
            }
            FilePath filePath = new FilePath(basedir, sourcePath);
            if (filePath.exists()) {
                logger.log(String.format("Scanning folder '%s' for tasks ... ", sourcePath));
                WorkspaceScanner workspaceScanner = new WorkspaceScanner(StringUtils.defaultIfEmpty(pattern, DEFAULT_PATTERN),
                        excludePattern, getDefaultEncoding(), high, normal, low, ignoreCase, pom.getName());
                workspaceScanner.setPrefix(sourcePath);
                TasksParserResult subProject = filePath.act(workspaceScanner);
                project.addAnnotations(subProject.getAnnotations());
                project.addModule(pom.getName());
                project.addScannedFiles(subProject.getNumberOfScannedFiles());
                logger.log(String.format("Found %d.", subProject.getNumberOfAnnotations()));
            }
            else {
                logger.log(String.format("Scipping non-existent folder '%s'...", sourcePath));
            }
        }

        return project;
    }

    /** {@inheritDoc} */
    @Override
    protected BuildResult persistResult(final ParserResult project, final MavenBuild build) {
        TasksResult result = new TasksResultBuilder().build(build, (TasksParserResult)project, getDefaultEncoding(),
                high, normal, low);

        build.getActions().add(new MavenTasksResultAction(build, this, getHeight(), getDefaultEncoding(), high, normal, low, result));
        build.registerAsProjectAction(TasksReporter.this);

        return result;
    }

    /** {@inheritDoc} */
    @Override
    public Action getProjectAction(final MavenModule module) {
        return new TasksProjectAction(module, getTrendHeight());
    }

    /** {@inheritDoc} */
    @Override
    protected Class<? extends Action> getResultActionClass() {
        return MavenTasksResultAction.class;
    }

    /** {@inheritDoc} */
    @Override
    public MavenReporterDescriptor getDescriptor() {
        return TASK_SCANNER_DESCRIPTOR;
    }

    // Backward compatibility. Do not remove.
    // CHECKSTYLE:OFF
    @SuppressWarnings("all")
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("")
    @Deprecated
    private transient boolean isThresholdEnabled;
    @SuppressWarnings("all")
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("")
    @Deprecated
    private transient boolean isHealthyReportEnabled;
    @SuppressWarnings("all")
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("")
    @Deprecated
    private transient int healthTasks;
    @SuppressWarnings("all")
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("")
    @Deprecated
    private transient int unHealthyTasks;
    @SuppressWarnings("all")
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("")
    @Deprecated
    private transient int minimumTasks;
    @SuppressWarnings("all")
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("")
    @Deprecated
    private transient String height;
}

