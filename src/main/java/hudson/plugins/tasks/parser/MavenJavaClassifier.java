package hudson.plugins.tasks.parser;

import hudson.plugins.tasks.model.WorkspaceFile;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;

/**
 * Classifies a Java workspace file that is part of a maven module.
 *
 * @author Ulli Hafner
 */
public class MavenJavaClassifier implements FileClassifier {
    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    public boolean accepts(final String fileName) {
        return fileName.endsWith(".java");
    }
}

