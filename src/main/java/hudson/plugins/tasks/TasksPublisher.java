package hudson.plugins.tasks;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import hudson.FilePath;
import hudson.Launcher;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;
import hudson.model.BuildListener;
import hudson.model.Run;
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
    protected BuildResult perform(final Run<?, ?> build, FilePath workspace, final PluginLogger logger) throws InterruptedException, IOException {
        TasksParserResult project;
        WorkspaceScanner scanner = new WorkspaceScanner(StringUtils.defaultIfEmpty(getPattern(), DEFAULT_PATTERN),
                getExcludePattern(), getDefaultEncoding(), high, normal, low, ignoreCase, shouldDetectModules(), asRegexp);
        project = workspace.act(scanner);

        logger.logLines(project.getLogMessages());
        logger.log(String.format("Found %d open tasks.", project.getNumberOfAnnotations()));

        blame(project.getAnnotations(), build, workspace);

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
}
