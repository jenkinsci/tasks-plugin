package hudson.plugins.tasks;

import hudson.plugins.tasks.Task.Priority;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

/**
 * Scans a given input stream for open tasks.
 */
public class TaskScanner {
    /** The regular expression patterns to be used to scan the files. One pattern per priority. */
    private final Map<Priority, Pattern> patterns = new HashMap<Priority, Pattern>();

    /**
     * Creates a new instance of <code>TaskScanner</code>.
     */
    public TaskScanner() {
        patterns.put(Priority.HIGH, Pattern.compile("^.*FIXME(.*)$"));
        patterns.put(Priority.NORMAL, Pattern.compile("^.*TODO(.*)$"));
        patterns.put(Priority.LOW, Pattern.compile("^.*@deprecated(.*)$"));
    }

    /**
     * Scans the specified input stream for open tasks.
     *
     * @param file
     *            the file to scan
     * @return the result stored as java project
     * @throws IOException
     *             if we can't read the file
     */
    public JavaFile scan(final InputStream file) throws IOException {
        JavaFile javaFile = new JavaFile();
        LineIterator lineIterator = IOUtils.lineIterator(file, null);
        for (int lineNumber = 0; lineIterator.hasNext(); lineNumber++) {
            String line = (String)lineIterator.next();

            for (Priority priority : Priority.values()) {
                Matcher matcher = patterns.get(priority).matcher(line);
                if (matcher.matches() && matcher.groupCount() == 1) {
                    javaFile.addTask(priority, lineNumber, matcher.group(1).trim());
                }
            }
        }
        return javaFile;
    }
}

