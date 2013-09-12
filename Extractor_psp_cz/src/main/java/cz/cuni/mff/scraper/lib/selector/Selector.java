package cz.cuni.mff.scraper.lib.selector;

import java.util.*;
import java.util.regex.Pattern;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


/**
 * Abstract class for data selector.
 * 
 * @author Jakub Starka
 */
public abstract class Selector {
    
    protected static final HashMap<String, Integer> usageMap = new HashMap<>();

    public static void report() {
        SortedSet<Map.Entry<String, Integer>> sortedset = new TreeSet<Map.Entry<String, Integer>>(
            new Comparator<Map.Entry<String, Integer>>() {
                @Override
                public int compare(Map.Entry<String, Integer> e1,
                        Map.Entry<String, Integer> e2) {
                    if (e2.getValue().equals(e1.getValue())) {
                        return e1.getKey().compareTo(e2.getKey());
                    } else {
                        return e1.getValue().compareTo(e2.getValue());
                    }
                }
            });

        sortedset.addAll(usageMap.entrySet());
        
        for (Map.Entry<String, Integer> item: sortedset) {
            System.out.println(String.format("%1$-80s", item.getKey()) + ":\t" + item.getValue());
        }
    }
    
    /**
     * Create css selector from the result of this selector.
     * 
     * @param doc Input JSoup document.
     * @param selector CSS selector.
     * @param order Index of selector result.
     * @return New selector based on result of current selector.
     */
    public Selector selectCss(Element doc, String selector, Integer order) {
        return new CssSelector(doc, selector, order);
    }
    
    /**
     * Selector for n-th line. Note, that lines are based on &lt;br/&gt; tag
     * 
     * @param line Index of returned line.
     * @return New selector.
     */
    public Selector selectLine(Integer line) {
        return new LineSelector(this.getValue(), line);
    }
    
    /**
     * Selector for line starting with given string.
     * 
     * @param startsWith Line prefix.
     * @return New selector.
     */
    public Selector selectLine(String startsWith) {
        return new LineSelector(this.getValue(), startsWith);
    }

    /**
     * Selectors looks for given reqular expression and uses replacement with support of regex groups.
     * 
     * @param search Matching regular expression.
     * @param replacement Replacement expression with groups.
     * @return New selector.
     */
    public Selector selectRegex(String search, String replacement) {
        return new RegexSelector(this.getValue(), search, replacement);
    }
    
    /**
     * Select line based on given pattern and given index. This method supports negative indexes, i.e. -1 means the last.
     * 
     * @param regex Mathing regular expression.
     * @param line Index of mathing line. The negative values are supported.
     * @return New selector.
     */
    public Selector selectLine(Pattern regex, Integer line) {
        return new LineSelector(this.getValue(), regex, line);
    }
    
    /**
     * Gets the value provided by this selector.
     * 
     * @return 
     */
    public abstract String getValue();
    
}
