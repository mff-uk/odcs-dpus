package cz.cuni.mff.xrg.uv.addressmapper.streetAddress;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Škoda Petr
 */
public class StreetAddressParser {
    
    private static final Logger LOG = LoggerFactory.getLogger(StreetAddressParser.class);
    
    private final static String REGEXP_PATTERN = "^(?<name>(\\w+\\.?\\s*)*([\\p{L}\\.]+\\s*)+)(\\s(?<landNumber>\\d+)?)?(\\s*/\\s*(?<homeNumber>[\\w,]+)?)?$";

    private final Pattern pattern;

    private final List<Formatter> formaters;
        
    public StreetAddressParser() {
        this.pattern = Pattern.compile(REGEXP_PATTERN, Pattern.UNICODE_CHARACTER_CLASS);
        // ..
        this.formaters = new LinkedList<>();
        this.formaters.add(new BracesRemovalFormatter());
        this.formaters.add(new DuplicityRemovalFormatter());
        this.formaters.add(new NumbersSeparationFormatter());
        this.formaters.add(new ShortcutRemovalFormatter());
        this.formaters.add(new SpaceInsertFormatter());
    }

    public StreetAddress parse(String address) throws WrongAddressFormatException {
        // check if the string is not empty
        if (address.isEmpty()) {
            return new StreetAddress();
        }
        // check format for adress for strange characters
        checkFormat(address);
        // apply formaters 
        // TODO apply until there is a change ?
        for (Formatter form : formaters) {
            address = form.format(address);
        }
        // check if the string is not empty
        if (address.isEmpty()) {
            return new StreetAddress();
        }
        // from how many words address consists
        if (!address.contains(" ")) {
            return parseSingleWord(address);
        } else {
            return parseMultipleWords(address);
        }
    }

    /**
     * Check format of
     *
     * @param address
     */
    private void checkFormat(String address) throws WrongAddressFormatException {
        if (address.contains("?")) {
            throw new StrangeCharactersException("Adress contains '?'");
        }
    }

    /**
     * Parse StreetAddress that consists from a single word.
     *
     * @param str
     * @return
     * @throws IllegalArgumentException
     */
    private StreetAddress parseSingleWord(String address) throws IllegalArgumentException {
        // it's a single word
        try {
            Integer.parseInt(address);
            // it's number -> landNumber
            return new StreetAddress(null, address, null);
        } catch (NumberFormatException e) {
        }

        // can be number/number?
        if (address.contains("/")) {
            String[] split = address.split("/");
            return new StreetAddress(null, split[0], split[1]);
        }
        // it's name (not a number)
        return new StreetAddress(address, null, null);
    }

    /**
     * Parse StreetAddress which consists of multiple words,
     *
     * @param address
     * @return
     * @throws WrongAddressFormatException
     */
    private StreetAddress parseMultipleWords(String address) throws WrongAddressFormatException {
        String townName = null;
        if (address.contains("-")) {
            // use first part as a street name
            final int splitPos = address.indexOf("-");
            townName = address.substring(0, splitPos).trim();
            address = address.substring(splitPos + 1).trim();
        }
        
        if (!address.matches(".*\\d.*")) {
            // there are no numbers, it's just spaces and alpha -> it's just name
            // this should prevent from not necesary usage of reg. exp.
            return new StreetAddress(townName, address.trim(), null, null);
        }

        Matcher matcher = pattern.matcher(address);
        if (!matcher.find()) {
            throw new WrongAddressFormatException("Reg.exp. failed to parse: " + address);
        }
        return new StreetAddress(townName, matcher.group("name").trim(),
                matcher.group("landNumber"), matcher.group("homeNumber"));
    }
   
}
