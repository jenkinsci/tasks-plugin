package hudson.plugins.tasks;

import hudson.model.AbstractBuild;
import hudson.plugins.tasks.parser.TasksProject;

/**
 * Creates a new Tasks result based on the values of a previous build and the
 * current project.
 */
public class TasksResultBuilder {
    /**
     * Creates a result that persists the FindBugs information for the
     * specified build.
     *
     * @param build
     *            the build to create the action for
     * @param project
     *            the project containing the annotations
     * @param high
     *            tag identifiers indicating high priority
     * @param normal
     *            tag identifiers indicating normal priority
     * @param low
     *            tag identifiers indicating low priority
     * @return the result action
     */
    public TasksResult build(final AbstractBuild<?, ?> build, final TasksProject project,
            final String high, final String normal, final String low) {
        Object previous = build.getPreviousBuild();
        if (previous instanceof AbstractBuild<?, ?>) {
            AbstractBuild<?, ?> previousBuild = (AbstractBuild<?, ?>)previous;
            TasksResultAction previousAction = previousBuild.getAction(TasksResultAction.class);
            if (previousAction != null) {
                return new TasksResult(build, project, previousAction.getResult().getNumberOfAnnotations(), high, normal, low);
            }
        }
        return new TasksResult(build, project, high, normal, low);
    }
}

