package hudson.plugins.tasks.parser;

import hudson.plugins.tasks.util.model.WorkspaceFile;

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
    /** Predefined module name. */
    private final String moduleName;

    /**
     * Creates a new instance of <code>MavenJavaClassifier</code>.
     */
    public MavenJavaClassifier() {
        moduleName = null;
    }

    /**
     * Creates a new instance of <code>MavenJavaClassifier</code>.
     *
     * @param moduleName predefined maven module name
     */
    public MavenJavaClassifier(final String moduleName) {
        this.moduleName = moduleName;
    }

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
        if (moduleName == null) {
            guessModuleName(file);
        }
        else {
            file.setModuleName(moduleName);
        }
    }

    /**
     * Guesses a maven module name based on the source folder.
     *
     * @param file the file to guess the module for
     */
    private void guessModuleName(final WorkspaceFile file) {
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

