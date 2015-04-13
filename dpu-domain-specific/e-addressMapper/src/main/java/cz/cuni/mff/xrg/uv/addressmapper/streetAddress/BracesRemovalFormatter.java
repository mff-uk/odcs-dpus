package cz.cuni.mff.xrg.uv.addressmapper.streetAddress;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Remove string in braces from address.
 *
 * @author Škoda Petr
 */
public class BracesRemovalFormatter implements Formatter {

    private final static String REGEXP_PATTERN = "^(?<prefix>[^\\(]*)(\\([^\\)]+\\)\\s*)*(?<suffix>.*)$";

    private final Pattern pattern;

    public BracesRemovalFormatter() {
        this.pattern = Pattern.compile(REGEXP_PATTERN, Pattern.UNICODE_CHARACTER_CLASS);
    }

    @Override
    public String format(String streetAddress) {
        Matcher matcher = pattern.matcher(streetAddress);
        if (!matcher.find()) {
            return streetAddress;
        }
        if (matcher.group("suffix").isEmpty() || matcher.group("suffix").startsWith(" ")) {
            // suffix starts with space, we don't need to add new
            return matcher.group("prefix").trim() + matcher.group("suffix").trim();
        } else {
            // insert space
            return matcher.group("prefix").trim() + " " + matcher.group("suffix").trim();
        }

    }

}
