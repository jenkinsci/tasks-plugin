package hudson.plugins.tasks.model;


import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

/**
 * A container for annotations.
 */
public class AnnotationContainer implements AnnotationProvider {
    /** The annotations mapped by their key. */
    private final Map<Long, FileAnnotation> annotations = new HashMap<Long, FileAnnotation>();
    /** The annotations mapped by priority. */
    private final Map<Priority, Set<FileAnnotation>> annotationsByPriority = new HashMap<Priority, Set<FileAnnotation>>();

    /**
     * Creates a new instance of <code>AnnotationContainer</code>.
     */
    public AnnotationContainer() {
        for (Priority priority : Priority.values()) {
            annotationsByPriority.put(priority, new HashSet<FileAnnotation>());
        }
    }

    /**
     * Adds the specified annotation to this container.
     *
     * @param annotation
     *            the annotation to add
     */
    public final void addAnnotation(final FileAnnotation annotation) {
        annotations.put(annotation.getKey(), annotation);
        annotationsByPriority.get(annotation.getPriority()).add(annotation);
        annotationAdded(annotation);
    }

    /**
     * Adds the specified annotations to this container.
     *
     * @param newAnnotations
     *            the annotations to add
     */
    public final void addAnnotations(final Collection<? extends FileAnnotation> newAnnotations) {
        for (FileAnnotation annotation : newAnnotations) {
            addAnnotation(annotation);
        }
    }


    /**
     * Called if the specified annotation has been added to this container.
     * Subclasses may override this default empty implementation.
     *
     * @param annotation
     *            the added annotation
     */
    protected void annotationAdded(final FileAnnotation annotation) {
        // empty default implementation
    }

    /** {@inheritDoc} */
    public final Collection<FileAnnotation> getAnnotations() {
        return Collections.unmodifiableCollection(annotations.values());
    }

    /** {@inheritDoc} */
    public final Collection<FileAnnotation> getAnnotations(final Priority priority) {
        return Collections.unmodifiableCollection(annotationsByPriority.get(priority));
    }

    /**
     * Returns the annotations of the specified priority for this object.
     *
     * @param priority
     *            the priority
     * @return annotations of the specified priority for this object
     */
    public final Collection<FileAnnotation> getAnnotations(final String priority) {
        return getAnnotations(getPriority(priority));
    }

    /**
     * Converts a String priority to an actual enumeration value.
     *
     * @param priority
     *            priority as a String
     * @return enumeration value.
     */
    private Priority getPriority(final String priority) {
        return Priority.valueOf(StringUtils.upperCase(priority));
    }

    /** {@inheritDoc} */
    public final int getNumberOfAnnotations() {
        return annotations.size();
    }

    /** {@inheritDoc} */
    public final int getNumberOfAnnotations(final Priority priority) {
        return annotationsByPriority.get(priority).size();
    }

    /**
     * Returns the annotations of the specified priority for this object.
     *
     * @param priority
     *            the priority
     * @return annotations of the specified priority for this object
     */
    public final int getNumberOfAnnotations(final String priority) {
        return getNumberOfAnnotations(getPriority(priority));
    }

    /** {@inheritDoc} */
    public final boolean hasAnnotations() {
        return !annotations.isEmpty();
    }

    /** {@inheritDoc} */
    public final boolean hasAnnotations(final Priority priority) {
        return !annotationsByPriority.get(priority).isEmpty();
    }

    /**
     * Returns whether this objects has annotations with the specified priority.
     *
     * @param priority
     *            the priority
     * @return <code>true</code> if this objects has annotations.
     */
    public final boolean hasAnnotations(final String priority) {
        return hasAnnotations(getPriority(priority));
    }

    /** {@inheritDoc} */
    public final FileAnnotation getAnnotation(final long key) {
        return annotations.get(key);
    }

    /**
     * Returns the annotation with the specified key.
     *
     * @param key
     *            the key of the annotation
     * @return the annotation with the specified key
     */
    public final FileAnnotation getAnnotation(final String key) {
        return getAnnotation(Long.parseLong(key));
    }
}

