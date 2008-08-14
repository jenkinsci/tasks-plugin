package hudson.plugins.tasks;

import hudson.model.AbstractBuild;
import hudson.plugins.tasks.util.PrioritiesDetail;
import hudson.plugins.tasks.util.PriorityDetailFactory;
import hudson.plugins.tasks.util.SourceDetail;
import hudson.plugins.tasks.util.model.AnnotationContainer;
import hudson.plugins.tasks.util.model.Priority;

import org.apache.commons.lang.StringUtils;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * Creates detail objects for the selected element of a tasks container.
 *
 * @author Ulli Hafner
 */
public class TaskDetailBuilder {
    /**
     * Returns a detail object for the selected element of a tasks container.
     *
     * @param link
     *            the link to the source code
     * @param owner
     *            the build as owner of the detail page
     * @param container
     *            the annotation container to get the details for
     * @param displayName
     *            the name of the selected object
     * @param high
     *            tag identifiers indicating high priority
     * @param normal
     *            tag identifiers indicating normal priority
     * @param low
     *            tag identifiers indicating low priority
     * @return the dynamic result of the FindBugs analysis (detail page for a
     *         package).
     */
    public Object getDynamic(final String link, final AbstractBuild<?, ?> owner,
            final AnnotationContainer container, final String displayName,
            final String high, final String normal, final String low) {
        PriorityDetailFactory factory = new PriorityDetailFactory() {
            /** {@inheritDoc} */
            @Override
            @SuppressWarnings("IMA")
            protected PrioritiesDetail createPrioritiesDetail(final Priority priority, final AbstractBuild<?, ?> build, final AnnotationContainer container, final String header) {
                return new TasksPrioritiesDetail(build, container, priority, header, high, normal, low);
            }
        };
        if (factory.isPriority(link)) {
            return factory.create(link, owner, container, displayName);
        }
        else if (link.startsWith("module.")) {
            return new TasksModuleDetail(owner, container.getModule(StringUtils.substringAfter(link, "module.")), displayName, high, normal, low);
        }
        else if (link.startsWith("package.")) {
            return new TasksPackageDetail(owner, container.getPackage(StringUtils.substringAfter(link, "package.")), displayName, high, normal, low);
        }
        else if (link.startsWith("tab.tasks.")) {
            return new TasksTabDetail(owner, container, "/tasks/" + StringUtils.substringAfter(link, "tab.tasks.") + ".jelly", high, normal, low);
        }
        else if (link.startsWith("tab.")) {
            return new TasksTabDetail(owner, container, "/tabview/" + StringUtils.substringAfter(link, "tab.") + ".jelly", high, normal, low);
        }
        else if (link.startsWith("file.")) {
            return new TasksFileDetail(owner, container.getFile(Integer.valueOf(StringUtils.substringAfter(link, "file."))), displayName, high, normal, low);
        }
        else if (link.startsWith("source.")) {
            return new SourceDetail(owner, container.getAnnotation(StringUtils.substringAfter(link, "source.")));
        }
        return null;
    }

}

