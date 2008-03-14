package hudson.plugins.tasks.parser;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;

/**
 * Detects the namespace of a C# workspace file.
 *
 * @author Ulli Hafner
 */
public class CsharpNamespaceDetector implements PackageDetector {
    /** {@inheritDoc} */
    public boolean accepts(final String fileName) {
        return fileName.endsWith(".cs");
    }

    /** {@inheritDoc}*/
    public String detectPackageName(final InputStream stream) throws IOException {
        try {
            LineIterator iterator = IOUtils.lineIterator(stream, "UTF-8");
            while (iterator.hasNext()) {
                String line = iterator.nextLine();
                if (line.matches("^namespace .*$")) {
                    if (line.contains("{")) {
                        return StringUtils.substringBetween(line, " ", "{").trim();
                    }
                    else {
                        return StringUtils.substringAfter(line, " ").trim();
                    }
                }
            }
            return StringUtils.EMPTY;
        }
        finally {
            IOUtils.closeQuietly(stream);
        }
    }
}

