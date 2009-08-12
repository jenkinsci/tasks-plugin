package hudson.plugins.tasks;

import hudson.model.AbstractBuild;
import hudson.model.Hudson;
import hudson.plugins.tasks.util.PrioritiesDetail;
import hudson.plugins.tasks.util.PriorityDetailFactory;
import hudson.plugins.tasks.util.SourceDetail;
import hudson.plugins.tasks.util.model.AnnotationContainer;
import hudson.plugins.tasks.util.model.Priority;

import org.apache.commons.lang.StringUtils;

/**
 * Creates detail objects for the selected element of a tasks container.
 *
 * @author Ulli Hafner
 */
@SuppressWarnings("PMD")
public class TasksDetailBuilder {
    /**
     * Returns a detail object for the selected element of a tasks container.
     *
     * @param link
     *            the link to the source code
     * @param owner
     *            the build as owner of the detail page
     * @param container
     *            the annotation container to get the details for
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
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
    @edu.umd.cs.findbugs.annotations.SuppressWarnings({"IMA", "SIC"})
    // CHECKSTYLE:OFF
    public Object getDynamic(final String link, final AbstractBuild<?, ?> owner,
            final AnnotationContainer container, final String defaultEncoding, final String displayName,
            final String high, final String normal, final String low) {
    // CHECKSTYLE:ON
        PriorityDetailFactory factory = new PriorityDetailFactory() {
            /** {@inheritDoc} */
            @Override
            protected PrioritiesDetail createPrioritiesDetail(final Priority priority, final AbstractBuild<?, ?> build, final AnnotationContainer annotationContainer, @SuppressWarnings("hiding") final String defaultEncoding, final String header) {
                return new TasksPrioritiesDetail(build, annotationContainer, priority, defaultEncoding, header, high, normal, low);
            }
        };
        if (factory.isPriority(link)) {
            return factory.create(link, owner, container, defaultEncoding, displayName);
        }
        else if (link.startsWith("module.")) {
            return new TasksModuleDetail(owner, container.getModule(Integer.valueOf(StringUtils.substringAfter(link, "module."))), defaultEncoding, displayName, high, normal, low);
        }
        else if (link.startsWith("package.")) {
            return new TasksPackageDetail(owner, container.getPackage(Integer.valueOf(StringUtils.substringAfter(link, "package."))), defaultEncoding, displayName, high, normal, low);
        }
        else if (link.startsWith("tab.tasks.")) {
            return new TasksTabDetail(owner, container, "/tasks/" + StringUtils.substringAfter(link, "tab.tasks.") + ".jelly", defaultEncoding, high, normal, low);
        }
        else if (link.startsWith("tab.")) {
            return new TasksTabDetail(owner, container, "/tabview/" + StringUtils.substringAfter(link, "tab.") + ".jelly", defaultEncoding, high, normal, low);
        }
        else if (link.startsWith("file.")) {
            return new TasksFileDetail(owner, container.getFile(Integer.valueOf(StringUtils.substringAfter(link, "file."))), defaultEncoding, displayName, high, normal, low);
        }
        else if (link.startsWith("source.")) {
            owner.checkPermission(Hudson.ADMINISTER);

            return new SourceDetail(owner, container.getAnnotation(StringUtils.substringAfter(link, "source.")), defaultEncoding);
        }
        return null;
    }
}

