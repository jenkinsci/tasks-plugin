package hudson.plugins.tasks;

import hudson.maven.AggregatableAction;
import hudson.maven.MavenAggregatedReport;
import hudson.maven.MavenBuild;
import hudson.maven.MavenModule;
import hudson.maven.MavenModuleSet;
import hudson.maven.MavenModuleSetBuild;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.plugins.tasks.parser.TasksParserResult;
import hudson.plugins.tasks.util.HealthDescriptor;
import hudson.plugins.tasks.util.ParserResult;
import hudson.plugins.tasks.util.TrendReportHeightValidator;

import java.util.List;
import java.util.Map;

/**
 * A {@link TasksResultAction} for native maven jobs. This action
 * additionally provides result aggregation for sub-modules and for the main
 * project.
 *
 * @author Ulli Hafner
 */
public class MavenTasksResultAction extends TasksResultAction implements AggregatableAction, MavenAggregatedReport {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 1273798369273225973L;
    /** Determines the height of the trend graph. */
    private String height;
    /** Tag identifiers indicating high priority. */
    private String high;
    /** Tag identifiers indicating normal priority. */
    private String normal;
    /** Tag identifiers indicating low priority. */
    private String low;
    /** The default encoding to be used when reading and parsing files. */
    private String defaultEncoding;

    /**
     * Creates a new instance of <code>MavenFindBugsResultAction</code>.
     *
     * @param owner
     *            the associated build of this action
     * @param healthDescriptor
     *            health descriptor to use
     * @param height
     *            the height of the trend graph
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param high
     *            tag identifiers indicating high priority
     * @param normal
     *            tag identifiers indicating normal priority
     * @param low
     *            tag identifiers indicating low priority
     * @param result
     *            the result in this build
     */
    // CHECKSTYLE:OFF
    public MavenTasksResultAction(final AbstractBuild<?, ?> owner, final HealthDescriptor healthDescriptor, final String height, final String defaultEncoding, final String high, final String normal, final String low, final TasksResult result) {
        super(owner, healthDescriptor, result);
        initializeFields(height, defaultEncoding, high, normal, low);
    }
    // CHECKSTYLE:ON

    /**
     * Creates a new instance of <code>MavenFindBugsResultAction</code>.
     *
     * @param owner
     *            the associated build of this action
     * @param healthDescriptor
     *            health descriptor to use
     * @param height
     *            the height of the trend graph
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param high
     *            tag identifiers indicating high priority
     * @param normal
     *            tag identifiers indicating normal priority
     * @param low
     *            tag identifiers indicating low priority
     */
    public MavenTasksResultAction(final AbstractBuild<?, ?> owner, final HealthDescriptor healthDescriptor, final String height, final String defaultEncoding, final String high, final String normal, final String low) {
        super(owner, healthDescriptor);
        initializeFields(height, defaultEncoding, high, normal, low);
    }

    /**
     * Initializes the fields of this action.
     *
     * @param height
     *            the height of the trend graph
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param high
     *            tag identifiers indicating high priority
     * @param normal
     *            tag identifiers indicating normal priority
     * @param low
     *            tag identifiers indicating low priority
     */
    // CHECKSTYLE:OFF
    @SuppressWarnings("hiding")
    private void initializeFields(final String height, final String defaultEncoding, final String high, final String normal, final String low) {
        this.height = height;
        this.high = high;
        this.normal = normal;
        this.low = low;
        this.defaultEncoding = defaultEncoding;
    }
    // CHECKSTYLE:ON

    /** {@inheritDoc} */
    public MavenAggregatedReport createAggregatedAction(final MavenModuleSetBuild build, final Map<MavenModule, List<MavenBuild>> moduleBuilds) {
        return new MavenTasksResultAction(build, getHealthDescriptor(), height, defaultEncoding, high, normal, low);
    }

    /** {@inheritDoc} */
    public Action getProjectAction(final MavenModuleSet moduleSet) {
        return new TasksProjectAction(moduleSet, TrendReportHeightValidator.defaultHeight(height));
    }

    /** {@inheritDoc} */
    public Class<? extends AggregatableAction> getIndividualActionType() {
        return getClass();
    }

    /**
     * Called whenever a new module build is completed, to update the
     * aggregated report. When multiple builds complete simultaneously,
     * Hudson serializes the execution of this method, so this method
     * needs not be concurrency-safe.
     *
     * @param moduleBuilds
     *      Same as <tt>MavenModuleSet.getModuleBuilds()</tt> but provided for convenience and efficiency.
     * @param newBuild
     *      Newly completed build.
     */
    public void update(final Map<MavenModule, List<MavenBuild>> moduleBuilds, final MavenBuild newBuild) {
        ParserResult result = createAggregatedResult(moduleBuilds);

        if (result instanceof TasksParserResult) {
            setResult(new TasksResultBuilder().build(getOwner(), (TasksParserResult)result, defaultEncoding, high, normal, low));
        }
    }

    /** {@inheritDoc} */
    @Override
    protected ParserResult createResult() {
        return new TasksParserResult();
    }
}

