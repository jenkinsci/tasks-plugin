package hudson.plugins.tasks;

import java.io.Serializable;

import hudson.model.Build;
import hudson.model.ModelObject;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * Provides common functionality of the different kind of tasks results details.
 */
public abstract class AbstractTasksResult implements ModelObject, Serializable {
    /** The current build as owner of this action. */
    @SuppressWarnings("Se")
    private final Build<?, ?> owner;

    /**
     * Creates a new instance of <code>AbstractTasksDetail</code>.
     *
     * @param owner
     *            the current build as owner of this result object
     */
    public AbstractTasksResult(final Build<?, ?> owner) {
        this.owner = owner;
    }

    /**
     * Returns the current build as owner of this result object.
     *
     * @return the owner of this details object
     */
    public final Build<?, ?> getOwner() {
        return owner;
    }

    /**
     * Returns whether this result object belongs to the last build.
     *
     * @return <code>true</code> if this result belongs to the last build
     */
    public final boolean isCurrent() {
        return getOwner().getProject().getLastBuild().number == getOwner().number;
    }
}
