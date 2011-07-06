package hudson.plugins.tasks.tokens;

import hudson.Extension;
import hudson.plugins.analysis.tokens.AbstractNewAnnotationsTokenMacro;
import hudson.plugins.tasks.TasksMavenResultAction;
import hudson.plugins.tasks.TasksResultAction;

/**
 * Provides a token that evaluates to the number of new open tasks.
 *
 * @author Ulli Hafner
 */
@Extension(optional = true)
public class NewTasksTokenMacro extends AbstractNewAnnotationsTokenMacro {
    /**
     * Creates a new instance of {@link NewTasksTokenMacro}.
     */
    @SuppressWarnings("unchecked")
    public NewTasksTokenMacro() {
        super("TASKS_NEW", TasksResultAction.class, TasksMavenResultAction.class);
    }
}

