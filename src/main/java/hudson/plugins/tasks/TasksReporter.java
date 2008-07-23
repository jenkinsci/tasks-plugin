package hudson.plugins.tasks;

import hudson.FilePath;
import hudson.maven.MavenBuild;
import hudson.maven.MavenBuildProxy;
import hudson.maven.MavenModule;
import hudson.maven.MavenReporterDescriptor;
import hudson.maven.MojoInfo;
import hudson.model.Action;
import hudson.plugins.tasks.parser.TasksProject;
import hudson.plugins.tasks.parser.WorkspaceScanner;
import hudson.plugins.tasks.util.HealthAwareMavenReporter;
import hudson.plugins.tasks.util.HealthReportBuilder;
import hudson.plugins.tasks.util.model.JavaProject;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.model.Resource;
import org.apache.maven.project.MavenProject;
import org.kohsuke.stapler.DataBoundConstructor;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

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
    /** Determines the height of the trend graph. */
    private final String height;
    /** Determines whether to use the provided threshold to mark a build as unstable. */
    @SuppressWarnings("unused") // NOPMD
    private boolean isThresholdEnabled; // backward compatibility
    /** Determines whether to use the provided healthy thresholds. */
    @SuppressWarnings("unused") // NOPMD
    private boolean isHealthyReportEnabled; // backward compatibility
    /** Report health as 100% when the number of warnings is less than this value. */
    @SuppressWarnings("unused") // NOPMD
    private int healthyTasks; // backward compatibility
    /** Report health as 0% when the number of warnings is greater than this value. */
    @SuppressWarnings("unused") // NOPMD
    private int unHealthyTasks; // backward compatibility
    /** Integer bug threshold to be reached if a build should be considered as unstable. */
    @SuppressWarnings("unused") // NOPMD
    private int minimumTasks; // backward compatibility

    /**
     * Creates a new instance of <code>TasksReporter</code>.
     *
     * @param pattern
     *            Ant file-set pattern of files to scan for open tasks in
     * @param threshold
     *            Tasks threshold to be reached if a build should be considered
     *            as unstable.
     * @param healthy
     *            Report health as 100% when the number of open tasks is less
     *            than this value
     * @param unHealthy
     *            Report health as 0% when the number of open tasks is greater
     *            than this value
     * @param height
     *            the height of the trend graph
     * @param high
     *            tag identifiers indicating high priority
     * @param normal
     *            tag identifiers indicating normal priority
     * @param low
     *            tag identifiers indicating low priority
     */
    // CHECKSTYLE:OFF
    @DataBoundConstructor
    public TasksReporter(final String pattern, final String excludePattern, final String threshold, final String healthy, final String unHealthy, final String height,
            final String high, final String normal, final String low) {
        super(threshold, healthy, unHealthy, pattern, "TASKS");
        this.pattern = pattern;
        this.excludePattern = excludePattern;
        this.height = height;
        this.high = high;
        this.normal = normal;
        this.low = low;
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

    /** {@inheritDoc} */
    @Override
    protected boolean acceptGoal(final String goal) {
        return true;
    }

    /** {@inheritDoc} */
    @java.lang.SuppressWarnings("unchecked")
    @Override
    public JavaProject perform(final MavenBuildProxy build, final MavenProject pom, final MojoInfo mojo, final PrintStream logger) throws InterruptedException, IOException {

        List<String> foldersToScan = new ArrayList<String>(pom.getCompileSourceRoots());
        List<Resource> resources = pom.getResources();
        for (Resource resource : resources) {
            foldersToScan.add(resource.getDirectory());
        }
        FilePath basedir = new FilePath(pom.getBasedir());
        final TasksProject project = new TasksProject();
        for (String sourcePath : foldersToScan) {
            if (StringUtils.isEmpty(sourcePath)) {
                continue;
            }
            FilePath filePath = new FilePath(basedir, sourcePath);
            if (filePath.exists()) {
                log(logger, String.format("Scanning folder '%s' for tasks ... ", sourcePath));
                WorkspaceScanner workspaceScanner = new WorkspaceScanner(
                        StringUtils.defaultIfEmpty(pattern, DEFAULT_PATTERN), excludePattern,
                        high, normal, low, pom.getName());
                workspaceScanner.setPrefix(sourcePath);
                TasksProject subProject = filePath.act(workspaceScanner);
                if (subProject.hasAnnotations()) {
                    project.addModules(subProject.getModules());
                }
                project.addScannedFiles(subProject.getNumberOfScannedFiles());
                log(logger, String.format("Found %d.", subProject.getNumberOfAnnotations()));
            }
            else {
                log(logger, String.format("Scipping non-existent folder '%s'...", sourcePath));
            }
        }

        return project;
    }

    /** {@inheritDoc} */
    @Override
    protected void persistResult(final JavaProject project, final MavenBuild build) {
        if (project instanceof TasksProject) {
            TasksResult result = new TasksResultBuilder().build(build, (TasksProject)project, high, normal, low);

            HealthReportBuilder healthReportBuilder = createHealthBuilder(
                    Messages.Tasks_ResultAction_HealthReportSingleItem(),
                    Messages.Tasks_ResultAction_HealthReportMultipleItem("%d"));
            build.getActions().add(new MavenTasksResultAction(build, healthReportBuilder, height, high, normal, low, result));
            build.registerAsProjectAction(TasksReporter.this);
        }
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
}

