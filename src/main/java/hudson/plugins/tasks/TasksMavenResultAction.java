package hudson.plugins.tasks;

import hudson.maven.MavenAggregatedReport;
import hudson.maven.MavenBuild;
import hudson.maven.MavenModule;
import hudson.maven.MavenModuleSet;
import hudson.maven.MavenModuleSetBuild;
import hudson.model.Action;
import hudson.model.AbstractBuild;
import hudson.plugins.analysis.core.HealthDescriptor;
import hudson.plugins.analysis.core.MavenResultAction;
import hudson.plugins.tasks.parser.TasksParserResult;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

/**
 * A {@link TasksResultAction} for native Maven jobs. This action
 * additionally provides result aggregation for sub-modules and for the main
 * project.
 *
 * @author Ulli Hafner
 */
public class TasksMavenResultAction extends MavenResultAction<TasksResult> {
    /** Tag identifiers indicating high priority. */
    private String high;
    /** Tag identifiers indicating normal priority. */
    private String normal;
    /** Tag identifiers indicating low priority. */
    private String low;

    /**
     * Creates a new instance of {@link TasksMavenResultAction}.
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
    public TasksMavenResultAction(final AbstractBuild<?, ?> owner, final HealthDescriptor healthDescriptor,
            final String defaultEncoding, final String high, final String normal, final String low, final TasksResult result) {
        super(new TasksResultAction(owner, healthDescriptor, result), defaultEncoding, "TASKS");

        initializeFields(high, normal, low);
    }

    /**
     * Initializes the fields of this action.
     * @param high
     *            tag identifiers indicating high priority
     * @param normal
     *            tag identifiers indicating normal priority
     * @param low
     *            tag identifiers indicating low priority
     */
    // CHECKSTYLE:OFF
    @SuppressWarnings("hiding")
    private void initializeFields(final String high, final String normal, final String low) {
        this.high = high;
        this.normal = normal;
        this.low = low;
    }
    // CHECKSTYLE:ON

    /** {@inheritDoc} */
    public MavenAggregatedReport createAggregatedAction(final MavenModuleSetBuild build, final Map<MavenModule, List<MavenBuild>> moduleBuilds) {
        return new TasksMavenResultAction(build, getHealthDescriptor(), getDefaultEncoding(), high, normal, low,
                new TasksResult(build, high, new TasksParserResult(), false, high, normal, low));
    }

    /** {@inheritDoc} */
    public Action getProjectAction(final MavenModuleSet moduleSet) {
        return new TasksProjectAction(moduleSet, TasksMavenResultAction.class);
    }

    @Override
    public Class<? extends MavenResultAction<TasksResult>> getIndividualActionType() {
        return TasksMavenResultAction.class;
    }

    @Override
    protected TasksResult createResult(final TasksResult existingResult, final TasksResult additionalResult) {
        return new TasksReporterResult(getOwner(), additionalResult.getDefaultEncoding(),
                aggregate(existingResult, additionalResult), existingResult.useOnlyStableBuildsAsReference(),
                high, normal, low);
    }

    @Override
    protected TasksParserResult aggregate(final TasksResult existingResult, final TasksResult additionalResult) {
        TasksParserResult aggregatedAnnotations = new TasksParserResult();

        List<TasksResult> results = Lists.newArrayList();
        if (existingResult != null) {
            results.add(existingResult);
        }
        results.add(additionalResult);

        for (TasksResult result : results) {
            aggregatedAnnotations.addAnnotations(result.getAnnotations());
            aggregatedAnnotations.addModules(result.getModules());
            aggregatedAnnotations.addErrors(result.getErrors());
            aggregatedAnnotations.addScannedFiles(result.getNumberOfFiles());
        }

        return aggregatedAnnotations;
    }
}

