package hudson.plugins.tasks;

import hudson.model.Build;
import hudson.model.ModelObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * Represents the details of a Java package.
 */
public class PackageDetail implements ModelObject, Serializable {
    /** The current build as owner of this object. */
    @SuppressWarnings("Se")
    private final TasksResult tasksResult;
    /** The selected package to show. */
    private final JavaPackage javaPackage;
    /** The current build as owner of this action. */
    @SuppressWarnings("Se")
    private final Build<?, ?> owner;

    /**
     * Creates a new instance of <code>TaskDetail</code>.
     *
     * @param owner
     *            the current build as owner of this action
     * @param tasksResult
     *            the current build as owner of this action
     * @param javaPackage
     *            the selected package to show
     */
    public PackageDetail(final Build<?,?> owner, final TasksResult tasksResult, final JavaPackage javaPackage) {
        this.owner = owner;
        this.tasksResult = tasksResult;
        this.javaPackage = javaPackage;
    }

    /**
     * Returns the owner.
     *
     * @return the owner
     */
    public TasksResult getTasksResult() {
        return tasksResult;
    }

    /**
     * Returns the owner.
     *
     * @return the owner
     */
    public Build<?,?> getOwner() {
        return owner;
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
     * Returns the defined priorities.
     *
     * @return the defined priorities.
     */
    public List<String> getPriorities() {
        List<String> priorities = new ArrayList<String>();
        for (String priority : tasksResult.getPriorities()) {
            if (getNumberOfTasks(priority) > 0) {
                priorities.add(priority);
            }
        }
        return priorities;
    }

    /**
     * Returns the tags for the specified priority.
     *
     * @param priority
     *            the priority
     * @return the tags for priority high
     */
    public String getTags(final String priority) {
        return tasksResult.getTags(priority);
    }

    /**
     * Returns the total number of tasks in this project.
     *
     * @return total number of tasks in this project.
     */
    public int getNumberOfTasks() {
        return javaPackage.getNumberOfTasks();
    }

    /**
     * Returns the number of tasks with the specified priority in this project.
     *
     * @param  priority the priority
     *
     * @return the number of tasks with the specified priority in this project.
     */
    public int getNumberOfTasks(final String priority) {
        return javaPackage.getNumberOfTasks(priority);
    }

    /**
     * Returns the dynamic result of the FindBugs analysis (detail page for a package).
     *
     * @param link the link to the source code
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @return the dynamic result of the FindBugs analysis (detail page for a package).
     */
    public Object getDynamic(final String link, final StaplerRequest request, final StaplerResponse response) {
        return new TaskDetail(tasksResult.getOwner(), link);
    }

    /**
     * Returns whether this result belongs to the last build.
     *
     * @return <code>true</code> if this result belongs to the last build
     */
    public boolean isCurrent() {
        return tasksResult.getOwner().getProject().getLastBuild().number == tasksResult.getOwner().number;
    }
}

