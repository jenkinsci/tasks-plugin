package hudson.plugins.tasks;

import hudson.plugins.analysis.util.model.AnnotationsLabelProvider;

/**
 * A label provider with a different 'warnings' tab.
 *
 * @author Ullrich Hafner
 */
public class TaskAnnotationsLabelProvider extends AnnotationsLabelProvider {
    public TaskAnnotationsLabelProvider(final String packageCategoryTitle) {
        super(packageCategoryTitle);
    }

    @Override
    public String getWarnings() {
        return Messages.Tasks_ProjectAction_Name();
    }
}
