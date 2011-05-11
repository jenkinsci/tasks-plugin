package hudson.plugins.tasks;

import hudson.maven.AggregatableAction;
import hudson.maven.MavenAggregatedReport;
import hudson.maven.MavenBuild;
import hudson.maven.MavenModule;
import hudson.maven.MavenModuleSet;
import hudson.maven.MavenModuleSetBuild;
import hudson.model.Action;
import hudson.model.AbstractBuild;
import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.core.HealthDescriptor;
import hudson.plugins.analysis.core.ParserResult;
import hudson.plugins.analysis.util.PluginLogger;
import hudson.plugins.analysis.util.model.Priority;
import hudson.plugins.tasks.parser.TasksParserResult;

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
    /** Tag identifiers indicating high priority. */
    private String high;
    /** Tag identifiers indicating normal priority. */
    private String normal;
    /** Tag identifiers indicating low priority. */
    private String low;
    /** The default encoding to be used when reading and parsing files. */
    private String defaultEncoding;

    /**
     * Creates a new instance of {@link MavenTasksResultAction}.
     *
     * @param owner
     *            the associated build of this action
     * @param healthDescriptor
     *            health descriptor to use
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
    public MavenTasksResultAction(final AbstractBuild<?, ?> owner, final HealthDescriptor healthDescriptor, final String defaultEncoding,
            final String high, final String normal, final String low, final TasksResult result) {
        super(owner, healthDescriptor, result);
        initializeFields(defaultEncoding, high, normal, low);
    }
    // CHECKSTYLE:ON

    /**
     * Creates a new instance of {@link MavenTasksResultAction}.
     *
     * @param owner
     *            the associated build of this action
     * @param healthDescriptor
     *            health descriptor to use
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param high
     *            tag identifiers indicating high priority
     * @param normal
     *            tag identifiers indicating normal priority
     * @param low
     *            tag identifiers indicating low priority
     */
    public MavenTasksResultAction(final AbstractBuild<?, ?> owner, final HealthDescriptor healthDescriptor, final String defaultEncoding,
            final String high, final String normal, final String low) {
        super(owner, healthDescriptor);
        initializeFields(defaultEncoding, high, normal, low);
    }

    /**
     * Initializes the fields of this action.
     *
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
    private void initializeFields(final String defaultEncoding, final String high, final String normal, final String low) {
        this.high = high;
        this.normal = normal;
        this.low = low;
        this.defaultEncoding = defaultEncoding;
    }
    // CHECKSTYLE:ON

    /** {@inheritDoc} */
    public MavenAggregatedReport createAggregatedAction(final MavenModuleSetBuild build, final Map<MavenModule, List<MavenBuild>> moduleBuilds) {
        return new MavenTasksResultAction(build, getHealthDescriptor(), defaultEncoding, high, normal, low);
    }

    /** {@inheritDoc} */
    public Action getProjectAction(final MavenModuleSet moduleSet) {
        return new TasksProjectAction(moduleSet);
    }

    /** {@inheritDoc} */
    public Class<? extends AggregatableAction> getIndividualActionType() {
        return getClass();
    }

    /**
     * Called whenever a new module build is completed, to update the aggregated
     * report. When multiple builds complete simultaneously, Jenkins serializes
     * the execution of this method, so this method needs not be
     * concurrency-safe.
     *
     * @param moduleBuilds
     *            Same as <tt>MavenModuleSet.getModuleBuilds()</tt> but provided
     *            for convenience and efficiency.
     * @param newBuild
     *            Newly completed build.
     */
    public void update(final Map<MavenModule, List<MavenBuild>> moduleBuilds, final MavenBuild newBuild) {
        MavenTasksResultAction additionalAction = newBuild.getAction(MavenTasksResultAction.class);
        if (additionalAction != null) {
            TasksResult existingResult = getResult();
            TasksResult additionalResult = additionalAction.getResult();

            log("Aggregating results of " + newBuild.getProject().getDisplayName());

            if (existingResult == null) {
                setResult(additionalResult);
                getOwner().setResult(additionalResult.getPluginResult());
            }
            else {
                setResult(aggregate(existingResult, additionalResult, getLogger()));
            }
        }
    }

    /**
     * Creates a new instance of {@link BuildResult} that contains the aggregated
     * results of this result and the provided additional result.
     *
     * @param existingResult
     *            the existing result
     * @param additionalResult
     *            the result that will be added to the existing result
     * @param logger
     *            the plug-in logger
     * @return the aggregated result
     */
    public TasksResult aggregate(final TasksResult existingResult, final TasksResult additionalResult, final PluginLogger logger) {
        TasksParserResult aggregatedAnnotations = new TasksParserResult();
        aggregatedAnnotations.addAnnotations(existingResult.getAnnotations());
        aggregatedAnnotations.addScannedFiles(existingResult.getNumberOfFiles());
        aggregatedAnnotations.addAnnotations(additionalResult.getAnnotations());
        aggregatedAnnotations.addScannedFiles(additionalResult.getNumberOfFiles());

        TasksResult createdResult = new TasksResult(getOwner(), existingResult.getDefaultEncoding(), aggregatedAnnotations,
                existingResult.getTags(Priority.HIGH), existingResult.getTags(Priority.NORMAL), existingResult.getTags(Priority.LOW));
        createdResult.evaluateStatus(existingResult.getThresholds(), existingResult.canUseDeltaValues(), logger);
        return createdResult;
    }
    /** {@inheritDoc} */
    @Override
    protected ParserResult createResult() {
        return new TasksParserResult();
    }

    /** Backward compatibility. @deprecated */
    @SuppressWarnings("unused")
    @Deprecated
    private transient String height;
}

