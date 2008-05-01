package hudson.plugins.tasks;

import hudson.FilePath;
import hudson.maven.MavenBuild;
import hudson.maven.MavenBuildProxy;
import hudson.maven.MavenModule;
import hudson.maven.MavenReporter;
import hudson.maven.MavenReporterDescriptor;
import hudson.maven.MojoInfo;
import hudson.maven.MavenBuildProxy.BuildCallable;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.plugins.tasks.parser.TasksProject;
import hudson.plugins.tasks.parser.WorkspaceScanner;
import hudson.plugins.tasks.util.AbortException;
import hudson.plugins.tasks.util.HealthReportBuilder;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.project.MavenProject;

// FIXME: this class more or less is a copy of the TasksPublisher, we should find a way to generalize portions of this class
public class TasksReporter extends MavenReporter {
    /** Default height of the graph. */
    private static final int HEIGHT = 200;
    /** Descriptor of this publisher. */
    public static final TasksReporterDescriptor TASK_SCANNER_DESCRIPTOR = new TasksReporterDescriptor(TasksPublisher.TASK_SCANNER_DESCRIPTOR);
    /** Default files pattern. */
    private static final String DEFAULT_PATTERN = "**/*.java";
    /** Ant file-set pattern of files to scan for open tasks in. */
    private final String pattern;
    /** Tasks threshold to be reached if a build should be considered as unstable. */
    private final String threshold;
    /** Determines whether to use the provided threshold to mark a build as unstable. */
    private boolean isThresholdEnabled;
    /** Integer bug threshold to be reached if a build should be considered as unstable. */
    private int minimumTasks;
    /** Report health as 100% when the number of warnings is less than this value. */
    private final String healthy;
    /** Report health as 0% when the number of warnings is greater than this value. */
    private final String unHealthy;
    /** Report health as 100% when the number of warnings is less than this value. */
    private int healthyTasks;
    /** Report health as 0% when the number of warnings is greater than this value. */
    private int unHealthyTasks;
    /** Determines whether to use the provided healthy thresholds. */
    private boolean isHealthyReportEnabled;
    /** Tag identifiers indicating high priority. */
    private final String high;
    /** Tag identifiers indicating normal priority. */
    private final String normal;
    /** Tag identifiers indicating low priority. */
    private final String low;
    /** Determines the height of the trend graph. */
    private final String height;

    /**
     * Creates a new instance of <code>TaskScannerPublisher</code>.
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
     * @stapler-constructor
     */
    public TasksReporter(final String pattern, final String threshold, final String healthy, final String unHealthy, final String height,
            final String high, final String normal, final String low) {
        super();
        this.threshold = threshold;
        this.healthy = healthy;
        this.unHealthy = unHealthy;
        this.pattern = pattern;
        this.height = height;
        this.high = high;
        this.normal = normal;
        this.low = low;

        if (!StringUtils.isEmpty(threshold)) {
            try {
                minimumTasks = Integer.valueOf(threshold);
                if (minimumTasks >= 0) {
                    isThresholdEnabled = true;
                }
            }
            catch (NumberFormatException exception) {
                // nothing to do, we use the default value
            }
        }
        if (!StringUtils.isEmpty(healthy) && !StringUtils.isEmpty(unHealthy)) {
            try {
                healthyTasks = Integer.valueOf(healthy);
                unHealthyTasks = Integer.valueOf(unHealthy);
                if (healthyTasks >= 0 && unHealthyTasks > healthyTasks) {
                    isHealthyReportEnabled = true;
                }
            }
            catch (NumberFormatException exception) {
                // nothing to do, we use the default value
            }
        }
    }

    /**
     * Returns the Bug threshold to be reached if a build should be considered as unstable.
     *
     * @return the bug threshold
     */
    public String getThreshold() {
        return threshold;
    }

    /**
     * Returns the healthy threshold.
     *
     * @return the healthy
     */
    public String getHealthy() {
        return healthy;
    }

    /**
     * Returns the unhealthy threshold.
     *
     * @return the unHealthy
     */
    public String getUnHealthy() {
        return unHealthy;
    }

    /**
     * Returns the Ant file-set pattern to the workspace files.
     *
     * @return ant file-set pattern to the workspace files.
     */
    public String getPattern() {
        return pattern;
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
    public boolean postExecute(final MavenBuildProxy build, final MavenProject pom, final MojoInfo mojo,
            final BuildListener listener, final Throwable error) throws InterruptedException, IOException {
        if (hasTaskResultAction(build)) {
            return true;
        }

        FilePath filePath = new FilePath(pom.getBasedir());
        final TasksProject project;
        try {
            listener.getLogger().println("Scanning workspace files for tasks...");
            project = filePath.act(new WorkspaceScanner(StringUtils.defaultIfEmpty(pattern, DEFAULT_PATTERN),
                            high, normal, low, pom.getName()));
        }
        catch (AbortException exception) {
            listener.getLogger().println(exception.getMessage());
            build.setResult(Result.FAILURE);
            return true;
        }

        build.execute(new BuildCallable<Void, IOException>() {
            public Void call(final MavenBuild build) throws IOException, InterruptedException {
                TasksResult result = new TasksResultBuilder().build(build, project, high, normal, low);

                HealthReportBuilder healthReportBuilder = new HealthReportBuilder(
                        isThresholdEnabled, minimumTasks, isHealthyReportEnabled, healthyTasks, unHealthyTasks,
                        Messages.Tasks_ResultAction_HealthReportSingleItem(),
                        Messages.Tasks_ResultAction_HealthReportMultipleItem("%d"));
                build.getActions().add(new TasksResultAction(build, result, healthReportBuilder));
                build.registerAsProjectAction(TasksReporter.this);

                return null;
            }
        });

        int warnings = project.getNumberOfAnnotations();
        if (warnings > 0) {
            listener.getLogger().println("A total of " + warnings + " open tasks have been found.");
            if (isThresholdEnabled && warnings >= minimumTasks) {
                build.setResult(Result.UNSTABLE);
            }
        }
        else {
            listener.getLogger().println("No open tasks have been found.");
        }

        return true;
    }

    /**
     * Returns whether we already have a task result for this build.
     *
     * @param build
     *            the current build.
     * @return <code>true</code> if we already have a task result action.
     * @throws IOException
     *             in case of an IO error
     * @throws InterruptedException
     *             if the call has been interrupted
     */
    private Boolean hasTaskResultAction(final MavenBuildProxy build) throws IOException,
            InterruptedException {
        return build.execute(new BuildCallable<Boolean, IOException>() {
            public Boolean call(final MavenBuild mavenBuild) throws IOException, InterruptedException {
                return mavenBuild.getAction(TasksResultAction.class) != null;
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public Action getProjectAction(final MavenModule module) {
        return new TasksProjectAction(module, getTrendHeight());
    }

    /** {@inheritDoc} */
    @Override
    public MavenReporterDescriptor getDescriptor() {
        return TASK_SCANNER_DESCRIPTOR;
    }

    /**
     * Returns the height of the trend graph.
     *
     * @return the height of the trend graph
     */
    public String getHeight() {
        return height;
    }

    /**
     * Returns the height of the trend graph.
     *
     * @return the height of the trend graph
     */
    public int getTrendHeight() {
        if (!StringUtils.isEmpty(height)) {
            try {
                return Math.max(50, Integer.valueOf(height));
            }
            catch (NumberFormatException exception) {
                // nothing to do, we use the default value
            }
        }
        return HEIGHT;
    }
}

