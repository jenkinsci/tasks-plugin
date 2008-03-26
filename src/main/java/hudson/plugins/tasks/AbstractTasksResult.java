package hudson.plugins.tasks;

import hudson.model.AbstractBuild;
import hudson.plugins.tasks.util.AbstractAnnotationsDetail;
import hudson.plugins.tasks.util.model.FileAnnotation;
import hudson.plugins.tasks.util.model.Priority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * Base class for tasks detail objects.
 */
public abstract class AbstractTasksResult extends AbstractAnnotationsDetail {
    /** Tag identifiers indicating high priority. */
    private final String high;
    /** Tag identifiers indicating normal priority. */
    private final String normal;
    /** Tag identifiers indicating low priority. */
    private final String low;

    /**
     * Creates a new instance of <code>AbstractTasksDetail</code>.
     *
     * @param owner
     *            the current build as owner of this result object
     * @param high
     *            tag identifiers indicating high priority
     * @param normal
     *            tag identifiers indicating normal priority
     * @param low
     *            tag identifiers indicating low priority
     * @param annotations
     *            all the files that contain tasks
     */
    public AbstractTasksResult(final AbstractBuild<?, ?> owner, final Collection<FileAnnotation> annotations,
            final String high, final String normal, final String low) {
        super(owner, annotations);

        this.high = high;
        this.normal = normal;
        this.low = low;
    }

    /**
     * Returns a localized priority name.
     *
     * @param priorityName
     *            priority as String value
     * @return localized priority name
     */
    public String getLocalizedPriority(final String priorityName) {
        return Priority.fromString(priorityName).getLongLocalizedString();
    }

    /**
     * Returns the package category name for the scanned files. Currently, only
     * java and c# files are supported.
     *
     * @return the package category name for the scanned files
     */
    public String getPackageCategoryName() {
        if (hasAnnotations()) {
            String fileName = getAnnotations().iterator().next().getFileName();
            if (fileName.endsWith(".cs")) {
                return Messages.Tasks_ResultAction_Category_Namespace();
            }
        }
        return Messages.Tasks_ResultAction_Category_Package();
    }

    /**
     * Returns the actually used priorities.
     *
     * @return the actually used priorities.
     */
    public List<String> getPriorities() {
        List<String> actualPriorities = new ArrayList<String>();
        for (String priority : getAvailablePriorities()) {
            if (getNumberOfAnnotations(priority) > 0) {
                actualPriorities.add(priority);
            }
        }
        return actualPriorities;
    }

    /**
     * Returns the defined priorities.
     *
     * @return the defined priorities.
     */
    public Collection<String> getAvailablePriorities() {
        ArrayList<String> priorities = new ArrayList<String>();
        if (StringUtils.isNotEmpty(high)) {
            priorities.add(StringUtils.capitalize(StringUtils.lowerCase(Priority.HIGH.name())));
        }
        if (StringUtils.isNotEmpty(normal)) {
            priorities.add(StringUtils.capitalize(StringUtils.lowerCase(Priority.NORMAL.name())));
        }
        if (StringUtils.isNotEmpty(low)) {
            priorities.add(StringUtils.capitalize(StringUtils.lowerCase(Priority.LOW.name())));
        }
        return priorities;
    }

    /**
     * Returns the tags for the specified priority.
     *
     * @param priority the priority
     *
     * @return the tags for the specified priority
     */
    public final String getTags(final String priority) {
        return getTags(Priority.fromString(priority));
    }

    /**
     * Returns the tags for the specified priority.
     *
     * @param priority
     *            the priority
     * @return the tags for the specified priority
     */
    public final String getTags(final Priority priority) {
        if (priority == Priority.HIGH) {
            return high;
        }
        else if (priority == Priority.NORMAL) {
            return normal;
        }
        else {
            return low;
        }
    }
}
