package hudson.plugins.tasks.parser;

import hudson.plugins.tasks.util.AnnotationDifferencer;
import hudson.plugins.tasks.util.AnnotationDifferencerTest;
import hudson.plugins.tasks.util.model.FileAnnotation;
import hudson.plugins.tasks.util.model.Priority;

/**
 * Tests the {@link AnnotationDifferencer} for tasks.
 */
public class TasksDifferencerTest extends AnnotationDifferencerTest {
    /** {@inheritDoc} */
    @Override
    public FileAnnotation createAnnotation(final Priority priority, final String message, final String category,
            final String type, final int start, final int end) {
        return new Task(priority, start, message);
    }
}

