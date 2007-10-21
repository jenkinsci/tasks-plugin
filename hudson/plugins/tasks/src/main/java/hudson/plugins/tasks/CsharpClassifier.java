package hudson.plugins.tasks;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;

/**
 * Classifies a C# workspace file. No module is assigned.
 */
public class CsharpClassifier implements FileClassifier {

    /** {@inheritDoc} */
    public boolean accepts(final String fileName) {
        return fileName.endsWith(".cs");
    }

    /** {@inheritDoc} */
    public void classify(final WorkspaceFile file, final InputStream stream) throws IOException {
        try {
            LineIterator iterator = IOUtils.lineIterator(stream, "UTF-8");
            while (iterator.hasNext()) {
                String line = iterator.nextLine();
                if (line.matches("^namespace .*$")) {
                    if (line.contains("{")) {
                        file.setPackageName(StringUtils.substringBetween(line, " ", "{").trim());
                    }
                    else {
                        file.setPackageName(StringUtils.substringAfter(line, " ").trim());
                    }
                    break;
                }
            }
        }
        finally {
            IOUtils.closeQuietly(stream);
        }
    }

}

