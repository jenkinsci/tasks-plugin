package hudson.plugins.tasks;


import hudson.plugins.analysis.util.AbstractHealthDescriptor;
import hudson.plugins.analysis.util.HealthDescriptor;
import hudson.plugins.analysis.util.model.AnnotationProvider;

import org.jvnet.localizer.Localizable;

/**
 * A health descriptor for FindBugs build results.
 *
 * @author Ulli Hafner
 */
public class TasksHealthDescriptor extends AbstractHealthDescriptor {
    /** Unique ID of this class. */
    private static final long serialVersionUID = -3404826986876607396L;

    /**
     * Creates a new instance of {@link TasksHealthDescriptor} based on the
     * values of the specified descriptor.
     *
     * @param healthDescriptor the descriptor to copy the values from
     */
    public TasksHealthDescriptor(final HealthDescriptor healthDescriptor) {
        super(healthDescriptor);
    }

    /** {@inheritDoc} */
    @Override
    protected Localizable createDescription(final AnnotationProvider result) {
        if (result.getNumberOfAnnotations() == 0) {
            return Messages._Tasks_ResultAction_HealthReportNoItem();
        }
        else if (result.getNumberOfAnnotations() == 1) {
            return Messages._Tasks_ResultAction_HealthReportSingleItem();
        }
        else {
            return Messages._Tasks_ResultAction_HealthReportMultipleItem(result.getNumberOfAnnotations());
        }
    }
}

