package hudson.plugins.tasks;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;

import hudson.plugins.analysis.views.WarningsCountColumn;

import hudson.views.ListViewColumnDescriptor;

/**
 * A column that shows the total number of open tasks in a job.
 *
 * @author Ulli Hafner
 */
public class TasksColumn extends WarningsCountColumn<TasksProjectAction> {
    /**
     * Creates a new instance of {@link TasksColumn}.
     */
    @DataBoundConstructor
    public TasksColumn() { // NOPMD: data binding
        super();
    }

    @Override
    protected Class<TasksProjectAction> getProjectAction() {
        return TasksProjectAction.class;
    }

    @Override
    public String getColumnCaption() {
        return Messages.Tasks_Warnings_ColumnHeader();
    }

    /**
     * Descriptor for the column.
     */
    @Extension
    public static class ColumnDescriptor extends ListViewColumnDescriptor {
        @Override
        public boolean shownByDefault() {
            return false;
        }

        @Override
        public String getDisplayName() {
            return Messages.Tasks_Warnings_Column();
        }
    }
}
