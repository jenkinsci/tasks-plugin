package hudson.plugins.tasks;

import hudson.model.AbstractBuild;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.views.DetailFactory;
import hudson.plugins.analysis.views.TabDetail;

import java.util.Collection;

/**
 * Creates detail objects for the selected element of a tasks container.
 *
 * @author Ulli Hafner
 */
public class TasksDetailBuilder extends DetailFactory {
    /** {@inheritDoc} */
    @Override
    protected TabDetail createTabDetail(final AbstractBuild<?, ?> owner,
            final Collection<FileAnnotation> annotations, final String url, final String defaultEncoding) {
        return new TasksTabDetail(owner, annotations, url, defaultEncoding);
    }
}

