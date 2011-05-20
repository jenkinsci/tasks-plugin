package hudson.plugins.tasks;

import hudson.Plugin;
import hudson.plugins.analysis.views.DetailFactory;

/**
 * Registers the task scanner plug-in publisher.
 *
 * @author Ulli Hafner
 */
public class TasksPlugin extends Plugin {
    /** {@inheritDoc} */
    @Override
    public void start() {
        DetailFactory.addDetailBuilder(TasksResultAction.class, new TasksDetailBuilder());
    }
}
