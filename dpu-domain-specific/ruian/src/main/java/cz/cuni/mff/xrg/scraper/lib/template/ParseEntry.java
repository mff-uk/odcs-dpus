package cz.cuni.mff.xrg.scraper.lib.template;

import java.net.URL;

/**
 * One parsing entry with URL and document type.
 * 
 * @author Jakub Starka
 */
public class ParseEntry {
    public URL url;
    public String type;
    public String datatype;

    /**
     * @param url Scraping URL.
     * @param type Document type.
     */
    public ParseEntry(URL url, String type, String datatype) {
        this.url = url;
        this.type = type;
        this.datatype = datatype;
    }

    @Override
    public boolean equals(Object o) {
        return (
            (o instanceof ParseEntry) && 
            ((ParseEntry)o).type.equals(type) &&
            ((ParseEntry)o).url.equals(url) &&
            ((ParseEntry)o).datatype.equals(datatype)
        );
    }
    
    
    
}
