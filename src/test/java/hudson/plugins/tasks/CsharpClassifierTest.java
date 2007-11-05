package hudson.plugins.tasks;

import static org.junit.Assert.*;
import hudson.plugins.tasks.model.WorkspaceFile;
import hudson.plugins.tasks.parser.CsharpClassifier;

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

        WorkspaceFile workspaceFile = new WorkspaceFile();
        workspaceFile.setName(fileName);

        CsharpClassifier classifier = new CsharpClassifier();
        classifier.classify(workspaceFile, stream);

        assertEquals("Wrong namespace name guessed.", "Avaloq.SmartClient.Utilities", workspaceFile.getPackageName());
    }

}

