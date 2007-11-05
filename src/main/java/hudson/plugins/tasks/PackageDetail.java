package hudson.plugins.tasks;

import hudson.plugins.tasks.model.JavaPackage;
import hudson.plugins.tasks.model.WorkspaceFile;
import hudson.plugins.tasks.util.SourceDetail;

import java.util.Collection;

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
        super(root, javaPackage.getAnnotations());

        addAnnotations(javaPackage.getAnnotations());

        this.javaPackage = javaPackage;
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return javaPackage.getName();
    }

    /**
     * Gets the files of this package that have open tasks.
     *
     * @return the files
     */
    public Collection<WorkspaceFile> getFiles() {
        return javaPackage.getFiles();
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
     * @return the dynamic result of the tasks analysis (detail page for a
     *         package).
     */
    public Object getDynamic(final String link, final StaplerRequest request, final StaplerResponse response) {
        return new SourceDetail(getOwner(), getAnnotation(link));
    }
}

