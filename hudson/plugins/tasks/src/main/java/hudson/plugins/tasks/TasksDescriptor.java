package hudson.plugins.tasks;

import hudson.maven.AbstractMavenProject;
import hudson.model.AbstractProject;
import hudson.plugins.tasks.util.ThresholdValidator;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.util.FormFieldValidator;

import java.io.IOException;

import javax.servlet.ServletException;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Descriptor for the class {@link TasksPublisher}. Used as a singleton. The
 * class is marked as public so that it can be accessed from views.
 *
 * @author Ulli Hafner
 */
public final class TasksDescriptor extends BuildStepDescriptor<Publisher> {
    /** Icon to use for the result and project action. */
    public static final String TASKS_ACTION_LOGO = "/plugin/tasks/icons/tasks-24x24.png";

    /**
     * Instantiates a new find bugs descriptor.
     */
    TasksDescriptor() {
        super(TasksPublisher.class);
    }

    /** {@inheritDoc} */
    @Override
    public String getDisplayName() {
        return "Scan workspace for open tasks";
    }

    /** {@inheritDoc} */
    @Override
    public String getHelpFile() {
        return "/plugin/tasks/help.html";
    }

    /**
     * Performs on-the-fly validation on thresholds.
     *
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     */
    public void doCheckThreshold(final StaplerRequest request, final StaplerResponse response)
            throws IOException, ServletException {
        new ThresholdValidator(request, response).process();
    }

    /**
     * Performs on-the-fly validation on the file mask.
     *
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     */
    public void doCheckPattern(final StaplerRequest request, final StaplerResponse response)
            throws IOException, ServletException {
        new FormFieldValidator.WorkspaceFileMask(request, response).process();
    }

    /** {@inheritDoc} */
    @Override
    public TasksPublisher newInstance(final StaplerRequest request) throws FormException {
        return request.bindParameters(TasksPublisher.class, "tasks_");
    }

    /** {@inheritDoc} */
    @Override
    public boolean isApplicable(final Class<? extends AbstractProject> jobType) {
        return !AbstractMavenProject.class.isAssignableFrom(jobType);
    }
}