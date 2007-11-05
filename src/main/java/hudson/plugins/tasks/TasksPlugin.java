package hudson.plugins.tasks;

import hudson.Plugin;
import hudson.tasks.BuildStep;

/**
 * Registers the task scanner plug-in publisher.
 *
 * @author Ulli Hafner
 */
@SuppressWarnings("PMD")
public class TasksPlugin extends Plugin {
    /** {@inheritDoc} */
    @Override
    public void start() throws Exception {
        BuildStep.PUBLISHERS.addRecorder(TasksPublisher.TASK_SCANNER_DESCRIPTOR);
    }
}
