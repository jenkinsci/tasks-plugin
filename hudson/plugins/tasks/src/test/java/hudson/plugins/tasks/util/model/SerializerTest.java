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
 */
public class SerializerTest {
    @Test
    public void testSerialization() throws IOException {
        AnnotationContainer original = createOriginal();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(out);
        oos.writeObject(original);
        oos.close();

        Assert.assertTrue(out.toByteArray().length > 0);
    }

    /**
     * FIXME: Document method createOriginal
     *
     * @return
     */
    private AnnotationContainer createOriginal() {
        final JavaProject project = new JavaProject();
        Task task = new Task(Priority.HIGH, 100, "Test Task");
        task.setFileName("Path/To/File");
        task.setPackageName("Package");
        task.setModuleName("Module");

        project.addAnnotation(task);
        return project;
    }

   @Test
   public void testObjectIsSameAfterDeserialization() throws IOException, ClassNotFoundException {
       AnnotationContainer original = createOriginal();

       Assert.assertTrue(original.hasAnnotations());
       Assert.assertEquals(1, original.getNumberOfAnnotations());
       Assert.assertEquals(1, original.getNumberOfAnnotations(Priority.HIGH));
       Assert.assertEquals(0, original.getNumberOfAnnotations(Priority.NORMAL));

       ByteArrayOutputStream out = new ByteArrayOutputStream();
       ObjectOutputStream oos = new ObjectOutputStream(out);
       oos.writeObject(original);
       oos.close();

       byte[] pickled = out.toByteArray();
       InputStream inputStream = new ByteArrayInputStream(pickled);
       ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
       Object readObject = objectInputStream.readObject();
       AnnotationContainer copy = (AnnotationContainer) readObject;

       Assert.assertEquals(original.hasAnnotations(), copy.hasAnnotations());
       Assert.assertEquals(original.getNumberOfAnnotations(), copy.getNumberOfAnnotations());
       Assert.assertEquals(original.getNumberOfAnnotations(Priority.HIGH), copy.getNumberOfAnnotations(Priority.HIGH));
       Assert.assertEquals(original.getNumberOfAnnotations(Priority.NORMAL), copy.getNumberOfAnnotations(Priority.NORMAL));

       FileAnnotation originalFile = original.getAnnotations().iterator().next();
       FileAnnotation copyFile = copy.getAnnotations().iterator().next();

       Assert.assertSame(originalFile.getPriority(), copyFile.getPriority());
       Assert.assertSame(originalFile.getMessage(), copyFile.getMessage());
   }
}

