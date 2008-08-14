package hudson.plugins.tasks;

import hudson.model.AbstractBuild;
import hudson.plugins.tasks.util.TabDetail;
import hudson.plugins.tasks.util.model.AnnotationContainer;
import hudson.plugins.tasks.util.model.Priority;

import java.util.Collection;

/**
 * TODO: Document type TasksTabDetail.
 *
 * @author Ulli Hafner
 */
public class TasksTabDetail extends TabDetail {
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

