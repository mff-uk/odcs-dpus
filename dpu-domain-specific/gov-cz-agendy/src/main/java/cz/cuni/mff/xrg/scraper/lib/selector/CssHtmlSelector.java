package cz.cuni.mff.xrg.scraper.lib.selector;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *  This is CSS selector. It works with document and css selector.
 * 
 * @author Jakub Starka
 */
public class CssHtmlSelector extends Selector {

    protected Element d = null;
    protected String selector = null;
    protected String attribute = null;
    protected String checked = null;
    protected String checkedAttribute = null;
    protected String value = null;
    protected Integer index = 0;
    
    public CssHtmlSelector(Element doc, String string) {
    this(doc, string, null, 0);
    }
    
    public CssHtmlSelector(Element doc, String string, String checked) {
        this(doc, string, checked, 0);
    }
    
    public CssHtmlSelector(Element doc, String string, Integer index) {
        this(doc, string, null, index);
    }
    
    public CssHtmlSelector(Element doc, String string, String checked, Integer index) {
        d = doc;
    if (string.contains("@")) {
        String[] split = string.split("@");
        selector = split[0].trim();
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
        
        if (attribute != null && !usageMap.containsKey(selector + " @" + attribute)) {
            usageMap.put(selector + " @" + attribute, 0);
        } else if (attribute == null && !usageMap.containsKey(selector)) {
            usageMap.put(selector, 0);
        }
            
    }

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
                usageMap.put(selector + " @" + attribute, usageMap.get(selector + " @" + attribute) + 1);
        return el.attr(attribute);
        } else {
                usageMap.put(selector, usageMap.get(selector) + 1);
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
                usageMap.put(selector + " @" + attribute, usageMap.get(selector + " @" + attribute) + 1);
        return el.attr(attribute);
        } else {
                usageMap.put(selector, usageMap.get(selector) + 1);
        return el.html();
        }
    } else {
        return null;
    }
    }

}
