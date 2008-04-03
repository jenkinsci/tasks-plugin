package hudson.plugins.tasks;

import hudson.model.AbstractBuild;
import hudson.plugins.tasks.util.PackageDetail;
import hudson.plugins.tasks.util.model.JavaPackage;
import hudson.plugins.tasks.util.model.Priority;

import java.util.Collection;
import java.util.List;

/**
 * Represents the tasks details of a Java package.
 */
public class TasksPackageDetail extends PackageDetail {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 3082184559129569059L;
    /** Handles the task tags. */
    private final TaskTagsHandler taskTagsHandler;

    /**
     * Creates a new instance of <code>PackageDetail</code>.
     *
     * @param owner
     *            the current build as owner of this result object
     * @param high
     *            tag identifiers indicating high priority
     * @param normal
     *            tag identifiers indicating normal priority
     * @param low
     *            tag identifiers indicating low priority
     * @param javaPackage
     *            the selected Java package to show
     */
    public TasksPackageDetail(final AbstractBuild<?, ?> owner, final JavaPackage javaPackage,
            final String high, final String normal, final String low) {
        super(owner, javaPackage, Messages.Tasks_ProjectAction_Name());

        taskTagsHandler = new TaskTagsHandler(high, normal, low, javaPackage);
    }

    // CHECKSTYLE:OFF - generated delegate -

    public Collection<String> getAvailablePriorities() {
        return taskTagsHandler.getAvailablePriorities();
    }

    public List<String> getPriorities() {
        return taskTagsHandler.getPriorities();
    }

    public final String getTags(final Priority priority) {
        return taskTagsHandler.getTags(priority);
    }

    public final String getTags(final String priority) {
        return taskTagsHandler.getTags(priority);
    }
}

