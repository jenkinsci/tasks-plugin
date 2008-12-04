package hudson.plugins.tasks;

import hudson.model.AbstractBuild;
import hudson.plugins.tasks.util.TabDetail;
import hudson.plugins.tasks.util.model.AnnotationContainer;
import hudson.plugins.tasks.util.model.Priority;

import java.util.Collection;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Result object representing a dynamic tab of the tasks plug-in.
 *
 * @author Ulli Hafner
 */
public class TasksTabDetail extends TabDetail {
    /** Unique ID of this class. */
    private static final long serialVersionUID = 8964198520312051468L;
    /** Handles the task tags. */
    private final TaskTagsHandler taskTagsHandler;

    /**
     * Creates a new instance of <code>ModuleDetail</code>.
     *
     * @param owner
     *            current build as owner of this action.
     * @param container
     *            the container to show the details for
     * @param url
     *            URL to render the content of this tab
     * @param high
     *            tag identifiers indicating high priority
     * @param normal
     *            tag identifiers indicating normal priority
     * @param low
     *            tag identifiers indicating low priority
     */
    public TasksTabDetail(final AbstractBuild<?, ?> owner, final AnnotationContainer container, final String url,
            final String high, final String normal, final String low) {
        super(owner, container.getAnnotations(), url);

        taskTagsHandler = new TaskTagsHandler(high, normal, low, container);
    }

    /** {@inheritDoc} */
    @Override
    public Object getDynamic(final String link, final StaplerRequest request, final StaplerResponse response) {
        return new TasksDetailBuilder().getDynamic(link, getOwner(), getContainer(), getDisplayName(),
                    getTags(Priority.HIGH), getTags(Priority.NORMAL), getTags(Priority.LOW));
    }

    // CHECKSTYLE:OFF - generated delegate -

    /**
     * Returns all priorities that have a user defined tag.
     *
     * @return all priorities that have a user defined tag
     */
    public Collection<String> getAvailablePriorities() {
        return taskTagsHandler.getAvailablePriorities();
    }

    /** {@inheritDoc} */
    @Override
    public Priority[] getPriorities() {
        return taskTagsHandler.getPriorities();
    }

    /**
     * Returns the defined tags for the given priority.
     *
     * @param priority the priority
     * @return the defined tags for the given priority
     */
    public final String getTags(final Priority priority) {
        return taskTagsHandler.getTags(priority);
    }

    /**
     * Returns the defined tags for the given priority.
     *
     * @param priority the priority
     * @return the defined tags for the given priority
     */
    public final String getTags(final String priority) {
        return taskTagsHandler.getTags(priority);
    }
}

