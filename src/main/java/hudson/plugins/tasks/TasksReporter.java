package hudson.plugins.tasks;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.project.MavenProject;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.FilePath;
import hudson.maven.MavenAggregatedReport;
import hudson.maven.MavenBuild;
import hudson.maven.MavenBuildProxy;
import hudson.maven.MavenModule;
import hudson.maven.MojoInfo;
import hudson.plugins.analysis.core.HealthAwareReporter;
import hudson.plugins.analysis.core.ParserResult;
import hudson.plugins.analysis.util.PluginLogger;
import hudson.plugins.tasks.parser.TasksParserResult;
import hudson.plugins.tasks.parser.WorkspaceScanner;

/**
 * Publishes the results of the task scanner (maven 2 project type).
 *
 * @author Ulli Hafner
 */
// CHECKSTYLE:COUPLING-OFF
public class TasksReporter extends HealthAwareReporter<TasksResult> {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -4159947472293502606L;

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
    /** If the identifiers should be treated as regular expression. */
    private final boolean asRegexp;

    /**
     * Creates a new instance of <code>TasksReporter</code>.
     *
     * @param pattern
     *            Ant file-set pattern of files to scan for open tasks in
     * @param excludePattern
     *            Ant file-set pattern of files to exclude from scan
     * @param healthy
     *            Report health as 100% when the number of warnings is less than
     *            this value
     * @param unHealthy
     *            Report health as 0% when the number of warnings is greater
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
     * @param canRunOnFailed
     *            determines whether the plug-in can run for failed builds, too
     * @param useStableBuildAsReference
     *            determines whether only stable builds should be used as reference builds or not
     * @param canComputeNew
     *            determines whether new warnings should be computed (with
     *            respect to baseline)
     */
    // CHECKSTYLE:OFF
    @SuppressWarnings("PMD.ExcessiveParameterList")
    @DataBoundConstructor
    public TasksReporter(final String pattern, final String excludePattern,
            final String healthy, final String unHealthy, final String thresholdLimit, final boolean useDeltaValues,
            final String unstableTotalAll, final String unstableTotalHigh, final String unstableTotalNormal, final String unstableTotalLow,
            final String unstableNewAll, final String unstableNewHigh, final String unstableNewNormal, final String unstableNewLow,
            final String failedTotalAll, final String failedTotalHigh, final String failedTotalNormal, final String failedTotalLow,
            final String failedNewAll, final String failedNewHigh, final String failedNewNormal, final String failedNewLow,
            final String high, final String normal, final String low,
            final boolean ignoreCase, final boolean asRegexp, final boolean canRunOnFailed,
            final boolean useStableBuildAsReference, final boolean canComputeNew) {
        super(healthy, unHealthy, thresholdLimit, useDeltaValues,
                unstableTotalAll, unstableTotalHigh, unstableTotalNormal, unstableTotalLow,
                unstableNewAll, unstableNewHigh, unstableNewNormal, unstableNewLow,
                failedTotalAll, failedTotalHigh, failedTotalNormal, failedTotalLow,
                failedNewAll, failedNewHigh, failedNewNormal, failedNewLow,
                canRunOnFailed, useStableBuildAsReference, canComputeNew, "TASKS");
        this.pattern = pattern;
        this.excludePattern = excludePattern;
        this.high = high;
        this.normal = normal;
        this.low = low;
        this.ignoreCase = ignoreCase;
        this.asRegexp = asRegexp;
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

    /**
     * Returns whether the identifiers should be treated as regular expression.
     *
     * @return <code>true</code> if the identifiers should be treated as regular expression
     */
    public boolean getAsRegexp() {
        return asRegexp;
    }

    @Override
    protected boolean acceptGoal(final String goal) {
        return true;
    }

    @SuppressWarnings("PMD.AvoidFinalLocalVariable")
    @Override
    public TasksParserResult perform(final MavenBuildProxy build, final MavenProject pom, final MojoInfo mojo, final PluginLogger logger) throws InterruptedException, IOException {
        FilePath basedir = new FilePath(pom.getBasedir());

        WorkspaceScanner workspaceScanner = new WorkspaceScanner(
                StringUtils.defaultIfEmpty(pattern, DEFAULT_PATTERN),
                excludePattern, getDefaultEncoding(), high, normal, low, ignoreCase, pom.getName(),
                pom.getModules(), asRegexp);
        TasksParserResult project = basedir.act(workspaceScanner);

        project.setLog(project.getLogMessages()
                + String.format("Found %d open tasks.%n", project.getNumberOfAnnotations()));

        return project;
    }

    @Override
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("BC")
    protected TasksResult createResult(final MavenBuild build, final ParserResult project) {
        return new TasksReporterResult(build, getDefaultEncoding(), (TasksParserResult)project,
                useOnlyStableBuildsAsReference(), high, normal, low);
    }

    @Override
    protected MavenAggregatedReport createMavenAggregatedReport(final MavenBuild build, final TasksResult result) {
        return new TasksMavenResultAction(build, this, getDefaultEncoding(), high, normal, low, result);
    }

    @Override
    public List<TasksProjectAction> getProjectActions(final MavenModule module) {
        return Collections.singletonList(new TasksProjectAction(module, getResultActionClass()));
    }

    @Override
    protected Class<TasksMavenResultAction> getResultActionClass() {
        return TasksMavenResultAction.class;
    }

    // Backward compatibility. Do not remove.
    // CHECKSTYLE:OFF
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("")
    @SuppressWarnings({"all", "PMD"})
    @Deprecated
    private transient boolean isThresholdEnabled;
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("")
    @SuppressWarnings({"all", "PMD"})
    @Deprecated
    private transient boolean isHealthyReportEnabled;
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("")
    @SuppressWarnings({"all", "PMD"})
    @Deprecated
    private transient int healthyTasks;
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("")
    @SuppressWarnings({"all", "PMD"})
    @Deprecated
    private transient int unHealthyTasks;
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("")
    @SuppressWarnings({"all", "PMD"})
    @Deprecated
    private transient int minimumTasks;
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("")
    @SuppressWarnings({"all", "PMD"})
    @Deprecated
    private transient String height;
}

