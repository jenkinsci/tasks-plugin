package hudson.plugins.tasks.parser;

import hudson.plugins.tasks.util.model.JavaProject;

/**
 * Remembers the number of scanned files in a {@link JavaProject}.
 */
public class TasksProject extends JavaProject {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 4904609926196858707L;

    /** The number of scanned files in this project. */
    private final int numberOfFiles;

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
    public int getNumberOfFiles() {
        return numberOfFiles;
    }
}

