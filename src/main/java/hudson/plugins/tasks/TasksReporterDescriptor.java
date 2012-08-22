package hudson.plugins.tasks;

import hudson.Extension;
import hudson.plugins.analysis.core.ReporterDescriptor;

/**
 * Descriptor for the class {@link TasksReporter}. Used as a singleton. The
 * class is marked as public so that it can be accessed from views.
 *
 * @author Ulli Hafner
 */
@Extension(ordinal = 100, optional = true) // NOCHECKSTYLE
public class TasksReporterDescriptor extends ReporterDescriptor {
    /**
     * Creates a new instance of <code>TasksReporterDescriptor</code>.
     */
    public TasksReporterDescriptor() {
        super(TasksReporter.class, new TasksDescriptor());
    }
}

