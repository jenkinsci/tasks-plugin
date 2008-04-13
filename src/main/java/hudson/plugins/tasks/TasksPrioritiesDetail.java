package hudson.plugins.tasks;

import hudson.model.AbstractBuild;
import hudson.plugins.tasks.util.PrioritiesDetail;
import hudson.plugins.tasks.util.model.AnnotationContainer;
import hudson.plugins.tasks.util.model.Priority;

import java.util.Collection;

/**
 * Result object to visualize the priorities statistics of an annotation container.
 */
public class TasksPrioritiesDetail extends PrioritiesDetail {
    /** Handles the task tags. */
    private final TaskTagsHandler taskTagsHandler;

    /**
     * Creates a new instance of <code>TasksPrioritiesDetail</code>.
     *
     * @param owner
     *            the current build as owner of this result object
     * @param container
     *            the annotations to show the details for
     * @param priority
     *            the priority of all annotations
     * @param header
     *            header to be shown on detail page
     * @param high
     *            tag identifiers indicating high priority
     * @param normal
     *            tag identifiers indicating normal priority
     * @param low
     *            tag identifiers indicating low priority
     */
    public TasksPrioritiesDetail(final AbstractBuild<?, ?> owner, final AnnotationContainer container,
            final Priority priority, final String header, final String high, final String normal, final String low) {
        super(owner, container.getAnnotations(priority), priority, header);

        taskTagsHandler = new TaskTagsHandler(high, normal, low, container);
    }

    // CHECKSTYLE:OFF - generated delegate -

    public Collection<String> getAvailablePriorities() {
        return taskTagsHandler.getAvailablePriorities();
    }

    @Override
    public Priority[] getPriorities() {
        return taskTagsHandler.getPriorities();
    }

    public final String getTags(final Priority priority) {
        return taskTagsHandler.getTags(priority);
    }

    public final String getTags(final String priority) {
        return taskTagsHandler.getTags(priority);
    }
}

