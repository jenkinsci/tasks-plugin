package hudson.plugins.tasks.tokens;

import hudson.Extension;
import hudson.plugins.analysis.tokens.AbstractAnnotationsCountTokenMacro;
import hudson.plugins.tasks.TasksMavenResultAction;
import hudson.plugins.tasks.TasksResultAction;

/**
 * Provides a token that evaluates to the number of open tasks.
 *
 * @author Ulli Hafner
 */
@Extension(optional = true)
public class TasksCountTokenMacro extends AbstractAnnotationsCountTokenMacro {
    /**
     * Creates a new instance of {@link TasksCountTokenMacro}.
     */
    @SuppressWarnings("unchecked")
    public TasksCountTokenMacro() {
        super("TASKS_COUNT", TasksResultAction.class, TasksMavenResultAction.class);
    }
}

