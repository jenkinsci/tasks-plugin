package hudson.plugins.tasks;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import hudson.FilePath;
import hudson.Launcher;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.Action;
import hudson.model.BuildListener;

import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.core.HealthAwarePublisher;
import hudson.plugins.analysis.util.PluginLogger;
import hudson.plugins.tasks.parser.TasksParserResult;
import hudson.plugins.tasks.parser.WorkspaceScanner;

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
    private String high;
    /** Tag identifiers indicating normal priority. */
    private String normal;
    /** Tag identifiers indicating low priority. */
    private String low;
    /** Tag identifiers indicating case sensitivity. */
    private boolean ignoreCase;
    /** If the identifiers should be treated as regular expression. */
    private boolean asRegexp;
    /** Ant file-set pattern of files to work with. */
    private String pattern;
    /** Ant file-set pattern of files to exclude from work. */
    private String excludePattern;
    /** Plugin name */
    private static final String PLUGIN_NAME = "TASKS";

    /**
     * Simplified default constructor. 
     * Use setters to initialize if required.
     */
    @DataBoundConstructor
    public TasksPublisher() {
        super(PLUGIN_NAME);
    }

    /**
     * Returns the Ant file-set pattern of files to work with.
     *
     * @return Ant file-set pattern of files to work with
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * @see {@link #getPattern()}
     */
    @DataBoundSetter
    public void setPattern(String pattern) {
        this.pattern = pattern;
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
     * @see {@link #getExcludePattern()}
     */
    @DataBoundSetter
    public void setExcludePattern(String excludePattern) {
        this.excludePattern = excludePattern;
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
     * @see {@link #getHigh()}
     */
    @DataBoundSetter
    public void setHigh(String high) {
        this.high = high;
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
     * @see {@link #getNormal()}
     */
    @DataBoundSetter
    public void setNormal(String normal) {
        this.normal = normal;
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
     * @see {@link #getLow()}
     */
    @DataBoundSetter
    public void setLow(String low) {
        this.low = low;
    }

    /**
     * Returns whether case should be ignored during the scanning.
     *
     * @return <code>true</code> if case should be ignored during the scanning
     */
    public boolean getIgnoreCase() {
        return ignoreCase;
    }

    /**
     * @see {@link #getIgnoreCase()}
     */
    @DataBoundSetter
    public void setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

    /**
     * Returns whether the identifiers should be treated as regular expression.
     *
     * @return <code>true</code> if the identifiers should be treated as regular expression
     */
    public boolean getAsRegexp() {
        return asRegexp;
    }

    /**
     * @see {@link #getAsRegexp()}
     */
    @DataBoundSetter
    public void setAsRegexp(boolean asRegexp) {
        this.asRegexp = asRegexp;
    }

    @Override
    public Action getProjectAction(final AbstractProject<?, ?> project) {
        return new TasksProjectAction(project);
    }

    @Override
    protected BuildResult perform(final Run<?, ?> build, FilePath workspace, final PluginLogger logger) throws InterruptedException, IOException {
        TasksParserResult project;
        WorkspaceScanner scanner = new WorkspaceScanner(StringUtils.defaultIfEmpty(getPattern(), DEFAULT_PATTERN),
                getExcludePattern(), getDefaultEncoding(), high, normal, low, ignoreCase, shouldDetectModules(), asRegexp);
        project = workspace.act(scanner);

        logger.logLines(project.getLogMessages());
        logger.log(String.format("Found %d open tasks.", project.getNumberOfAnnotations()));

        TasksResult result = new TasksResult(build, getDefaultEncoding(), project,
                usePreviousBuildAsReference(), useOnlyStableBuildsAsReference(), high, normal, low);
        build.addAction(new TasksResultAction(build, this, result));

        return result;
    }

    @Override
    public TasksDescriptor getDescriptor() {
        return (TasksDescriptor)super.getDescriptor();
    }

    @Override
    public MatrixAggregator createAggregator(final MatrixBuild build, final Launcher launcher,
            final BuildListener listener) {
        return new TasksAnnotationsAggregator(build, launcher, listener, this, getDefaultEncoding(),
                usePreviousBuildAsReference(), useOnlyStableBuildsAsReference());
    }

    /**
     * Creates a new instance of <code>TasksPublisher</code>.
     *
     * @param healthy
     *            Report health as 100% when the number of open tasks is less
     *            than this value
     * @param unHealthy
     *            Report health as 0% when the number of open tasks is greater
     *            than this value
     * @param thresholdLimit
     *            determines which warning priorities should be considered when
     *            evaluating the build stability and health
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
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
     * @param usePreviousBuildAsReference
     *            determines whether to always use the previous build as the reference build
     * @param useStableBuildAsReference
     *            determines whether only stable builds should be used as reference builds or not
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
     * @param asRegexp
     *            if the identifiers should be treated as regular expression
     * @param pattern
     *            Ant file-set pattern of files to scan for open tasks in
     * @param excludePattern
     *            Ant file-set pattern of files to exclude from scan
     * @deprecated This constructor is called internally only, but if you need to use it (for some strange reason), call
     *            {@link #TasksPublisher()} and available setters
     */
    // CHECKSTYLE:OFF
    @SuppressWarnings("PMD.ExcessiveParameterList")
    @Deprecated
    public TasksPublisher(final String healthy, final String unHealthy, final String thresholdLimit,
            final String defaultEncoding, final boolean useDeltaValues,
            final String unstableTotalAll, final String unstableTotalHigh, final String unstableTotalNormal, final String unstableTotalLow,
            final String unstableNewAll, final String unstableNewHigh, final String unstableNewNormal, final String unstableNewLow,
            final String failedTotalAll, final String failedTotalHigh, final String failedTotalNormal, final String failedTotalLow,
            final String failedNewAll, final String failedNewHigh, final String failedNewNormal, final String failedNewLow,
            final boolean canRunOnFailed, final boolean usePreviousBuildAsReference, final boolean useStableBuildAsReference,
            final boolean shouldDetectModules, final boolean canComputeNew, final String high, final String normal, final String low,
            final boolean ignoreCase, final boolean asRegexp, final String pattern, final String excludePattern) {
        super(healthy, unHealthy, thresholdLimit, defaultEncoding, useDeltaValues,
                unstableTotalAll, unstableTotalHigh, unstableTotalNormal, unstableTotalLow,
                unstableNewAll, unstableNewHigh, unstableNewNormal, unstableNewLow,
                failedTotalAll, failedTotalHigh, failedTotalNormal, failedTotalLow,
                failedNewAll, failedNewHigh, failedNewNormal, failedNewLow,
                canRunOnFailed, usePreviousBuildAsReference, useStableBuildAsReference,
                shouldDetectModules, canComputeNew, true, PLUGIN_NAME);
        this.pattern = pattern;
        this.excludePattern = excludePattern;
        this.high = high;
        this.normal = normal;
        this.low = low;
        this.ignoreCase = ignoreCase;
        this.asRegexp = asRegexp;
    }
    // CHECKSTYLE:ON
}
