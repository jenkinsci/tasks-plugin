package hudson.plugins.tasks;

import hudson.model.AbstractBuild;
import hudson.plugins.tasks.parser.TasksParserResult;
import hudson.plugins.tasks.util.BuildResult;
import hudson.plugins.tasks.util.model.JavaProject;

/**
 * Represents the aggregated results of the PMD analysis in m2 jobs.
 *
 * @author Ulli Hafner
 */
public class TasksMavenResult extends TasksResult {
    /** Unique ID of this class. */
    private static final long serialVersionUID = -4913938782537266259L;

    /**
     * Creates a new instance of {@link TasksMavenResult}.
     *
     * @param build
     *            the current build as owner of this action
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param result
     *            the parsed annotations
     * @param highTags
     *            tag identifiers indicating high priority
     * @param normalTags
     *            tag identifiers indicating normal priority
     * @param lowTags
     *            tag identifiers indicating low priority
     */
    public TasksMavenResult(final AbstractBuild<?, ?> build, final String defaultEncoding, final TasksParserResult result,
            final String highTags, final String normalTags, final String lowTags) {
        super(build, defaultEncoding, result, highTags, normalTags, lowTags);
    }

    /**
     * Creates a new instance of {@link TasksMavenResult}.
     *
     * @param build
     *            the current build as owner of this action
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param result
     *            the parsed FindBugs result
     * @param previous
     *            the previous result of open tasks
     * @param highTags
     *            tag identifiers indicating high priority
     * @param normalTags
     *            tag identifiers indicating normal priority
     * @param lowTags
     *            tag identifiers indicating low priority
     */
    public TasksMavenResult(final AbstractBuild<?, ?> build, final String defaultEncoding,
            final TasksParserResult result, final BuildResult previous,
            final String highTags, final String normalTags, final String lowTags) {
        super(build, defaultEncoding, result, previous, highTags, normalTags, lowTags);
    }

    /**
     * Returns the results of the previous build.
     *
     * @return the result of the previous build, or <code>null</code> if no
     *         such build exists
     */
    @Override
    public JavaProject getPreviousResult() {
        MavenTasksResultAction action = getOwner().getAction(MavenTasksResultAction.class);
        if (action.hasPreviousResultAction()) {
            return action.getPreviousResultAction().getResult().getProject();
        }
        else {
            return null;
        }
    }

    /**
     * Returns whether a previous build result exists.
     *
     * @return <code>true</code> if a previous build result exists.
     */
    @Override
    public boolean hasPreviousResult() {
        return getOwner().getAction(MavenTasksResultAction.class).hasPreviousResultAction();
    }
}

