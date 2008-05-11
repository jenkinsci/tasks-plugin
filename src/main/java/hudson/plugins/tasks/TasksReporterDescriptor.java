package hudson.plugins.tasks;

import hudson.plugins.tasks.util.PluginDescriptor;
import hudson.plugins.tasks.util.ReporterDescriptor;

/**
 * Descriptor for the class {@link TasksReporter}. Used as a singleton. The
 * class is marked as public so that it can be accessed from views.
 *
 * @author Ulli Hafner
 */
public class TasksReporterDescriptor extends ReporterDescriptor {
    /**
     * Creates a new instance of <code>TasksReporterDescriptor</code>.
     *
     * @param pluginDescriptor
     *            the plug-in descriptor of the publisher
     */
    public TasksReporterDescriptor(final PluginDescriptor pluginDescriptor) {
        super(TasksReporter.class, pluginDescriptor);
    }

    /** {@inheritDoc} */
    @Override
    public String getConfigPage() {
        return getViewPage(TasksPublisher.class, "config.jelly");
    }
}

