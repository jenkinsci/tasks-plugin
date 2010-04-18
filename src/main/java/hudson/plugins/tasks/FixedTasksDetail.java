package hudson.plugins.tasks;

import hudson.model.AbstractBuild;
import hudson.plugins.analysis.util.model.DefaultAnnotationContainer;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;
import hudson.plugins.analysis.views.DetailFactory;
import hudson.plugins.analysis.views.FixedWarningsDetail;

import java.util.Collection;

/**
 * Result object to visualize the fixed tasks in a build.
 *
 * @author Ulli Hafner
 */
public class FixedTasksDetail extends FixedWarningsDetail {
    /** Unique ID of this class. */
    private static final long serialVersionUID = -8592850365611555429L;
    /** Handles the task tags. */
    private final TaskTagsHandler taskTagsHandler;

    /**
     * Creates a new instance of {@link FixedTasksDetail}.
     *
     * @param owner
     *            the current results object as owner of this action
     * @param fixedTasks
     *            all fixed tasks in this build
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
    public FixedTasksDetail(final AbstractBuild<?, ?> owner, final Collection<FileAnnotation> fixedTasks, final String defaultEncoding, final String header,
            final String high, final String normal, final String low) {
        super(owner, new DetailFactory(), fixedTasks, defaultEncoding, header);

        taskTagsHandler = new TaskTagsHandler(high, normal, low, new DefaultAnnotationContainer(fixedTasks));
    }

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

    /** {@inheritDoc} */
    @Override
    public String getDisplayName() {
        return Messages.FixedTasksDetail_Name();
    }
}

