package cz.mff.cuni.scraper.lib.selector;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *  This is CSS selector. It works with document and css selector.
 * 
 * @author Jakub Starka
 */
public class CssSelector extends Selector {

    protected Element d = null;
    protected String selector = null;
    protected String attribute = null;
    protected String checked = null;
    protected String checkedAttribute = null;
    protected String value = null;
    protected Integer index = 0;
    
    /**
     * Constructor on JSoup document with CSS selector.
     * 
     * @param doc Input JSoup document.
     * @param string CSS selector.
     */
    public CssSelector(Element doc, String string) {
	this(doc, string, null, 0);
    }
    
    /**
     * Constructor with JSoup element
     * 
     * @param doc Input document.
     * @param string CSS selector.
     * @param checked Deprecated.
     */
    public CssSelector(Element doc, String string, String checked) {
        this(doc, string, checked, 0);
    }
    
    /**
     * Constructor with input document, css selector and index.
     * 
     * @param doc Input document.
     * @param string CSS selector.
     * @param index Index.
     */
    public CssSelector(Element doc, String string, Integer index) {
        this(doc, string, null, index);
    }
    
    /**
     * Constructor with input document, css selector string and index.
     * 
     * @param doc Input document.
     * @param string CSS selector.
     * @param checked Deprecated.
     * @param index Index.
     */
    public CssSelector(Element doc, String string, String checked, Integer index) {
        d = doc;
	if (string.contains("@")) {
	    String[] split = string.split("@");
	    selector = split[0];
	    attribute = split[1];
	} else {
	    selector = string;
	}
        if (checked != null) {
            if (checked.contains("@")) {
                String[] split = checked.split("@");
                this.checked = split[0];
                checkedAttribute = split[1];
            } else {
                this.checked = checked;
            }
        }
        this.index = index;
    }

    /**
     * Gets the value of CSS selector with HTML content.
     * 
     * @return CSS selected value with HTML.
     */
    public String getHtml() {
        if (this.checked != null) {
            Elements c = d.select(checked);
            if (checkedAttribute != null) {
                if (c.isEmpty() || !c.get(0).hasAttr(checkedAttribute) ||
                    c.get(0).attr(checkedAttribute).isEmpty()) {
                    return null;
                }
            } else {
                if (c.isEmpty() || c.get(0).text().isEmpty()) {
                   return null;
                }
            }
        }
        
        Elements e = d.select(selector);
	if (e.size() > index) {
            Element el = e.get(index);
	    if (attribute != null) {
		return el.attr(attribute);
	    } else {
		return el.html();
	    }
	} else {
	    return null;
	}
    }
    
    @Override
    public String getValue() {
	if (this.checked != null) {
            Elements c = d.select(checked);
            if (checkedAttribute != null) {
                if (c.isEmpty() || !c.get(0).hasAttr(checkedAttribute) ||
                    c.get(0).attr(checkedAttribute).isEmpty()) {
                    return null;
                }
            } else {
                if (c.isEmpty() || c.get(0).text().isEmpty()) {
                   return null;
                }
            }
        }
        
        Elements e = d.select(selector);
	if (e.size() > index) {
            Element el = e.get(index);
	    if (attribute != null) {
		return el.attr(attribute);
	    } else {
		return el.text();
	    }
	} else {
	    return null;
	}
    }

}
