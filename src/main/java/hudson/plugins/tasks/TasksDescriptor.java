package hudson.plugins.tasks;

import hudson.plugins.tasks.util.PluginDescriptor;

/**
 * Descriptor for the class {@link TasksPublisher}. Used as a singleton. The
 * class is marked as public so that it can be accessed from views.
 *
 * @author Ulli Hafner
 */
public final class TasksDescriptor extends PluginDescriptor {
    /** Plug-in name. */
    private static final String PLUGIN_NAME = "tasks";
    /** Icon to use for the result and project action. */
    private static final String ACTION_ICON = "/plugin/tasks/icons/tasks-24x24.png";

    /**
     * Instantiates a new find bugs descriptor.
     */
    TasksDescriptor() {
        super(TasksPublisher.class);
    }

    /** {@inheritDoc} */
    @Override
    public String getDisplayName() {
        return Messages.Tasks_Publisher_Name();
    }

    /** {@inheritDoc} */
    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

    /** {@inheritDoc} */
    @Override
    public String getIconUrl() {
        return ACTION_ICON;
    }
}