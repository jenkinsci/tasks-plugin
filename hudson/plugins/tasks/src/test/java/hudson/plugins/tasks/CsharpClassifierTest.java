package hudson.plugins.tasks;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

/**
 * Tests the class {@link CsharpClassifier}.
 */
public class CsharpClassifierTest {
    /**
     * Checks the classification for the specified file name.
     *
     * @throws IOException in case of an error
     */
    @Test
    public void checkClassification() throws IOException {
        String fileName = "ActionBinding.cs";
        InputStream stream = CsharpClassifierTest.class.getResourceAsStream(fileName);
        WorkspaceFile file = new TaskScanner().scan(stream);
        stream.close();
        stream = CsharpClassifierTest.class.getResourceAsStream(fileName);
        file.setName(fileName);
        CsharpClassifier classifier = new CsharpClassifier();
        classifier.classify(file, stream);

        assertEquals("Wrong namespace name guessed.", "Avaloq.SmartClient.Utilities", file.getPackageName());
    }

}

