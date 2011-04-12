package hudson.plugins.tasks.tokens;

import hudson.Extension;
import hudson.plugins.analysis.tokens.AbstractResultTokenMacro;
import hudson.plugins.tasks.TasksResultAction;

/**
 * Provides a token that evaluates to the number of open tasks.
 *
 * @author Ulli Hafner
 */
@Extension(optional = true)
public class TasksCountTokenMacro extends AbstractResultTokenMacro {
    /**
     * Creates a new instance of {@link TasksCountTokenMacro}.
     */
    public TasksCountTokenMacro() {
        super(TasksResultAction.class, "TASKS_COUNT");
    }
}

