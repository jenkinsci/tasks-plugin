package hudson.plugins.tasks;

import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.plugins.util.AbortException;
import hudson.remoting.VirtualChannel;

import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.types.FileSet;

/**
 * Scans the workspace and records the found tasks.
 *
 * @author Ulli Hafner
 */
class WorkspaceScanner implements FileCallable<JavaProject> {
    /** Generated ID. */
    private static final long serialVersionUID = -4355362392102020724L;
    /** Ant file-set pattern to scan for FindBugs files. */
    private final String filePattern;
    /** Scans for the tags. */
    private final TaskScanner taskScanner;

    /**
     * Creates a new instance of <code>WorkspaceScanner</code>.
     *
     * @param filePattern
     *            ant file-set pattern to scan for files
     * @param high
     *            tag identifiers indicating high priority
     * @param normal
     *            tag identifiers indicating normal priority
     * @param low
     *            tag identifiers indicating low priority
     */
    WorkspaceScanner(final String filePattern, final String high, final String normal, final String low) {
        this.filePattern = filePattern;
        taskScanner = new TaskScanner(high, normal, low);
    }

    /** {@inheritDoc} */
    public JavaProject invoke(final File workspace, final VirtualChannel channel) throws IOException {
        String[] files = findFiles(workspace);
        if (files.length == 0) {
            throw new AbortException("No files were found that match the pattern '" + filePattern + "'. Configuration error?");
        }

        JavaProject javaProject = new JavaProject();
        for (String file : files) {
            File originalFile = new File(workspace, file);
            JavaFile javaFile = taskScanner.scan(new FilePath(originalFile).read());
            if (javaFile.hasTasks()) {
                javaProject.addFile(javaFile);
            }
        }
        return javaProject;
    }

    /**
     * Returns an array with the filenames of the files that have been found in
     * the workspace.
     *
     * @param workspaceRoot
     *            root directory of the workspace
     * @return the filenames of the FindBugs files
     */
    private String[] findFiles(final File workspaceRoot) {
        FileSet fileSet = new FileSet();
        org.apache.tools.ant.Project project = new org.apache.tools.ant.Project();
        fileSet.setProject(project);
        fileSet.setDir(workspaceRoot);
        fileSet.setIncludes(filePattern);

        return fileSet.getDirectoryScanner(project).getIncludedFiles();
    }
}