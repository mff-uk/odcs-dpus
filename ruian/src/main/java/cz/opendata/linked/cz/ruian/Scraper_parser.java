package cz.opendata.linked.cz.ruian;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;

import cz.cuni.mff.xrg.odcs.rdf.interfaces.RDFDataUnit;
import cz.cuni.mff.xrg.scraper.lib.template.ParseEntry;
import cz.cuni.mff.xrg.scraper.lib.template.ScrapingTemplate;

/**
 * Scraper pro RUIAN
 * 
 * @author Jakub Klímek
 */

public class Scraper_parser extends ScrapingTemplate{
    
	public RDFDataUnit obce;
	private int numDetails;
	private int current;
	
	@Override
    protected LinkedList<ParseEntry> getLinks(String doc, String docType) {
        final LinkedList<ParseEntry> out = new LinkedList<>();
        
        if (docType.equals("init"))
        {
        	String[] lines = doc.split("\\r\\n");
        	numDetails = lines.length;
        	logger.info("I see " + numDetails + " files");
        	for (String line : lines)
        	{
        		try {
					out.add(new ParseEntry(new URL(line),"obec","gz"));
				} catch (MalformedURLException e) {
					logger.warn(e.getLocalizedMessage());
				}
        	}
        }
        return out;
    }
    
    @Override
    protected void parse(String doc, String docType, URL url) {
    	if (docType.equals("init"))
    	{
			
    	}
    	else if (docType.equals("obec"))
    	{
    		logger.debug("Processing detail " + ++current + "/" + numDetails + ": " + url.toString());
    		obce.addTriple(obce.createURI("http://linked.opendata.cz/ontology/odcs/DataUnit"), obce.createURI("http://linked.opendata.cz/ontology/odcs/xmlValue"),obce.createLiteral(doc));
    	}
    }
}
