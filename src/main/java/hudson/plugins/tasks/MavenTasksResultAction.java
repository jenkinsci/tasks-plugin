package hudson.plugins.tasks;

import java.util.List;
import java.util.Map;

import hudson.maven.AggregatableAction;
import hudson.maven.MavenAggregatedReport;
import hudson.maven.MavenBuild;
import hudson.maven.MavenModule;
import hudson.maven.MavenModuleSet;
import hudson.maven.MavenModuleSetBuild;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.plugins.analysis.core.HealthDescriptor;
import hudson.plugins.analysis.core.ParserResult;
import hudson.plugins.tasks.parser.TasksParserResult;

/**
 * A {@link TasksResultAction} for native maven jobs. This action
 * additionally provides result aggregation for sub-modules and for the main
 * project.
 *
 * @author Ulli Hafner
 * @deprecated not used anymore
 */
@SuppressWarnings("deprecation")
@Deprecated
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

    @Override
    public MavenAggregatedReport createAggregatedAction(final MavenModuleSetBuild build, final Map<MavenModule, List<MavenBuild>> moduleBuilds) {
        return new MavenTasksResultAction(build, getHealthDescriptor(), defaultEncoding, high, normal, low,
                new TasksResult(build, defaultEncoding, new TasksParserResult(), false, false, high, normal, low));
    }

    @Override
    public Action getProjectAction(final MavenModuleSet moduleSet) {
        return new TasksProjectAction(moduleSet);
    }

    @Override
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
    @Override
    public void update(final Map<MavenModule, List<MavenBuild>> moduleBuilds, final MavenBuild newBuild) {
        // not used anymore
    }

    @Override
    protected ParserResult createResult() {
        return new TasksParserResult();
    }

    /** Backward compatibility. @deprecated */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings("UUF")
    @SuppressWarnings("PMD")
    @Deprecated
    private transient String height;
}

