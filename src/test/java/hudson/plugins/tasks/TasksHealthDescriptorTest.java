package hudson.plugins.tasks;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import hudson.plugins.tasks.util.AbstractEnglishLocaleTest;
import hudson.plugins.tasks.util.NullHealthDescriptor;
import hudson.plugins.tasks.util.model.AnnotationProvider;

import org.junit.Test;
import org.jvnet.localizer.Localizable;

/**
 * Tests the class {@link TasksHealthDescriptor}.
 *
 * @author Ulli Hafner
 */
public class TasksHealthDescriptorTest extends AbstractEnglishLocaleTest {
    /**
     * Verifies the different messages if the number of items are 0, 1, and 2.
     */
    @Test
    public void verifyNumberOfItems() {
        AnnotationProvider provider = mock(AnnotationProvider.class);
        TasksHealthDescriptor healthDescriptor = new TasksHealthDescriptor(NullHealthDescriptor.NULL_HEALTH_DESCRIPTOR);

        Localizable description = healthDescriptor.createDescription(provider);
        assertEquals(Messages.Tasks_ResultAction_HealthReportNoItem(), description.toString());

        stub(provider.getNumberOfAnnotations()).toReturn(1);
        description = healthDescriptor.createDescription(provider);
        assertEquals(Messages.Tasks_ResultAction_HealthReportSingleItem(), description.toString());

        stub(provider.getNumberOfAnnotations()).toReturn(2);
        description = healthDescriptor.createDescription(provider);
        assertEquals(Messages.Tasks_ResultAction_HealthReportMultipleItem(2), description.toString());
    }
}

