package hudson.plugins.tasks.dashboard;

import hudson.plugins.analysis.core.AbstractProjectAction;
import hudson.plugins.analysis.dashboard.AbstractWarningsGraphPortlet;
import hudson.plugins.tasks.TasksProjectAction;

/**
 * A base class for portlets of the Task Scanner plug-in.
 *
 * @author Ulli Hafner
 */
public abstract class TasksPortlet extends AbstractWarningsGraphPortlet {
    /**
     * Creates a new instance of {@link TasksPortlet}.
     *
     * @param name
     *            the name of the portlet
     */
    public TasksPortlet(final String name) {
        super(name);
    }

    /** {@inheritDoc} */
    @Override
    protected Class<? extends AbstractProjectAction<?>> getAction() {
        return TasksProjectAction.class;
    }

    /** {@inheritDoc} */
    @Override
    protected String getPluginName() {
        return "tasks";
    }
}
