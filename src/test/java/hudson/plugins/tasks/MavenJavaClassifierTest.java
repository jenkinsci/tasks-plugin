package hudson.plugins.tasks;

import static org.junit.Assert.*;
import hudson.plugins.tasks.Task.Priority;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

/**
 *  Tests the class {@link MavenJavaClassifier}.
 */
public class MavenJavaClassifierTest {
    /**
     * Checks whether we could identify a java package name and maven module.
     *
     * @throws IOException
     *             in case of an error
     */
    @Test
    public void checkPackage() throws IOException {
        InputStream stream = MavenJavaClassifierTest.class.getResourceAsStream("MavenJavaTest.txt");
        WorkspaceFile file = new TaskScanner().scan(stream);
        stream.close();

        assertEquals("Wrong FIXME count.", 1, file.getNumberOfTasks(Priority.HIGH));

        checkClassification(file, "com.avaloq.adt.core/src/com/avaloq/adt/core/job/AvaloqJob.java");
        checkClassification(file, "base/com.hello.world/com.avaloq.adt.core/src/com/avaloq/adt/core/job/AvaloqJob.java");
    }

    /**
     * Checks the classification for the specified file name.
     *
     * @param file
     *            the workspace file
     * @param fileName
     *            the file name
     * @throws IOException in case of an error
     */
    private void checkClassification(final WorkspaceFile file, final String fileName) throws IOException {
        InputStream stream;
        stream = MavenJavaClassifierTest.class.getResourceAsStream("MavenJavaTest.txt");
        file.setName(fileName);
        MavenJavaClassifier classifier = new MavenJavaClassifier();
        classifier.classify(file, stream);

        assertEquals("Wrong packag name guessed.", "hudson.plugins.tasks.util", file.getProperty("package"));
        assertEquals("Wrong module name guessed", "com.avaloq.adt.core", file.getProperty("module"));
    }
}
