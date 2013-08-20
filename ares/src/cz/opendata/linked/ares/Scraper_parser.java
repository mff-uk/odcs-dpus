package cz.opendata.linked.ares;
import cz.mff.cuni.scraper.lib.selector.CssSelector;
import cz.mff.cuni.scraper.lib.template.ParseEntry;
import cz.mff.cuni.scraper.lib.template.ScrapingTemplate;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Specificky scraper pro statni spravu.
 * 
 * @author Jakub Starka
 */

public class Scraper_parser extends ScrapingTemplate{
    
	private static String icoBEprefix = "http://linked.opendata.cz/resource/business-entity/CZ";
    
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
