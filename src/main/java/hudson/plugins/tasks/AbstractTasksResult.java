package hudson.plugins.tasks;

import hudson.model.AbstractBuild;
import hudson.model.ModelObject;
import hudson.plugins.tasks.util.ChartBuilder;
import hudson.plugins.tasks.util.model.AnnotationContainer;
import hudson.plugins.tasks.util.model.AnnotationProvider;
import hudson.plugins.tasks.util.model.FileAnnotation;
import hudson.plugins.tasks.util.model.Priority;
import hudson.plugins.tasks.util.model.WorkspaceFile;
import hudson.util.ChartUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jfree.chart.JFreeChart;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * Provides common functionality of the different kind of tasks results details.
 */
public abstract class AbstractTasksResult implements ModelObject, AnnotationProvider {
    /** The current build as owner of this action. */
    @SuppressWarnings("Se")
    private final AbstractBuild<?, ?> owner;

    /** Tag identifiers indicating high priority. */
    private final String high;

    /** Tag identifiers indicating normal priority. */
    private final String normal;

    /** Tag identifiers indicating low priority. */
    private final String low;

    /** The annotation container. */
    private transient AnnotationContainer annotationContainer;

    /**
     * Creates a new instance of <code>AbstractTasksDetail</code>.
     *
     * @param owner the current build as owner of this result object
     * @param high tag identifiers indicating high priority
     * @param normal tag identifiers indicating normal priority
     * @param low tag identifiers indicating low priority
     * @param annotations all the files that contain tasks
     */
    public AbstractTasksResult(final AbstractBuild<?, ?> owner, final String high, final String normal, final String low, final Collection<FileAnnotation> annotations) {
        this.owner = owner;
        this.high = high;
        this.normal = normal;
        this.low = low;

        annotationContainer = new AnnotationContainer();
        annotationContainer.addAnnotations(annotations);
    }

    /**
     * Creates a new instance of <code>AbstractTasksResult</code>.
     *
     * @param root the root result object that is used to get the available tasks
     * @param annotations the annotations of the child
     */
    public AbstractTasksResult(final AbstractTasksResult root, final Collection<FileAnnotation> annotations) {
        this(root.owner, root.high, root.normal, root.low, annotations);
    }

    /**
     * Returns the package category name for the scanned files. Currently, only
     * java and c# files are supported.
     *
     * @return the package category name for the scanned files
     */
    public String getPackageCategoryName() {
        if (hasAnnotations()) {
            WorkspaceFile file = getAnnotations().iterator().next().getWorkspaceFile();
            if (file.getShortName().endsWith(".cs")) {
                return "Namespace";
            }
        }
        return "Package";
    }

    /**
     * FIXME: Document method getAnnotations.
     *
     * @param annotations the annotations
     */
    protected final void setAnnotations(final Collection<FileAnnotation> annotations) {
        annotationContainer = new AnnotationContainer();
        annotationContainer.addAnnotations(annotations);
    }

    /**
     * Returns the current build as owner of this result object.
     *
     * @return the owner of this details object
     */
    public final AbstractBuild<?, ?> getOwner() {
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

    /**
     * Returns the actually used priorities.
     *
     * @return the actually used priorities.
     */
    public List<String> getPriorities() {
        List<String> actualPriorities = new ArrayList<String>();
        for (String priority : getAvailablePriorities()) {
            if (getNumberOfAnnotations(priority) > 0) {
                actualPriorities.add(priority);
            }
        }
        return actualPriorities;
    }

    /**
     * Returns the defined priorities.
     *
     * @return the defined priorities.
     */
    public Collection<String> getAvailablePriorities() {
        ArrayList<String> priorities = new ArrayList<String>();
        if (StringUtils.isNotEmpty(high)) {
            priorities.add(StringUtils.capitalize(StringUtils.lowerCase(Priority.HIGH.name())));
        }
        if (StringUtils.isNotEmpty(normal)) {
            priorities.add(StringUtils.capitalize(StringUtils.lowerCase(Priority.NORMAL.name())));
        }
        if (StringUtils.isNotEmpty(low)) {
            priorities.add(StringUtils.capitalize(StringUtils.lowerCase(Priority.LOW.name())));
        }
        return priorities;
    }

    /**
     * Returns the tags for the specified priority.
     *
     * @param priority the priority
     *
     * @return the tags for priority high
     */
    public final String getTags(final String priority) {
        Priority converted = Priority.valueOf(StringUtils.upperCase(priority));
        if (converted == Priority.HIGH) {
            return high;
        }
        else if (converted == Priority.NORMAL) {
            return normal;
        }
        else {
            return low;
        }
    }

    /**
     * Creates a detail graph for the specified detail object.
     *
     * @param request Stapler request
     * @param response Stapler response
     * @param detailObject the detail object to compute the graph for
     * @param upperBound the upper bound of all tasks
     *
     * @throws IOException in case of an error
     */
    protected final void createDetailGraph(final StaplerRequest request, final StaplerResponse response,
            final AnnotationProvider detailObject, final int upperBound) throws IOException {
        if (ChartUtil.awtProblem) {
            response.sendRedirect2(request.getContextPath() + "/images/headless.png");
            return;
        }
        JFreeChart chart = ChartBuilder.createHighNormalLowChart(
                detailObject.getNumberOfAnnotations(Priority.HIGH),
                detailObject.getNumberOfAnnotations(Priority.NORMAL),
                detailObject.getNumberOfAnnotations(Priority.LOW), upperBound);
        ChartUtil.generateGraph(request, response, chart, 400, 20);
    }

    /** {@inheritDoc} */
    public final FileAnnotation getAnnotation(final long key) {
        return annotationContainer.getAnnotation(key);
    }

    /** {@inheritDoc} */
    public final FileAnnotation getAnnotation(final String key) {
        return annotationContainer.getAnnotation(key);
    }

    /** {@inheritDoc} */
    public final Collection<FileAnnotation> getAnnotations() {
        return annotationContainer.getAnnotations();
    }

    /** {@inheritDoc} */
    public final Collection<FileAnnotation> getAnnotations(final Priority priority) {
        return annotationContainer.getAnnotations(priority);
    }

    /** {@inheritDoc} */
    public final Collection<FileAnnotation> getAnnotations(final String priority) {
        return annotationContainer.getAnnotations(priority);
    }

    /** {@inheritDoc} */
    public int getNumberOfAnnotations() {
        return annotationContainer.getNumberOfAnnotations();
    }

    /** {@inheritDoc} */
    public int getNumberOfAnnotations(final Priority priority) {
        return annotationContainer.getNumberOfAnnotations(priority);
    }

    /** {@inheritDoc} */
    public int getNumberOfAnnotations(final String priority) {
        return annotationContainer.getNumberOfAnnotations(priority);
    }

    /** {@inheritDoc} */
    public final boolean hasAnnotations() {
        return annotationContainer.hasAnnotations();
    }

    /** {@inheritDoc} */
    public final boolean hasAnnotations(final Priority priority) {
        return annotationContainer.hasAnnotations(priority);
    }

    /** {@inheritDoc} */
    public final boolean hasAnnotations(final String priority) {
        return annotationContainer.hasAnnotations(priority);
    }
}
