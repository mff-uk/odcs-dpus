package cz.opendata.linked.cz.gov.organy;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.URL;
import java.util.LinkedList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import cz.cuni.mff.xrg.odcs.commons.dpu.DPUContext;
import cz.cuni.mff.xrg.odcs.rdf.interfaces.RDFDataUnit;
import cz.cuni.mff.xrg.scraper.lib.template.ParseEntry;
import cz.cuni.mff.xrg.scraper.lib.template.ScrapingTemplate;

/**
 * Specificky scraper pro statni spravu.
 * 
 * @author Jakub Starka
 */

public class Scraper_parser extends ScrapingTemplate{
    
	public Logger logger ;
	public PrintStream ps;
	public DPUContext ctx;
	public RDFDataUnit list, details;
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				xr.parse(new InputSource(new StringReader(doc)));
				numDetails = out.size();
				logger.info("Got " + numDetails + " links to details.");
				current = 0;
				
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (SAXException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

        }
        return out;
    }
    
    @Override
    protected void parse(String doc, String docType, URL url) {
    	if (docType.equals("list"))
    	{
			list.addTriple(list.createURI("http://linked.opendata.cz/ontology/odcs/DataUnit"), list.createURI("http://linked.opendata.cz/ontology/odcs/xmlValue"),list.createLiteral(doc));
    	}
    	if (docType.equals("detail"))
    	{
    		logger.debug("Processing detail " + ++current + "/" + numDetails + ": " + url.toString());
    		details.addTriple(list.createURI("http://linked.opendata.cz/ontology/odcs/DataUnit"), details.createURI("http://linked.opendata.cz/ontology/odcs/xmlValue"),details.createLiteral(doc));
    	}
    }
}
