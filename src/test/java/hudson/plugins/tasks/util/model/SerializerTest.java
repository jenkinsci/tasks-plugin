package hudson.plugins.tasks.util.model;

import hudson.plugins.tasks.parser.Task;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Tests the serialization of the model.
 *
 * @see <a href="http://www.ibm.com/developerworks/library/j-serialtest.html">Testing object serialization</a>
 */
public class SerializerTest {
    /**
     * Test whether we could serialize the a task.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testSerialization() throws IOException {
        JavaProject original = createOriginal();
        ByteArrayOutputStream out = serialize(original);

        Assert.assertTrue(out.toByteArray().length > 0);
    }

    /**
     * Creates the original object that will be serialized.
     *
     * @return the annotation container
     */
    private JavaProject createOriginal() {
        final JavaProject project = new JavaProject();

        Task task = new Task(Priority.HIGH, 100, "Test Task");
        task.setFileName("Path/To/File");
        task.setPackageName("Package");
        task.setModuleName("Module");

        project.addAnnotation(task);

        verifyProject(project);

        return project;
    }

    /**
     * Verifies the created project.
     *
     * @param project the created project
     */
    private void verifyProject(final JavaProject project) {
        Assert.assertTrue(project.hasAnnotations());
        Assert.assertEquals(1, project.getNumberOfAnnotations());
        Assert.assertEquals(1, project.getNumberOfAnnotations(Priority.HIGH));
        Assert.assertEquals(0, project.getNumberOfAnnotations(Priority.NORMAL));
        Assert.assertEquals(0, project.getNumberOfAnnotations(Priority.LOW));
    }

    /**
     * Test whether a serialized task is the same object after deserialization.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
   @Test
   public void testObjectIsSameAfterDeserialization() throws IOException, ClassNotFoundException {
       JavaProject original = createOriginal();

       ByteArrayOutputStream outputStream = serialize(original);
       JavaProject copy = deserialize(outputStream.toByteArray());

       verifyProject(copy);

       FileAnnotation originalFile = original.getAnnotations().iterator().next();
       FileAnnotation copyFile = copy.getAnnotations().iterator().next();

       Assert.assertSame(originalFile.getPriority(), copyFile.getPriority());
       Assert.assertSame(originalFile.getMessage(), copyFile.getMessage());
   }

    /**
     * Deserializes an object from the specified data and returns it.
     *
     * @param objectData
     *            the serialized object in plain bytes
     * @return the deserialized object
     * @throws IOException
     *             in case of an IO error
     * @throws ClassNotFoundException
     *             if the wrong class is created
     */
    private JavaProject deserialize(final byte[] objectData) throws IOException, ClassNotFoundException {
       InputStream inputStream = new ByteArrayInputStream(objectData);
       ObjectInputStream objectStream = new ObjectInputStream(inputStream);
       Object readObject = objectStream.readObject();

       return (JavaProject) readObject;
    }

    /**
     * Serializes the specified object and returns the created output stream.
     *
     * @param original
     *            original object
     * @return created output stream
     * @throws IOException
     */
    private ByteArrayOutputStream serialize(final JavaProject original) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectStream = new ObjectOutputStream(outputStream);
        objectStream.writeObject(original);
        objectStream.close();

        return outputStream;
    }
}

