package hudson.plugins.tasks.parser;

import hudson.plugins.tasks.util.ParserResult;
import hudson.plugins.tasks.util.model.JavaProject;

/**
 * Remembers the number of scanned files in a {@link JavaProject}.
 *
 * @author Ulli Hafner
 */
public class TasksParserResult extends ParserResult {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 4904609926196858707L;
    /** The number of scanned files in this project. */
    private int numberOfFiles;

    /**
     * Creates a new instance of <code>TasksProject</code>.
     *
     * @param numberOfFiles
     *            the number of scanned files in this project
     */
    public TasksParserResult(final int numberOfFiles) {
        super();

        this.numberOfFiles = numberOfFiles;
    }

    /**
     * Creates a new instance of <code>TasksProject</code>.
     */
    public TasksParserResult() {
        super();

        numberOfFiles = 0;
    }

    /**
     * Returns the number of scanned files in this project.
     *
     * @return the number of scanned files in a {@link JavaProject}
     */
    public int getNumberOfScannedFiles() {
        return numberOfFiles;
    }

    /**
     * Adds the specified number of parsed files to this project.
     *
     * @param newFiles the new files
     */
    public void addScannedFiles(final int newFiles) {
        numberOfFiles += newFiles;
    }
}

