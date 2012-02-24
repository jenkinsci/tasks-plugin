package hudson.plugins.tasks.parser;

import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.plugins.analysis.util.ContextHashCode;
import hudson.plugins.analysis.util.EncodingValidator;
import hudson.plugins.analysis.util.ModuleDetector;
import hudson.plugins.analysis.util.NullModuleDetector;
import hudson.plugins.analysis.util.PackageDetectors;
import hudson.plugins.analysis.util.StringPluginLogger;
import hudson.remoting.VirtualChannel;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.types.FileSet;

/**
 * Scans the workspace and records the found tasks. Each file is then
 * classified, i.e., a module and package is guessed and assigned.
 *
 * @author Ulli Hafner
 */
// CHECKSTYLE:COUPLING-OFF
public class WorkspaceScanner implements FileCallable<TasksParserResult> {
    /** Generated ID. */
    private static final long serialVersionUID = -4355362392102020724L;
    /** Ant file-set pattern to define the files to scan. */
    private final String filePattern;
    /** Ant file-set pattern to define the files to exclude from scan. */
    private String excludeFilePattern;
    /** The maven module. If <code>null</code>, then the scanner tries to guess it (freestyle project). */
    private String moduleName;
    /** Tag identifiers indicating high priority. */
    private final String high;
    /** Tag identifiers indicating normal priority. */
    private final String normal;
    /** Tag identifiers indicating low priority. */
    private final String low;
    /** Tag identifiers indicating case sensitive parsing. */
    private final boolean ignoreCase;
    /** Prefix of path. */
    private String prefix;
    /** The default encoding to be used when reading and parsing files. */
    private final String defaultEncoding;
    /** Determines whether module names should be derived from Maven or Ant. */
    private final boolean shouldDetectModules;

    private transient StringPluginLogger stringLogger;

    /**
     * Creates a new instance of <code>WorkspaceScanner</code>.
     *
     * @param filePattern
     *            ant file-set pattern to scan for files
     * @param excludeFilePattern
     *            ant file-set pattern to exclude from scan
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param high
     *            tag identifiers indicating high priority
     * @param normal
     *            tag identifiers indicating normal priority
     * @param low
     *            tag identifiers indicating low priority
     * @param ignoreCase
     *            if case should be ignored during matching
     * @param shouldDetectModules
     *            determines whether module names should be derived from Maven POM or Ant build files
     */
    // CHECKSTYLE:OFF
    public WorkspaceScanner(final String filePattern, final String excludeFilePattern, final String defaultEncoding,
            final String high, final String normal, final String low, final boolean ignoreCase, final boolean shouldDetectModules) {
        this.filePattern = filePattern;
        this.excludeFilePattern = excludeFilePattern;
        this.defaultEncoding = defaultEncoding;
        this.high = high;
        this.normal = normal;
        this.low = low;
        this.ignoreCase = ignoreCase;
        this.shouldDetectModules = shouldDetectModules;
    }
    // CHECKSTYLE:ON

    /**
     * Logs the specified message.
     *
     * @param message the message
     */
    protected void log(final String message) {
        if (stringLogger == null) {
            stringLogger = new StringPluginLogger("TASKS");
        }
        stringLogger.log(message);
    }


    /**
     * Creates a new instance of <code>WorkspaceScanner</code>.
     *
     * @param filePattern
     *            ant file-set pattern to scan for files
     * @param excludeFilePattern
     *            ant file-set pattern to exclude from scan
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param high
     *            tag identifiers indicating high priority
     * @param normal
     *            tag identifiers indicating normal priority
     * @param low
     *            tag identifiers indicating low priority
     * @param caseSensitive
     *            determines whether the scanner should work case sensitive
     * @param moduleName
     *            the maven module name
     */
    // CHECKSTYLE:OFF
    public WorkspaceScanner(final String filePattern, final String excludeFilePattern, final String defaultEncoding,
            final String high, final String normal, final String low, final boolean caseSensitive,
            final String moduleName) {
        this(filePattern, excludeFilePattern, defaultEncoding, high, normal, low, caseSensitive, false);

        this.moduleName = moduleName;
    }
    // CHECKSTYLE:ON

    /**
     * Creates a new instance of <code>WorkspaceScanner</code>.
     *
     * @param filePattern
     *            ant file-set pattern to scan for files
     * @param excludeFilePattern
     *            ant file-set pattern to exclude from scan
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param high
     *            tag identifiers indicating high priority
     * @param normal
     *            tag identifiers indicating normal priority
     * @param low
     *            tag identifiers indicating low priority
     * @param caseSensitive
     *            determines whether the scanner should work case sensitive
     * @param moduleName
     *            the maven module name
     */
    // CHECKSTYLE:OFF
    public WorkspaceScanner(final String filePattern, final String excludeFilePattern, final String defaultEncoding,
            final String high, final String normal, final String low, final boolean caseSensitive,
            final String moduleName, final List<String> modules) {
        this(filePattern, excludeFilePattern, defaultEncoding, high, normal, low, caseSensitive, moduleName);

        StringBuilder excludes = new StringBuilder(excludeFilePattern);
        for (String folder : modules) {
            if (StringUtils.isNotBlank(excludes.toString())) {
                excludes.append(", ");
            }
            excludes.append(folder + "/**/*");
        }
        this.excludeFilePattern = excludes.toString();
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
    public TasksParserResult invoke(final File workspace, final VirtualChannel channel) throws IOException, InterruptedException {
        String[] files = findFiles(workspace);

        TaskScanner taskScanner = new TaskScanner(high, normal, low, ignoreCase);
        TasksParserResult result = new TasksParserResult(files.length);
        ModuleDetector moduleDetector = createModuleDetector(workspace);
        log("Found " + files.length + " files to scan for tasks");
        for (String fileName : files) {
            try {
                File originalFile = new File(workspace, fileName);
                Collection<Task> tasks = taskScanner.scan(readFile(originalFile));
                if (!tasks.isEmpty()) {
                    String absolutePath = originalFile.getAbsolutePath();
                    String packageName = PackageDetectors.detectPackageName(absolutePath);
                    String guessedModule = moduleDetector.guessModuleName(absolutePath);
                    String actualModule = StringUtils.defaultIfEmpty(moduleName, guessedModule);

                    for (Task task : tasks) {
                        task.setFileName(absolutePath);
                        task.setPackageName(packageName);
                        task.setModuleName(actualModule);
                        task.setPathName(workspace.getPath());

                        ContextHashCode hashCode = new ContextHashCode();
                        task.setContextHashCode(hashCode.create(absolutePath, task.getPrimaryLineNumber(), defaultEncoding));
                    }

                    result.addAnnotations(tasks);
                }
            }
            catch (IOException exception) {
                // ignore files that could not be read
            }
            if (Thread.interrupted()) {
                throw new InterruptedException("Canceling scanning since build has been aborted.");
            }
        }
        result.addModule(moduleName);

        if (stringLogger != null) {
            result.setLog(stringLogger.toString());
        }

        return result;
    }

    private InputStreamReader readFile(final File originalFile) throws IOException {
        return new InputStreamReader(new FilePath(originalFile).read(),
                    EncodingValidator.defaultCharset(defaultEncoding));
    }

    private ModuleDetector createModuleDetector(final File workspace) {
        if (shouldDetectModules) {
            return new ModuleDetector(workspace);
        }
        else {
            return new NullModuleDetector();
        }
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

        log("Scanning folder '" + workspaceRoot + "' for files matching the pattern '" + filePattern
                + "' - excludes: " + excludeFilePattern);

        return fileSet.getDirectoryScanner(project).getIncludedFiles();
    }
}