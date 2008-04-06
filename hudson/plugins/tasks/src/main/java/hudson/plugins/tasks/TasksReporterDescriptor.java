package hudson.plugins.tasks;

import hudson.maven.MavenReporter;
import hudson.maven.MavenReporterDescriptor;

import org.kohsuke.stapler.StaplerRequest;

/**
 * Descriptor for the class {@link TasksReporter}. Used as a singleton. The
 * class is marked as public so that it can be accessed from views.
 *
 * @author Ulli Hafner
 */
public class TasksReporterDescriptor extends MavenReporterDescriptor {
    /**
     * Creates a new instance of <code>TasksReporterDescriptor</code>.
     */
    public TasksReporterDescriptor() {
        super(TasksReporter.class);
    }

    /** {@inheritDoc} */
    @Override
    public String getDisplayName() {
        return Messages.Tasks_Publisher_Name();
    }

    /** {@inheritDoc} */
    @Override
    public String getConfigPage() {
        return getViewPage(TasksPublisher.class, "config.jelly");
    }

    /** {@inheritDoc} */
    @Override
    public String getHelpFile() {
        return "/plugin/" +  TasksDescriptor.PLUGIN_NAME + "/help.html";
    }

    /** {@inheritDoc} */
    @Override
    public MavenReporter newInstance(final StaplerRequest request) throws FormException {
        return request.bindParameters(TasksReporter.class, "tasks_");
    }
}

