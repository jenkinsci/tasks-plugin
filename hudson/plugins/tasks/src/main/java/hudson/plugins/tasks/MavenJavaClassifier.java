package hudson.plugins.tasks;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;

/**
 * Classifies a Java workspace file that is part of a maven module. This
 * classification is used to group the workspace files. This class uses the
 * classification properties "package" and "module".
 */
public class MavenJavaClassifier {
    /** Maven module classification. */
    private static final String MODULE_CLASSIFICATION = "module";
    /** Java package classification. */
    private static final String PACKAGE_CLASSIFICATION = "package";

    /**
     * Classifies the specified workspace file. The provided stream is closed
     * afterwards.
     *
     * @param file
     *            the workspace file model
     * @param stream
     *            the content of the workspace file
     * @throws IOException
     *             if the file could not be read
     */
    public void classify(final WorkspaceFile file, final InputStream stream) throws IOException {
        try {
            LineIterator iterator = IOUtils.lineIterator(stream, "UTF-8");
            while (iterator.hasNext()) {
                String line = iterator.nextLine();
                if (line.matches("^package .*;$")) {
                    file.setPackageName(StringUtils.substringBetween(line, " ", ";").trim());
                    break;
                }
            }
        }
        finally {
            IOUtils.closeQuietly(stream);
        }
        String module = StringUtils.substringBefore(file.getName(), "/src/");
        if (module.contains("/")) {
            module = StringUtils.substringAfterLast(module, "/");
        }
        if (StringUtils.isNotBlank(module)) {
            file.setModuleName(module);
        }
    }
}

