package hudson.plugins.tasks;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Result;
import hudson.plugins.tasks.parser.WorkspaceScanner;
import hudson.plugins.tasks.util.AbortException;
import hudson.plugins.tasks.util.HealthAwarePublisher;
import hudson.plugins.tasks.util.HealthReportBuilder;
import hudson.plugins.tasks.util.model.JavaProject;
import hudson.tasks.Publisher;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;

/**
 * Publishes the results of the task scanner.
 *
 * @author Ulli Hafner
 */
public class TasksPublisher extends HealthAwarePublisher {
    /** Default files pattern. */
    private static final String DEFAULT_PATTERN = "**/*.java";
    /** Descriptor of this publisher. */
    public static final TasksDescriptor TASK_SCANNER_DESCRIPTOR = new TasksDescriptor();
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
        super(pattern, threshold, healthy, unHealthy);

        this.high = high;
        this.normal = normal;
        this.low = low;
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
    public Action getProjectAction(final AbstractProject<?, ?> project) {
        return new TasksProjectAction(project);
    }

    /**
     * Scans the workspace, collects all data files and copies these files to
     * the build results folder. Then counts the number of bugs and sets the
     * result of the build accordingly ({@link #getThreshold()}.
     *
     * @param build
     *            the build
     * @param launcher
     *            the launcher
     * @param listener
     *            the build listener
     * @return <code>true</code> if the build could continue
     * @throws IOException
     *             if the files could not be copied
     * @throws InterruptedException
     *             if user cancels the operation
     */
    @Override
    public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener) throws InterruptedException, IOException {
        JavaProject project;
        try {
            listener.getLogger().println("Scanning workspace files for tasks...");
            project = build.getProject().getWorkspace().act(
                    new WorkspaceScanner(StringUtils.defaultIfEmpty(getPattern(), DEFAULT_PATTERN), high, normal, low));
        }
        catch (AbortException exception) {
            listener.getLogger().println(exception.getMessage());
            build.setResult(Result.FAILURE);
            return false;
        }

        Object previous = build.getPreviousBuild();
        TasksResult result;
        if (previous instanceof AbstractBuild<?, ?>) {
            AbstractBuild<?, ?> previousBuild = (AbstractBuild<?, ?>)previous;
            TasksResultAction previousAction = previousBuild.getAction(TasksResultAction.class);
            if (previousAction == null) {
                result = new TasksResult(build, project, high, normal, low);
            }
            else {
                result = new TasksResult(build, project, previousAction.getResult().getNumberOfAnnotations(), high, normal, low);
            }
        }
        else {
            result = new TasksResult(build, project, high, normal, low);
        }

        HealthReportBuilder healthReportBuilder = createHealthReporter("Task Scanner", "open task");
        build.getActions().add(new TasksResultAction(build, result, healthReportBuilder));

        int warnings = project.getNumberOfAnnotations();
        if (warnings > 0) {
            listener.getLogger().println("A total of " + warnings + " open tasks have been found.");
            if (isThresholdEnabled() && warnings >= getMinimumAnnotations()) {
                build.setResult(Result.UNSTABLE);
            }
        }
        else {
            listener.getLogger().println("No open tasks have been found.");
        }

        return true;
    }

    /** {@inheritDoc} */
    public Descriptor<Publisher> getDescriptor() {
        return TASK_SCANNER_DESCRIPTOR;
    }
}
