package hudson.plugins.tasks.tokens;

import hudson.Extension;
import hudson.plugins.analysis.tokens.AbstractFixedAnnotationsTokenMacro;
import hudson.plugins.tasks.TasksMavenResultAction;
import hudson.plugins.tasks.TasksResultAction;

/**
 * Provides a token that evaluates to the number of fixed tasks.
 *
 * @author Ulli Hafner
 */
@Extension(optional = true)
public class FixedTasksTokenMacro extends AbstractFixedAnnotationsTokenMacro {
    /**
     * Creates a new instance of {@link FixedTasksTokenMacro}.
     */
    @SuppressWarnings("unchecked")
    public FixedTasksTokenMacro() {
        super("TASKS_FIXED", TasksResultAction.class, TasksMavenResultAction.class);
    }
}

