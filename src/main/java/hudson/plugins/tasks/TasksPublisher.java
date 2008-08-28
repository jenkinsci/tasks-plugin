package hudson.plugins.tasks;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Descriptor;
import hudson.plugins.tasks.parser.TasksParserResult;
import hudson.plugins.tasks.parser.WorkspaceScanner;
import hudson.plugins.tasks.util.HealthAwarePublisher;
import hudson.plugins.tasks.util.HealthReportBuilder;
import hudson.tasks.Publisher;

import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Publishes the results of the task scanner (freestyle project type).
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
    /** Ant file-set pattern of files to work with. */
    private final String pattern;
    /** Ant file-set pattern of files to exclude from work. */
    private final String excludePattern;

    /**
     * Creates a new instance of <code>TasksPublisher</code>.
     *
     * @param pattern
     *            Ant file-set pattern of files to scan for open tasks in
     * @param excludePattern
     *            Ant file-set pattern of files to exclude from scan
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
     * @param thresholdLimit
     *            determines which warning priorities should be considered when
     *            evaluating the build stability and health
     * @param high
     *            tag identifiers indicating high priority
     * @param normal
     *            tag identifiers indicating normal priority
     * @param low
     *            tag identifiers indicating low priority
     */
    // CHECKSTYLE:OFF
    @DataBoundConstructor
    public TasksPublisher(final String pattern, final String excludePattern, final String threshold,
            final String healthy, final String unHealthy, final String height, final String thresholdLimit,
            final String high, final String normal, final String low) {
        super(threshold, healthy, unHealthy, height, thresholdLimit, "TASKS");

        this.pattern = pattern;
        this.excludePattern = excludePattern;
        this.high = high;
        this.normal = normal;
        this.low = low;
    }
    // CHECKSTYLE:ON

    /**
     * Returns the Ant file-set pattern of files to work with.
     *
     * @return Ant file-set pattern of files to work with
     */
    public String getPattern() {
        return pattern;
    }

     /**
     * Returns the Ant file-set pattern of files to exclude from work.
     *
     * @return Ant file-set pattern of files to exclude from work
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
    public Action getProjectAction(final AbstractProject<?, ?> project) {
        return new TasksProjectAction(project, getTrendHeight());
    }

    /** {@inheritDoc} */
    @Override
    public TasksParserResult perform(final AbstractBuild<?, ?> build, final PrintStream logger) throws InterruptedException, IOException {
        TasksParserResult project;
        log(logger, "Scanning workspace files for tasks...");
        project = build.getProject().getWorkspace().act(
                new WorkspaceScanner(StringUtils.defaultIfEmpty(getPattern(), DEFAULT_PATTERN), getExcludePattern(), high, normal, low));

        TasksResult result = new TasksResultBuilder().build(build, project, high, normal, low);
        HealthReportBuilder healthReportBuilder = createHealthReporter(
                Messages.Tasks_ResultAction_HealthReportSingleItem(),
                Messages.Tasks_ResultAction_HealthReportMultipleItem());
        build.getActions().add(new TasksResultAction(build, healthReportBuilder, result));

        return project;
    }

    /** {@inheritDoc} */
    public Descriptor<Publisher> getDescriptor() {
        return TASK_SCANNER_DESCRIPTOR;
    }
}
