package hudson.plugins.tasks;

import hudson.Plugin;
import hudson.plugins.analysis.views.DetailFactory;

/**
 * Registers the task scanner plug-in publisher.
 *
 * @author Ulli Hafner
 */
public class TasksPlugin extends Plugin {
    @Override
    public void start() {
        TasksDetailBuilder detailBuilder = new TasksDetailBuilder();
        DetailFactory.addDetailBuilder(TasksResultAction.class, detailBuilder);
        DetailFactory.addDetailBuilder(TasksMavenResultAction.class, detailBuilder);
    }
}
