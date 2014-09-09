package cz.opendata.linked.ares;
import java.net.URL;
import java.util.LinkedList;

import org.slf4j.Logger;

import cz.cuni.mff.xrg.scraper.lib.template.ParseEntry;
import cz.cuni.mff.xrg.scraper.lib.template.ScrapingTemplate;

/**
 * Specificky scraper pro statni spravu.
 * 
 * @author Jakub Starka
 */

public class Scraper_parser extends ScrapingTemplate{
    
    private static String icoBEprefix = "http://linked.opendata.cz/resource/business-entity/CZ";
    public Logger logger ;
    
    @Override
    protected LinkedList<ParseEntry> getLinks(org.jsoup.nodes.Document doc, String docType) {
        LinkedList<ParseEntry> out = new LinkedList<>();

        return out;
    }
    
    private String escapeString(String original)
    {
        return original.replace("\n", " ").replace("<","").replace(">","").replace("\\","\\\\").replace("\"", "\\\"").replace("„", "\\\"").replace("“", "\\\"");
    }
    
    @Override
    protected void parse(org.jsoup.nodes.Document doc, String docType, URL url) {

    }
}
