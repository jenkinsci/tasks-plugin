package hudson.plugins.tasks;

import java.util.Collection;

import hudson.model.Run;

import hudson.plugins.analysis.util.model.AnnotationContainer;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.views.DetailFactory;
import hudson.plugins.analysis.views.FixedWarningsDetail;
import hudson.plugins.analysis.views.TabDetail;

/**
 * Creates detail objects for the selected element of a tasks container.
 *
 * @author Ulli Hafner
 */
public class TasksDetailBuilder extends DetailFactory {
    @Override
    protected void attachLabelProvider(final AnnotationContainer container) {
        container.setLabelProvider(new TaskAnnotationsLabelProvider(container.getPackageCategoryTitle()));
    }

    @Override
    protected TabDetail createTabDetail(final Run<?, ?> owner,
            final Collection<FileAnnotation> annotations, final String url, final String defaultEncoding) {
        return new TasksTabDetail(owner, annotations, url, defaultEncoding);
    }

    @Override
    protected FixedWarningsDetail createFixedWarningsDetail(final Run<?, ?> owner,
            final Collection<FileAnnotation> fixedAnnotations, final String defaultEncoding, final String displayName) {
        return new FixedTasksDetail(owner, fixedAnnotations, defaultEncoding, displayName);
    }
}

