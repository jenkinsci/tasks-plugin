package hudson.plugins.tasks.model;



import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

/**
 * A serializable Java Bean class representing a file in the Hudson workspace.
 */
public class WorkspaceFile extends AnnotationContainer implements Serializable {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 601361940925156719L;
    /** The absolute filename of this file. */
    private String name;
    /** Package name of this task. */
    private String packageName;
    /** Module name of this task. */
    private String moduleName;

    /**
     * Returns the filename name of this file.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this file.
     *
     * @param name the name of this file
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Returns a readable name of this workspace file.
     *
     * @return a readable name of this workspace file.
     */
    public String getShortName() {
        return StringUtils.substringAfterLast(name, "/");
    }

    /**
     * Sets the package name to the specified value.
     *
     * @param packageName the package name
     */
    public void setPackageName(final String packageName) {
        this.packageName = packageName;
    }

    /**
     * Returns the packageName.
     *
     * @return the packageName
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Sets the module name to the specified value.
     *
     * @param moduleName the module name
     */
    public void setModuleName(final String moduleName) {
        this.moduleName = moduleName;
    }

    /**
     * Returns the moduleName.
     *
     * @return the moduleName
     */
    public String getModuleName() {
        return moduleName;
    }
}

