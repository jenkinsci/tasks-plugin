package hudson.plugins.tasks.parser;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

/**
 *  Tests the class {@link JavaPackageDetector}.
 */
public class JavaPackageDetectorTest {
    /** The classifier under test. */
    private JavaPackageDetector classifier = new JavaPackageDetector();

    /**
     * Checks whether we could identify a java package name.
     *
     * @throws IOException
     *             in case of an error
     */
    @Test
    public void checkPackage() throws IOException {
        InputStream stream;
        stream = JavaPackageDetectorTest.class.getResourceAsStream("MavenJavaTest.txt");
        String packageName = classifier.detectPackageName(stream);

        assertEquals("Wrong package name guessed.", "hudson.plugins.tasks.util", packageName);
    }

    /**
     * Checks whether we do not detect a namespace in a text file.
     *
     * @throws IOException
     *             in case of an error
     */
    @Test
    public void checkEmptyPackageName() throws IOException {
        String fileName = "file-without-tasks.txt";
        InputStream stream = JavaPackageDetectorTest.class.getResourceAsStream(fileName);

        assertEquals("Wrong namespace name guessed.", StringUtils.EMPTY,
                classifier.detectPackageName(stream));
    }

    /**
     * Checks whether we correctly accept C# files.
     */
    @Test
    public void testFileSuffix() {
        assertTrue("Does not accept a Java file.", classifier.accepts("Action.java"));
        assertFalse("Accepts a non-Java file.", classifier.accepts("Action.java.old"));
    }
}
