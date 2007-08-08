package hudson.plugins.tasks;

import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.Build;
import hudson.model.Project;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * Entry point to visualize the task scanner trend graph. Drawing of the graph is
 * delegated to the associated {@link TasksResultAction}.
 *
 * @author Ulli Hafner
 */
public class TasksProjectAction implements Action {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 2726362508120270158L;
    /** Project that owns this action. */
    @SuppressWarnings("Se")
    private final Project<?, ?> project;

    /**
     * Instantiates a new find bugs project action.
     *
     * @param project
     *            the project that owns this action
     */
    public TasksProjectAction(final Project<?, ?> project) {
        this.project = project;
    }

    /**
     * Returns the project.
     *
     * @return the project
     */
    public Project<?, ?> getProject() {
        return project;
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return "Open Tasks";
    }

    /** {@inheritDoc} */
    public String getIconFileName() {
        return null;
    }

    /** {@inheritDoc} */
    public String getUrlName() {
        return "tasks";
    }

    /**
     * Returns whether we have enough valid results in order to draw a
     * meaningful graph.
     *
     * @param build
     *            the build to look backward from
     * @return <code>true</code> if the results are valid in order to draw a
     *         graph
     */
    public boolean hasValidResults(final Build<?, ?> build) {
        if (build != null) {
            TasksResultAction resultAction = build.getAction(TasksResultAction.class);
            if (resultAction != null) {
                return resultAction.hasPreviousResult();
            }
        }
        return false;
    }

    /**
     * Display the warnings trend. Delegates to the the associated
     * {@link TasksResultAction}.
     *
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @throws IOException
     *             in case of an error in
     *             {@link TasksResultAction#doGraph(StaplerRequest, StaplerResponse)}
     */
    public void doTrend(final StaplerRequest request, final StaplerResponse response) throws IOException {
        TasksResultAction action = getLastAction();
        if (action == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
        else {
            action.doGraph(request, response);
        }
    }

    /**
     * Display the warnings trend map. Delegates to the the associated
     * {@link TasksResultAction}.
     *
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @throws IOException
     *             in case of an error in
     *             {@link TasksResultAction#doGraph(StaplerRequest, StaplerResponse)}
     */
    public void doTrendMap(final StaplerRequest request, final StaplerResponse response) throws IOException {
        TasksResultAction action = getLastAction();
        if (action == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
        else {
            action.doGraphMap(request, response);
        }
    }

    /**
     * Returns the last valid tasks result action.
     *
     * @return the last valid tasks result action, or null if no such action
     *         is found
     */
    public TasksResultAction getLastAction() {
        AbstractBuild<?, ?> lastBuild = project.getLastSuccessfulBuild();
        if (lastBuild != null) {
            return lastBuild.getAction(TasksResultAction.class);
        }
        return null;
    }
}

