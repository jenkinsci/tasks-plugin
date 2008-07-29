package hudson.plugins.tasks.parser;

import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.plugins.tasks.util.CsharpNamespaceDetector;
import hudson.plugins.tasks.util.JavaPackageDetector;
import hudson.plugins.tasks.util.ModuleDetector;
import hudson.plugins.tasks.util.PackageDetector;
import hudson.remoting.VirtualChannel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.types.FileSet;

/**
 * Scans the workspace and records the found tasks. Each file is then
 * classified, i.e., a module and package is guessed and assigned.
 *
 * @author Ulli Hafner
 */
public class WorkspaceScanner implements FileCallable<TasksParserResult> {
    /** Generated ID. */
    private static final long serialVersionUID = -4355362392102020724L;
    /** Ant file-set pattern to define the files to scan. */
    private final String filePattern;
    /** Ant file-set pattern to define the files to exclude from scan. */
    private final String excludeFilePattern;
    /** The maven module. If <code>null</code>, then the scanner tries to guess it (freestyle project). */
    private String moduleName;
    /** Tag identifiers indicating high priority. */
    private final String high;
    /** Tag identifiers indicating normal priority. */
    private final String normal;
    /** Tag identifiers indicating low priority. */
    private final String low;
    /** Prefix of path. */
    private String prefix;

    /**
     * Creates a new instance of <code>WorkspaceScanner</code>.
     *
     * @param filePattern
     *            ant file-set pattern to scan for files
     * @param excludeFilePattern
     *            ant file-set pattern to exclude from scan
     * @param high
     *            tag identifiers indicating high priority
     * @param normal
     *            tag identifiers indicating normal priority
     * @param low
     *            tag identifiers indicating low priority
     */
    public WorkspaceScanner(final String filePattern, final String excludeFilePattern, final String high, final String normal, final String low) {
        this.filePattern = filePattern;
        this.excludeFilePattern = excludeFilePattern;
        this.high = high;
        this.normal = normal;
        this.low = low;
    }

    /**
     * Creates a new instance of <code>WorkspaceScanner</code>.
     *
     * @param moduleName
     *            the maven module name
     * @param filePattern
     *            ant file-set pattern to scan for files
     * @param excludeFilePattern
     *            ant file-set pattern to exclude from scan
     * @param high
     *            tag identifiers indicating high priority
     * @param normal
     *            tag identifiers indicating normal priority
     * @param low
     *            tag identifiers indicating low priority
     */
    public WorkspaceScanner(final String filePattern, final String excludeFilePattern, final String high, final String normal, final String low, final String moduleName) {
        this(filePattern, excludeFilePattern, high, normal, low);
        this.moduleName = moduleName;
    }

    /**
     * Sets the prefix to the specified value.
     *
     * @param prefix the value to set
     */
    public void setPrefix(final String prefix) {
        this.prefix = prefix + "/";
    }

    /**
     * Returns the prefix.
     *
     * @return the prefix
     */
    public String getPrefix() {
        return StringUtils.defaultIfEmpty(prefix, StringUtils.EMPTY);
    }

    /** {@inheritDoc} */
    public TasksParserResult invoke(final File workspace, final VirtualChannel channel) throws IOException {
        String[] files = findFiles(workspace);

        List<PackageDetector> detectors = new ArrayList<PackageDetector>();
        detectors.add(new JavaPackageDetector());
        detectors.add(new CsharpNamespaceDetector());

        TaskScanner taskScanner = new TaskScanner(high, normal, low);

        TasksParserResult javaProject = new TasksParserResult(files.length);
        ModuleDetector moduleDetector = new ModuleDetector();
        Map<String, String> pathNameToModuleMapping = moduleDetector.getModules(workspace);
        for (String fileName : files) {
            File originalFile = new File(workspace, fileName);
            Collection<Task> tasks = taskScanner.scan(new FilePath(originalFile).read());
            if (!tasks.isEmpty()) {
                String unixName = fileName.replace('\\', '/');
                String packageName = detectPackageName(detectors, unixName, new FilePath(originalFile).read());
                String guessedModule = guessModuleName(pathNameToModuleMapping, originalFile);
                String actualModule = StringUtils.defaultIfEmpty(moduleName, guessedModule);

                for (Task task : tasks) {
                    task.setFileName(getPrefix() + unixName);
                    task.setPackageName(packageName);
                    task.setModuleName(actualModule);
                }

                javaProject.addAnnotations(tasks);
            }
        }

        return javaProject;
    }

    /**
     * Uses the path prefixes of pom.xml or build.xml files to guess a module name for the specified file.
     *
     * @param pathNameToModuleMapping mapping of file prefixes to module names
     * @param originalFile file name to guess a module for
     * @return a module name or an empty string
     */
    private String guessModuleName(final Map<String, String> pathNameToModuleMapping, final File originalFile) {
        ArrayList<String> prefixes = new ArrayList<String>(pathNameToModuleMapping.keySet());
        Collections.sort(prefixes);

        String fullPath = originalFile.getAbsolutePath().replace('\\', '/');

        String guessedModule = StringUtils.EMPTY;
        for (String path : prefixes) {
            if (fullPath.startsWith(path)) {
                guessedModule = pathNameToModuleMapping.get(path);
            }
        }
        return guessedModule;
    }

    /**
     * Detects the package name of the specified file based on several detector
     * strategies.
     *
     * @param detectors
     *            the detectors to use
     * @param fileName
     *            the filename of the file to scan
     * @param content
     *            the content of the file
     * @return the package name or an empty string
     * @throws IOException
     */
    private String detectPackageName(final List<PackageDetector> detectors, final String fileName, final InputStream content) throws IOException {
        for (PackageDetector detector : detectors) {
            if (detector.accepts(fileName)) {
                return detector.detectPackageName(content);
            }
        }
        return "n/a";
    }

    /**
     * Returns an array with the filenames of the files that have been found in
     * the workspace.
     *
     * @param workspaceRoot
     *      root directory of the workspace
     * @return the filenames of the FindBugs files
     */
    private String[] findFiles(final File workspaceRoot) {
        FileSet fileSet = new FileSet();
        org.apache.tools.ant.Project project = new org.apache.tools.ant.Project();
        fileSet.setProject(project);
        fileSet.setDir(workspaceRoot);
        fileSet.setIncludes(filePattern);

        if (StringUtils.isNotBlank(excludeFilePattern)) {
            fileSet.setExcludes(excludeFilePattern);
        }

        return fileSet.getDirectoryScanner(project).getIncludedFiles();
    }
}