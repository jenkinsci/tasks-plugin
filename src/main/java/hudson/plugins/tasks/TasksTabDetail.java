package hudson.plugins.tasks;

import hudson.model.AbstractBuild;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.views.DetailFactory;
import hudson.plugins.analysis.views.TabDetail;

import java.util.Collection;

/**
 * Result object representing a dynamic tab of the tasks plug-in.
 *
 * @author Ulli Hafner
 */
public class TasksTabDetail extends TabDetail {
    /** Unique ID of this class. */
    private static final long serialVersionUID = 8964198520312051468L;

    /**
     * Creates a new instance of <code>ModuleDetail</code>.
     *
     * @param owner
     *            current build as owner of this action.
     * @param annotations
     *            the container to show the details for
     * @param url
     *            URL to render the content of this tab
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     */
    public TasksTabDetail(final AbstractBuild<?, ?> owner, final Collection<FileAnnotation> annotations, final String url, final String defaultEncoding) {
        super(owner, new DetailFactory(), annotations, url, defaultEncoding);
    }

    /** {@inheritDoc} */
    @Override
    public String getDetails() {
        return "tasks-details.jelly";
    }

    /** {@inheritDoc} */
    @Override
    public String getWarnings() {
        return "tasks-warnings.jelly";
    }
}

