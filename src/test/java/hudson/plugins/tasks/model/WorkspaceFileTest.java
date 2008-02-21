package hudson.plugins.tasks.model;

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Test;

/**
 * Tests the deserialization of workspace files.
 */
public class WorkspaceFileTest {
    /** Error message. */
    private static final String ERROR_MESSAGE = "Wrong number of deserialized annotations.";

    /**
     * Test the deserialization of the tasks.
     *
     * @throws URISyntaxException
     *             in case of a setup error
     * @throws IOException
     *             in case of a setup error
     */
    @Test
    public void deserializeFiles() throws URISyntaxException, IOException {
//        AnnotationStream xStream = new AnnotationStream();
//        xStream.alias("task", Task.class);
//        XmlFile xmlFile = new XmlFile(xStream, new File(WorkspaceFileTest.class.getResource("open-tasks.xml").toURI()));
//
//        WorkspaceFile[] files = (WorkspaceFile[])xmlFile.read();
//        Assert.assertEquals(85, files.length);
//
//        JavaProject project = new JavaProject();
//        project.addFiles(Arrays.asList(files));
//
//        Assert.assertEquals(ERROR_MESSAGE, 85, project.getFiles().size());
//        Assert.assertEquals(ERROR_MESSAGE, 125, project.getNumberOfAnnotations());
//        Assert.assertEquals(ERROR_MESSAGE, 19, project.getNumberOfAnnotations(Priority.HIGH));
//        Assert.assertEquals(ERROR_MESSAGE, 83, project.getNumberOfAnnotations(Priority.NORMAL));
//        Assert.assertEquals(ERROR_MESSAGE, 23, project.getNumberOfAnnotations(Priority.LOW));
//
//        Assert.assertEquals(ERROR_MESSAGE, 0, files[0].getNumberOfAnnotations(Priority.HIGH));
//        Assert.assertEquals(ERROR_MESSAGE, 1, files[0].getNumberOfAnnotations(Priority.NORMAL));
//        Assert.assertEquals(ERROR_MESSAGE, 0, files[0].getNumberOfAnnotations(Priority.LOW));
//
//        Assert.assertSame(files[0], files[0].getAnnotations().iterator().next().getWorkspaceFile());
    }
}

