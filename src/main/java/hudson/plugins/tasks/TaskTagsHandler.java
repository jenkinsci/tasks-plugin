package hudson.plugins.tasks;

import hudson.plugins.tasks.util.model.AnnotationContainer;
import hudson.plugins.tasks.util.model.Priority;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * Handles the tags of the open tasks.
 */
public class TaskTagsHandler implements Serializable {
    /** Unique ID of this class.*/
    private static final long serialVersionUID = 4156585047399976629L;
    /** Tag identifiers indicating high priority. */
    private final String high;
    /** Tag identifiers indicating normal priority. */
    private final String normal;
    /** Tag identifiers indicating low priority. */
    private final String low;
    /** References all the warnings. */
    private final AnnotationContainer provider;

    /**
     * Creates a new instance of <code>TaskTagsHandler</code>.
     *
     * @param high
     *            tag identifiers indicating high priority
     * @param normal
     *            tag identifiers indicating normal priority
     * @param low
     *            tag identifiers indicating low priority
     * @param provider
     *             References all the warnings.
     */
    public TaskTagsHandler(final String high, final String normal, final String low, final AnnotationContainer provider) {
        this.high = high;
        this.normal = normal;
        this.low = low;
        this.provider = provider;
    }

    /**
     * Returns the actually used priorities.
     *
     * @return the actually used priorities.
     */
    public List<String> getPriorities() {
        List<String> actualPriorities = new ArrayList<String>();
        for (String priority : getAvailablePriorities()) {
            if (provider.getNumberOfAnnotations(priority) > 0) {
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
        // FIXME: l10n
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
