package hudson.plugins.tasks;

import hudson.model.AbstractBuild;
import hudson.plugins.analysis.util.model.AnnotationContainer;
import hudson.plugins.analysis.util.model.Priority;
import hudson.plugins.analysis.views.DetailFactory;
import hudson.plugins.analysis.views.PrioritiesDetail;

import java.util.Collection;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Result object to visualize the priorities statistics of an annotation container.
 *
 * @author Ulli Hafner
 */
public class TasksPrioritiesDetail extends PrioritiesDetail {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -4312016503040391234L;
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
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param header
     *            header to be shown on detail page
     * @param high
     *            tag identifiers indicating high priority
     * @param normal
     *            tag identifiers indicating normal priority
     * @param low
     *            tag identifiers indicating low priority
     */
    // CHECKSTYLE:OFF
    public TasksPrioritiesDetail(final AbstractBuild<?, ?> owner, final AnnotationContainer container,
            final Priority priority, final String defaultEncoding, final String header, final String high, final String normal, final String low) {
        super(owner, new DetailFactory(), container.getAnnotations(priority), priority, defaultEncoding, header);

        taskTagsHandler = new TaskTagsHandler(high, normal, low, container);
    }
    // CHECKSTYLE:ON

    /** {@inheritDoc} */
    @Override
    public Object getDynamic(final String link, final StaplerRequest request, final StaplerResponse response) {
        return new TasksDetailBuilder().getDynamic(link, getOwner(), getContainer(), getDefaultEncoding(), getDisplayName(),
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

