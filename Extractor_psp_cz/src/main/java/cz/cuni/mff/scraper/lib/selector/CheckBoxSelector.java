package cz.cuni.mff.scraper.lib.selector;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * @deprecated Never worked :)
 * 
 * @author Jakub
 */
public class CheckBoxSelector extends CssSelector {

    public CheckBoxSelector(Document doc, String string) {
	this(doc, string, null);
    }
    
    public CheckBoxSelector(Document doc, String string, String checked) {
        super(doc, string, checked);
    }

    public String getValue() {
        Elements e = d.select("input[name=" + selector + "]");
        
	if (e.size() != 0) {
            usageMap.put(selector, usageMap.get(selector) + 1);
            for (int i = 0; i < e.size(); i ++) {
                if (e.get(i).hasAttr("checked") && e.get(i).attr("checked").equals("checked")) {
                    return e.get(i).attr("value");
                }
            }
	}
        return null;
    }

}
