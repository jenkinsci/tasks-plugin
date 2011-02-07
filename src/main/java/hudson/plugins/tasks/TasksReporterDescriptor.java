package hudson.plugins.tasks;

import hudson.Extension;
import hudson.maven.MavenReporter;
import hudson.plugins.analysis.core.ReporterDescriptor;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

/**
 * Descriptor for the class {@link TasksReporter}. Used as a singleton. The
 * class is marked as public so that it can be accessed from views.
 *
 * @author Ulli Hafner
 */
@Extension(ordinal = 100) // NOCHECKSTYLE
public class TasksReporterDescriptor extends ReporterDescriptor {
    /**
     * Creates a new instance of <code>TasksReporterDescriptor</code>.
     */
    public TasksReporterDescriptor() {
        super(TasksReporter.class, new TasksDescriptor());
    }

    /** {@inheritDoc} */
    @Override
    public MavenReporter newInstance(final StaplerRequest request, final JSONObject formData) throws FormException {
        return request.bindJSON(TasksReporter.class, formData);
    }
}

