package hudson.plugins.tasks;

import java.io.IOException;
import java.io.InputStream;

/**
 * Classifies a workspace file. A module and package should be assigned to each
 * workspace file.
 */
public interface FileClassifier {
    /**
     * Classifies the specified workspace file. A module and package should be
     * assigned to each workspace file. The provided stream must be closed
     * afterwards.
     *
     * @param file
     *            the workspace file model
     * @param stream
     *            the content of the workspace file
     * @throws IOException
     *             if the file could not be read
     */
    void classify(final WorkspaceFile file, final InputStream stream) throws IOException;

    /**
     * Returns whether this classifier accepts the specified file for
     * processing.
     *
     * @param fileName
     *            the file name
     * @return <code>true</code> if the classifier accepts the specified file
     *         for processing.
     */
    boolean accepts(String fileName);
}
