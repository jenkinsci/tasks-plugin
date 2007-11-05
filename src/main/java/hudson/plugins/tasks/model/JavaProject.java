package hudson.plugins.tasks.model;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * A serializable Java Bean class representing a project that has been built by
 * Hudson.
 */
public class JavaProject extends AnnotationContainer implements Serializable {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 8556968267678442661L;
    /** All maven modules in this project (mapped by their name). */
    private final Map<String, MavenModule> moduleMapping = new HashMap<String, MavenModule>();
    /** Path of the workspace. */
    private String workspacePath;

    /**
     * Creates the mapping of modules.
     *
     * @param annotation
     *            the added annotation
     */
    @Override
    protected void annotationAdded(final FileAnnotation annotation) {
        WorkspaceFile file = annotation.getWorkspaceFile();

        String moduleName = StringUtils.defaultIfEmpty(file.getModuleName(), "Default Module");
        if (!moduleMapping.containsKey(moduleName)) {
            moduleMapping.put(moduleName, new MavenModule(moduleName));
        }
        moduleMapping.get(moduleName).addAnnotation(annotation);
    }

    /**
     * Gets the modules of this project that have annotations.
     *
     * @return the modules with annotations
     */
    public Collection<MavenModule> getModules() {
        return Collections.unmodifiableCollection(moduleMapping.values());
    }

    /**
     * Returns the maven module with the given name.
     *
     * @param moduleName
     *            the module to get
     * @return the module with the given name
     */
    public MavenModule getModule(final String moduleName) {
        return moduleMapping.get(moduleName);
    }

    /**
     * Gets the packages of this project that have annotations.
     *
     * @return the packages with annotations
     */
    public Collection<JavaPackage> getPackages() {
        List<JavaPackage> packages = new ArrayList<JavaPackage>();
        for (MavenModule module : moduleMapping.values()) {
            packages.addAll(module.getPackages());
        }
        return packages;
    }

    /**
     * Returns the package with the given name.
     *
     * @param name the package name
     * @return the package with the given name.
     */
    public JavaPackage getPackage(final String name) {
        return moduleMapping.values().iterator().next().getPackage(name);
    }

    /**
     * Gets the files of this project that have annotations.
     *
     * @return the files with annotations
     */
    public Collection<WorkspaceFile> getFiles() {
        List<WorkspaceFile> files = new ArrayList<WorkspaceFile>();
        for (MavenModule module : moduleMapping.values()) {
            files.addAll(module.getFiles());
        }
        return files;
    }

    /**
     * Returns the file with the given name.
     *
     * @param name the file name
     * @return the file with the given name.
     */
    public WorkspaceFile getFile(final String name) {
        return moduleMapping.values().iterator().next().getFile(name);
    }

    /**
     * Sets the root path of the workspace files.
     *
     * @param workspacePath path to workspace
     */
    public void setWorkspacePath(final String workspacePath) {
        this.workspacePath = workspacePath;
    }

    /**
     * Returns the root path of the workspace files.
     *
     * @return the workspace path
     */
    public String getWorkspacePath() {
        return workspacePath;
    }

    /**
     * Gets the maximum number of tasks in a module.
     *
     * @return the maximum number of tasks
     */
    public int getTaskBound() {
        int tasks = 0;
        for (MavenModule module : moduleMapping.values()) {
            tasks = Math.max(tasks, module.getNumberOfAnnotations());
        }
        return tasks;
    }
}

