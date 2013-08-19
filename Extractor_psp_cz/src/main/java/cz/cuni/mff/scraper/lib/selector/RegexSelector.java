package cz.cuni.mff.scraper.lib.selector;

/**
 *  This is line selector. Works with HTML input.
 * 
 * @author Jakub
 */
public class RegexSelector extends Selector {

    String text;
    String search;
    String replacement;
    
    /**
     * Constructor with input text value, regular expression string and replacement string.
     * 
     * @param text Input text value.
     * @param search Search regular expression.
     * @param replacement Replacement string with groups.
     */
    public RegexSelector(String text, String search, String replacement) {
        this.text = text;
        this.search = search;
        this.replacement = replacement;
    }

    @Override
    public String getValue() {
        if (text == null || search == null || replacement == null) {
            return null;
        }
        return text.replaceAll(search, replacement);
    }    
}
