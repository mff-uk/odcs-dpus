package cz.cuni.mff.scraper.lib.selector;

import java.util.LinkedList;
import java.util.regex.Pattern;

/**
 *  This is line selector. Works with HTML input.
 * 
 * @author Jakub Starka
 */
public class LineSelector extends Selector {

    Integer line;
    String text;
    String startsWith;
    Pattern pattern;
    
    /**
     * Contructor with input text and line index.
     * 
     * @param text Input text.
     * @param line Line index.
     */
    public LineSelector(String text, Integer line) {
        this(text, "", line);
    }
    
    /**
     * Constructor with input text and prefix string.
     * 
     * @param text Input string.
     * @param startsWith Prefix string.
     */
    public LineSelector(String text, String startsWith) {
        this(text, startsWith, 0);
    }
    
    /**
     * Constructor with input text, prefix string and index.
     * 
     * @param text Input text.
     * @param startsWith Prefix string.
     * @param appereance Index.
     */
    public LineSelector(String text, String startsWith, Integer appereance) {
        this.line = appereance;
        this.text = text;
        this.startsWith = startsWith;
    }
    
    /**
     * Constructor with input text, matching regular expression and index.
     * 
     * @param text Input text.
     * @param regex Reqular expression.
     * @param appereance Index.
     */
    public LineSelector(String text, Pattern regex, Integer appereance) {
        this(text, "", appereance);
        this.pattern = regex;
    }

    @Override
    public String getValue() {
        if (text == null) {
            return null;
        }
        LinkedList<Integer> foundList = new LinkedList<>();
        String[] lines = text.split("<br />");
        Integer current = 0;
        for (int i = 0; i < lines.length; i ++) {
            if (pattern != null) {
                if(pattern.matcher(lines[i]).matches()) {
                    foundList.add(i);
                }
            } else if (lines[i].startsWith(startsWith)) {
                foundList.add(i);
            }
        }
        int lineNo = line;
        if (lineNo < 0) {
            lineNo = foundList.size() + lineNo;
            if (lineNo < 0) {
                return null;
            }
        }
        if (foundList.size() > lineNo) {
            return lines[foundList.get(lineNo)];
        }
        return null;
    }
    
    
    
}
