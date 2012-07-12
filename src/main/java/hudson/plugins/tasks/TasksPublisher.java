package hudson.plugins.tasks;

import hudson.Launcher;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.plugins.analysis.core.HealthAwarePublisher;
import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.util.PluginLogger;
import hudson.plugins.tasks.parser.TasksParserResult;
import hudson.plugins.tasks.parser.WorkspaceScanner;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Publishes the results of the task scanner (freestyle project type).
 *
 * @author Ulli Hafner
 */
public class TasksPublisher extends HealthAwarePublisher {
    /** Unique ID of this class. */
    private static final long serialVersionUID = 3787892530045641806L;

    /** Default files pattern. */
    private static final String DEFAULT_PATTERN = "**/*.java";
    /** Tag identifiers indicating high priority. */
    private final String high;
    /** Tag identifiers indicating normal priority. */
    private final String normal;
    /** Tag identifiers indicating low priority. */
    private final String low;
    /** Tag identifiers indicating case sensitivity. */
    private final boolean ignoreCase;
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
     * @param healthy
     *            Report health as 100% when the number of open tasks is less
     *            than this value
     * @param unHealthy
     *            Report health as 0% when the number of open tasks is greater
     *            than this value
     * @param thresholdLimit
     *            determines which warning priorities should be considered when
     *            evaluating the build stability and health
     * @param useDeltaValues
     *            determines whether the absolute annotations delta or the
     *            actual annotations set difference should be used to evaluate
     *            the build stability
     * @param unstableTotalAll
     *            annotation threshold
     * @param unstableTotalHigh
     *            annotation threshold
     * @param unstableTotalNormal
     *            annotation threshold
     * @param unstableTotalLow
     *            annotation threshold
     * @param unstableNewAll
     *            annotation threshold
     * @param unstableNewHigh
     *            annotation threshold
     * @param unstableNewNormal
     *            annotation threshold
     * @param unstableNewLow
     *            annotation threshold
     * @param failedTotalAll
     *            annotation threshold
     * @param failedTotalHigh
     *            annotation threshold
     * @param failedTotalNormal
     *            annotation threshold
     * @param failedTotalLow
     *            annotation threshold
     * @param failedNewAll
     *            annotation threshold
     * @param failedNewHigh
     *            annotation threshold
     * @param failedNewNormal
     *            annotation threshold
     * @param failedNewLow
     *            annotation threshold
     * @param canRunOnFailed
     *            determines whether the plug-in can run for failed builds, too
     * @param canComputeNew
     *            determines whether new warnings should be computed (with
     *            respect to baseline)
     * @param shouldDetectModules
     *            determines whether module names should be derived from Maven POM or Ant build files
     * @param high
     *            tag identifiers indicating high priority
     * @param normal
     *            tag identifiers indicating normal priority
     * @param low
     *            tag identifiers indicating low priority
     * @param ignoreCase
     *            if case should be ignored during matching
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     */
    // CHECKSTYLE:OFF
    @SuppressWarnings("PMD.ExcessiveParameterList")
    @DataBoundConstructor
    public TasksPublisher(final String healthy, final String unHealthy, final String thresholdLimit,
            final String defaultEncoding, final boolean useDeltaValues,
            final String unstableTotalAll, final String unstableTotalHigh, final String unstableTotalNormal, final String unstableTotalLow,
            final String unstableNewAll, final String unstableNewHigh, final String unstableNewNormal, final String unstableNewLow,
            final String failedTotalAll, final String failedTotalHigh, final String failedTotalNormal, final String failedTotalLow,
            final String failedNewAll, final String failedNewHigh, final String failedNewNormal, final String failedNewLow,
            final boolean canRunOnFailed, final boolean shouldDetectModules, final boolean canComputeNew,
            final String high, final String normal, final String low, final boolean ignoreCase,
            final String pattern, final String excludePattern) {
        super(healthy, unHealthy, thresholdLimit, defaultEncoding, useDeltaValues,
                unstableTotalAll, unstableTotalHigh, unstableTotalNormal, unstableTotalLow,
                unstableNewAll, unstableNewHigh, unstableNewNormal, unstableNewLow,
                failedTotalAll, failedTotalHigh, failedTotalNormal, failedTotalLow,
                failedNewAll, failedNewHigh, failedNewNormal, failedNewLow,
                canRunOnFailed, shouldDetectModules, canComputeNew, "TASKS");
        this.pattern = pattern;
        this.excludePattern = excludePattern;
        this.high = high;
        this.normal = normal;
        this.low = low;
        this.ignoreCase = ignoreCase;
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

    /**
     * Returns whether case should be ignored during the scanning.
     *
     * @return <code>true</code> if case should be ignored during the scanning
     */
    public boolean getIgnoreCase() {
        return ignoreCase;
    }

    @Override
    public Action getProjectAction(final AbstractProject<?, ?> project) {
        return new TasksProjectAction(project);
    }

    @Override
    protected BuildResult perform(final AbstractBuild<?, ?> build, final PluginLogger logger) throws InterruptedException, IOException {
        TasksParserResult project;
        WorkspaceScanner scanner = new WorkspaceScanner(StringUtils.defaultIfEmpty(getPattern(), DEFAULT_PATTERN),
                getExcludePattern(), getDefaultEncoding(), high, normal, low, ignoreCase, shouldDetectModules());
        project = build.getWorkspace().act(scanner);

        logger.logLines(project.getLogMessages());
        logger.log(String.format("Found %d open tasks.", project.getNumberOfAnnotations()));

        TasksResult result = new TasksResult(build, getDefaultEncoding(), project, high, normal, low);
        build.getActions().add(new TasksResultAction(build, this, result));

        return result;
    }

    @Override
    public TasksDescriptor getDescriptor() {
        return (TasksDescriptor)super.getDescriptor();
    }

    /** {@inheritDoc} */
    public MatrixAggregator createAggregator(final MatrixBuild build, final Launcher launcher,
            final BuildListener listener) {
        return new TasksAnnotationsAggregator(build, launcher, listener, this, getDefaultEncoding());
    }
}
