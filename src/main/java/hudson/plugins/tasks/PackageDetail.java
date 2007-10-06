package hudson.plugins.tasks;

import hudson.plugins.tasks.Task.Priority;

import java.util.Set;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Represents the tasks details of a Java package.
 */
public class PackageDetail extends AbstractTasksResult {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 3082184559129569059L;
    /** The selected package to show. */
    private final JavaPackage javaPackage;

    /**
     * Creates a new instance of <code>PackageDetail</code>.
     *
     * @param root
     *            the root result object that is used to get the available tasks
     * @param javaPackage
     *            the selected package to show
     */
    public PackageDetail(final AbstractTasksResult root, final JavaPackage javaPackage) {
        super(root);

        this.javaPackage = javaPackage;
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return javaPackage.getName();
    }

    /**
     * Returns the files.
     *
     * @return the files
     */
    public Set<WorkspaceFile> getFiles() {
        return javaPackage.getFiles();
    }

    /**
     * Returns the number of tasks with the specified priority in this project.
     *
     * @param  priority the priority
     *
     * @return the number of tasks with the specified priority in this project.
     */
    @Override
    public int getNumberOfTasks(final Priority priority) {
        return javaPackage.getNumberOfTasks(priority);
    }

    /**
     * Returns the dynamic result of this package detail view, which is a task
     * detail object for a single workspace file.
     *
     * @param link
     *            the link containing the path to the selected workspace file
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @return the dynamic result of the FindBugs analysis (detail page for a
     *         package).
     */
    public Object getDynamic(final String link, final StaplerRequest request, final StaplerResponse response) {
        return new TaskDetail(getOwner(), link);
    }
}

