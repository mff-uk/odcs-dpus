package cz.opendata.linked.cz.gov.organy;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.LinkedList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import cz.cuni.mff.xrg.odcs.rdf.simple.OperationFailedException;
import cz.cuni.mff.xrg.odcs.rdf.simple.SimpleRDF;
import cz.cuni.mff.xrg.scraper.lib.template.ParseEntry;
import cz.cuni.mff.xrg.scraper.lib.template.ScrapingTemplate;
import org.openrdf.model.ValueFactory;

/**
 * Specificky scraper pro statni spravu.
 * 
 * @author Jakub Starka
 */

public class Scraper_parser extends ScrapingTemplate {
    
	public SimpleRDF list, details;
	private int numDetails;
	private int current;
	
	@Override
    protected LinkedList<ParseEntry> getLinks(String doc, String docType) {
        final LinkedList<ParseEntry> out = new LinkedList<>();
        
        if (docType.equals("init"))
        {
        	XMLReader xr = null;
			try {
				SAXParserFactory factory = SAXParserFactory.newInstance();
			    SAXParser sp = factory.newSAXParser();
			    xr = sp.getXMLReader(); 

			    OrganyListParser handler = new OrganyListParser(out);
			    xr.setContentHandler(handler);				
			} catch (SAXException e) {
				logger.error(e.getLocalizedMessage());
			} catch (ParserConfigurationException e) {
				logger.error(e.getLocalizedMessage());
			}
			
			try {
				xr.parse(new InputSource(new StringReader(doc)));
				numDetails = out.size();
				logger.info("Got " + numDetails + " links to details.");
				current = 0;
				
			} catch (IOException | SAXException e) {
				logger.error("Failed to parse document", e);
			}

        }
        return out;
    }
    
    @Override
    protected void parse(String doc, String docType, URL url) {
		try {
    	if (docType.equals("init"))
    	{
			final ValueFactory valueFactory = list.getValueFactory();
			list.add(valueFactory.createURI("http://linked.opendata.cz/ontology/odcs/DataUnit"), 
					valueFactory.createURI("http://linked.opendata.cz/ontology/odcs/xmlValue"),
					valueFactory.createLiteral(doc));
    	}
    	if (docType.equals("detail"))
    	{
    		logger.debug("Processing detail " + ++current + "/" + numDetails + ": " + url.toString());
			final ValueFactory valueFactory = details.getValueFactory();
    		details.add(valueFactory.createURI("http://linked.opendata.cz/ontology/odcs/DataUnit"), 
					valueFactory.createURI("http://linked.opendata.cz/ontology/odcs/xmlValue"),
					valueFactory.createLiteral(doc));
    	}
		} catch (OperationFailedException ex) {
			logger.error("Failed to add triple into storage.", ex);
		}
    }
}
