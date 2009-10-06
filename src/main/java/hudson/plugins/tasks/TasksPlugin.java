package hudson.plugins.tasks;

import hudson.Plugin;

/**
 * Registers the task scanner plug-in publisher.
 *
 * @author Ulli Hafner
 */
public class TasksPlugin extends Plugin {
    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("PMD")
    public void start() throws Exception {
        // FIXME: check if we can register a new detail builder?
    }
}
