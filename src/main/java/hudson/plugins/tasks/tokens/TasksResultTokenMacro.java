package hudson.plugins.tasks.tokens;

import hudson.Extension;
import hudson.plugins.analysis.tokens.AbstractResultTokenMacro;
import hudson.plugins.tasks.TasksResultAction;

/**
 * Provides a token that evaluates to the tasks scanner build result.
 *
 * @author Ulli Hafner
 */
@Extension(optional = true)
public class TasksResultTokenMacro extends AbstractResultTokenMacro {
    /**
     * Creates a new instance of {@link TasksResultTokenMacro}.
     */
    public TasksResultTokenMacro() {
        super(TasksResultAction.class, "TASKS_RESULT");
    }
}

