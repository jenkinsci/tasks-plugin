package hudson.plugins.tasks;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.QueryParameter;

import hudson.Extension;
import hudson.plugins.analysis.core.PluginDescriptor;
import hudson.plugins.tasks.parser.Task;
import hudson.plugins.tasks.parser.TaskScanner;
import hudson.util.FormValidation;

/**
 * Descriptor for the class {@link TasksPublisher}. Used as a singleton. The
 * class is marked as public so that it can be accessed from views.
 *
 * @author Ulli Hafner
 */
@Extension(ordinal = 100) @Symbol("openTasks")
public final class TasksDescriptor extends PluginDescriptor {
    private static final String ICONS_PREFIX = "/plugin/tasks/icons/";
    /** The ID of this plug-in is used as URL. */
    static final String PLUGIN_ID = "tasks";
    /** The URL of the result action. */
    static final String RESULT_URL = PluginDescriptor.createResultUrlName(PLUGIN_ID);
    /** Icon to use for the result and project action. */
    static final String ICON_URL = ICONS_PREFIX + "tasks-24x24.png";

    /**
     * Creates a new instance of {@link TasksDescriptor}.
     */
    public TasksDescriptor() {
        super(TasksPublisher.class);
    }

    @Override
    public String getDisplayName() {
        return Messages.Tasks_Publisher_Name();
    }

    @Override
    public String getPluginName() {
        return PLUGIN_ID;
    }

    @Override
    public String getIconUrl() {
        return ICON_URL;
    }

    @Override
    public String getSummaryIconUrl() {
        return ICONS_PREFIX + "tasks-48x48.png";
    }

    /**
     * Validates the example text that will be scanned for open tasks.
     *
     * @param example the text to be scanned for open tasks
     * @param high
     *            tag identifiers indicating high priority
     * @param normal
     *            tag identifiers indicating normal priority
     * @param low
     *            tag identifiers indicating low priority
     * @param ignoreCase
     *            if case should be ignored during matching
     * @param asRegexp
     *            if the identifiers should be treated as regular expression
     * @return validation result
     * @throws IOException if an error occurs
     */
    public FormValidation doCheckExample(@QueryParameter final String example,
                                         @QueryParameter final String high,
                                         @QueryParameter final String normal,
                                         @QueryParameter final String low,
                                         @QueryParameter final boolean ignoreCase,
                                         @QueryParameter final boolean asRegexp) throws IOException {
        if (StringUtils.isEmpty(example)) {
            return FormValidation.ok();
        }

        TaskScanner scanner = new TaskScanner(high, normal, low, ignoreCase, asRegexp);
        if (scanner.isInvalidPattern()) {
            return  FormValidation.error(scanner.getErrorMessage());
        }

        Collection<Task> tasks = scanner.scan(new StringReader(example));
        if (tasks.isEmpty()) {
            return FormValidation.warning(Messages.Validation_NoTask());
        }
        else if (tasks.size() != 1) {
            return FormValidation.warning(Messages.Validation_MultipleTasks(tasks.size()));
        }
        else {
            Task task = tasks.iterator().next();
            return FormValidation.ok(Messages.Validation_OneTask(task.getType(),
                    task.getDetailMessage()));
        }
    }
}