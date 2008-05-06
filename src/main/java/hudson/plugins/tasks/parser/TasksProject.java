package hudson.plugins.tasks.parser;

import hudson.plugins.tasks.util.model.JavaProject;

/**
 * Remembers the number of scanned files in a {@link JavaProject}.
 */
public class TasksProject extends JavaProject {
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
    public TasksProject(final int numberOfFiles) {
        super();

        this.numberOfFiles = numberOfFiles;
    }

    /**
     * Creates a new instance of <code>TasksProject</code>.
     */
    public TasksProject() {
        super();

        numberOfFiles = 0;
    }

    /**
     * Rebuilds the priorities mapping.
     *
     * @return the created object
     */
    private Object readResolve() {
        rebuildMappings(false);
        return this;
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

