package hudson.plugins.tasks.parser;

import hudson.plugins.tasks.util.AbortException;
import hudson.plugins.tasks.util.model.Priority;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;

/**
 * Scans a given input stream for open tasks.
 *
 * @author Ulli Hafner
 */
public class TaskScanner {
    /** The regular expression patterns to be used to scan the files. One pattern per priority. */
    private final Map<Priority, Pattern> patterns = new HashMap<Priority, Pattern>();

    /**
     * Creates a new instance of <code>TaskScanner</code>.
     */
    public TaskScanner() {
        this("FIXME", "TODO", "@deprecated", false);
    }

    /**
     * Creates a new instance of <code>TaskScanner</code>.
     *
     * @param high
     *            tag identifiers indicating high priority
     * @param normal
     *            tag identifiers indicating normal priority
     * @param low
     *            tag identifiers indicating low priority
     * @param ignoreCase
     *            if case should be ignored during matching
     */
    public TaskScanner(final String high, final String normal, final String low,
            final boolean ignoreCase) {
        if (StringUtils.isNotBlank(high)) {
            patterns.put(Priority.HIGH, compile(high, ignoreCase));
        }
        if (StringUtils.isNotBlank(normal)) {
            patterns.put(Priority.NORMAL, compile(normal, ignoreCase));
        }
        if (StringUtils.isNotBlank(low)) {
            patterns.put(Priority.LOW, compile(low, ignoreCase));
        }
    }

    /**
     * Compiles a regular expression pattern to scan for tag identifiers.
     *
     * @param tagIdentifiers
     *            the identifiers to scan for
     * @param ignoreCase
     *            specifies if case should be ignored
     * @return the compiled pattern
     */
    private Pattern compile(final String tagIdentifiers, final boolean ignoreCase) {
        try {
            String[] tags;
            if (tagIdentifiers.indexOf(',') == -1) {
                tags = new String[] {tagIdentifiers};
            }
            else {
                tags = StringUtils.split(tagIdentifiers, ",");
            }
            List<String> regexps = new ArrayList<String>();
            for (int i = 0; i < tags.length; i++) {
                String tag = tags[i].trim();
                if (StringUtils.isNotBlank(tag)) {
                    if (Character.isLetterOrDigit(tag.charAt(0))) {
                        regexps.add("\\b" + tag + "\\b");
                    }
                    else {
                        regexps.add(tag + "\\b");
                    }
                }
            }
            int flags;
            if (ignoreCase) {
                flags = Pattern.CASE_INSENSITIVE;
            }
            else {
                flags = 0;
            }
            return Pattern.compile("^.*(" + StringUtils.join(regexps.iterator(), "|") + ")(.*)$", flags);
        }
        catch (PatternSyntaxException exception) {
            throw new AbortException("Invalid identifiers in a regular expression: " + tagIdentifiers + "\n", exception);
        }
    }

    /**
     * Scans the specified input stream for open tasks.
     *
     * @param reader
     *            the file to scan
     * @return the result stored as java project
     * @throws IOException
     *             if we can't read the file
     */
    public Collection<Task> scan(final Reader reader) throws IOException {
        LineIterator lineIterator = IOUtils.lineIterator(reader);
        List<Task> tasks = new ArrayList<Task>();
        for (int lineNumber = 1; lineIterator.hasNext(); lineNumber++) {
            String line = (String)lineIterator.next();

            for (Priority priority : Priority.values()) {
                if (patterns.containsKey(priority)) {
                    Matcher matcher = patterns.get(priority).matcher(line);
                    if (matcher.matches() && matcher.groupCount() == 2) {
                        String message = matcher.group(2).trim();
                        tasks.add(new Task(priority, lineNumber, matcher.group(1), StringUtils.remove(message, ":").trim()));
                    }
                }
            }
        }
        reader.close();

        return tasks;
    }
}

