package hudson.plugins.tasks;

import hudson.Launcher;
import hudson.model.Action;
import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Project;
import hudson.model.Result;
import hudson.plugins.util.AbortException;
import hudson.tasks.Publisher;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;

/**
 * Publishes the results of the task scanner.
 *
 * @author Ulli Hafner
 */
public class TasksPublisher extends Publisher {
    /** Default files pattern. */
    private static final String DEFAULT_PATTERN = "**/*.java";
    /** Descriptor of this publisher. */
    public static final TasksDescriptor TASK_SCANNER_DESCRIPTOR = new TasksDescriptor();
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
     * @param high
     *            tag identifiers indicating high priority
     * @param normal
     *            tag identifiers indicating normal priority
     * @param low
     *            tag identifiers indicating low priority
     * @stapler-constructor
     */
    public TasksPublisher(final String pattern, final String threshold,
            final String healthy, final String unHealthy,
            final String high, final String normal, final String low) {
        super();
        this.threshold = threshold;
        this.healthy = healthy;
        this.unHealthy = unHealthy;
        this.pattern = pattern;
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

    /** {@inheritDoc} */
    @Override
    public Action getProjectAction(final Project project) {
        return new TasksProjectAction(project);
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

    /**
     * Scans the workspace, collects all data files and copies these files to
     * the build results folder. Then counts the number of bugs and sets the
     * result of the build accordingly ({@link #threshold}.
     *
     * @param build
     *            the build
     * @param launcher
     *            the launcher
     * @param listener
     *            the build listener
     * @return true in case the processing has been aborted
     * @throws IOException
     *             if the files could not be copied
     * @throws InterruptedException
     *             if user cancels the operation
     */
    public boolean perform(final Build<?, ?> build, final Launcher launcher, final BuildListener listener) throws InterruptedException, IOException {

        JavaProject project;
        try {
            listener.getLogger().println("Scanning workspace files for tasks...");
            project = build.getProject().getWorkspace().act(
                    new WorkspaceScanner(StringUtils.defaultIfEmpty(pattern, DEFAULT_PATTERN),
                            high, normal, low));
        }
        catch (AbortException exception) {
            listener.getLogger().println(exception.getMessage());
            build.setResult(Result.FAILURE);
            return true;
        }

        Object previous = build.getPreviousBuild();
        TasksResult result;
        if (previous instanceof Build<?, ?>) {
            Build<?, ?> previousBuild = (Build<?, ?>)previous;
            TasksResultAction previousAction = previousBuild.getAction(TasksResultAction.class);
            if (previousAction == null) {
                result = new TasksResult(build, project);
            }
            else {
                result = new TasksResult(build, project, previousAction.getResult().getNumberOfTasks());
            }
        }
        else {
            result = new TasksResult(build, project);
        }

        build.getActions().add(new TasksResultAction(build, result, isHealthyReportEnabled, healthyTasks, unHealthyTasks));

        int warnings = project.getNumberOfTasks();
        if (warnings > 0) {
            listener.getLogger().println("A total of " + warnings + " open tasks have been found.");
            if (isThresholdEnabled && warnings >= minimumTasks) {
                build.setResult(Result.UNSTABLE);
            }
        }
        else {
            listener.getLogger().println("No open tasks have been found.");
        }

        return false;
    }

    /** {@inheritDoc} */
    public Descriptor<Publisher> getDescriptor() {
        return TASK_SCANNER_DESCRIPTOR;
    }
}
