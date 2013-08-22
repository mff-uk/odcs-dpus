package cz.cuni.mff.scraper.lib.template;

import java.net.URL;

/**
 * One parsing entry with URL and document type.
 * 
 * @author Jakub Starka
 */
public class ParseEntry {
    public URL url;
    public String type;

    /**
     * @param url Scraping URL.
     * @param type Document type.
     */
    public ParseEntry(URL url, String type) {
        this.url = url;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        return (
            (o instanceof ParseEntry) && 
            ((ParseEntry)o).type.equals(type) &&
            ((ParseEntry)o).url.equals(url)    
        );
    }
    
    
    
}
