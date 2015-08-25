package hudson.plugins.tasks;

import hudson.model.Run;

import hudson.plugins.analysis.core.ResultAction;
import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.tasks.parser.TasksParserResult;

/**
 * Represents the aggregated results of the open tasks scanner in m2 jobs.
 *
 * @author Ulli Hafner
 */
public class TasksReporterResult extends TasksResult {
    private static final long serialVersionUID = 3803699268659365514L;

    /**
     * Creates a new instance of {@link TasksReporterResult}.
     *
     * @param build
     *            the current build as owner of this action
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param result
     *            the parsed annotations
     * @param usePreviousBuildAsReference
     *            determines whether to always use the previous build as the reference build
     * @param useStableBuildAsReference
     *            determines whether only stable builds should be used as
     *            reference builds or not
     * @param highTags
     *            tag identifiers indicating high priority
     * @param normalTags
     *            tag identifiers indicating normal priority
     * @param lowTags
     *            tag identifiers indicating low priority
     */
    public TasksReporterResult(final Run<?, ?> build, final String defaultEncoding, final TasksParserResult result,
            final boolean usePreviousBuildAsReference, final boolean useStableBuildAsReference,
            final String highTags, final String normalTags, final String lowTags) {
        super(build, defaultEncoding, result, usePreviousBuildAsReference, useStableBuildAsReference,
                highTags, normalTags, lowTags, TasksMavenResultAction.class);
    }

    @Override
    protected Class<? extends ResultAction<? extends BuildResult>> getResultActionType() {
        return TasksMavenResultAction.class;
    }
}

