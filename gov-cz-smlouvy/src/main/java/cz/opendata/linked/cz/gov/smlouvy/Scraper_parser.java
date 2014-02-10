package cz.opendata.linked.cz.gov.smlouvy;
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

import cz.cuni.mff.xrg.odcs.rdf.interfaces.RDFDataUnit;
import cz.cuni.mff.xrg.scraper.lib.template.ParseEntry;
import cz.cuni.mff.xrg.scraper.lib.template.ScrapingTemplate;

/**
 * Specificky scraper pro statni spravu.
 * 
 * @author Jakub Klímek
 */

public class Scraper_parser extends ScrapingTemplate{
    
	public RDFDataUnit smlouvy, objednavky, plneni, smlouvy_roky, objednavky_roky, plneni_roky;
	public int numSmlouvy = 0, numObjednavky = 0, numPlneni = 0;
	public int numSmlouvyRoks = 0, numObjednavkyRoks = 0, numPlneniRoks = 0;
	public int currentSmlouvy = 0, currentObjednavky = 0, currentPlneni = 0;
	public int currentSmlouvyRoks = 0, currentObjednavkyRoks = 0, currentPlneniRoks = 0;
	
	@Override
    protected LinkedList<ParseEntry> getLinks(String doc, String docType) {
        final LinkedList<ParseEntry> out = new LinkedList<>();
    	String type = docType.substring(docType.indexOf('-') + 1);

        if (docType.startsWith("init"))
        {
        	XMLReader xr = null;
			try {
				SAXParserFactory factory = SAXParserFactory.newInstance();
			    SAXParser sp = factory.newSAXParser();
			    xr = sp.getXMLReader(); 

			    XMLListParser handler = new XMLListParser(out, type);
			    xr.setContentHandler(handler);				
			} catch (SAXException e) {
				logger.error(e.getLocalizedMessage());
			} catch (ParserConfigurationException e) {
				logger.error(e.getLocalizedMessage());
			}
			
			try {
				xr.parse(new InputSource(new StringReader(doc)));
				
				switch (type) {
					case "s":
						numSmlouvyRoks = out.size();
						logger.info("Got " + numSmlouvyRoks + " links to yearly lists of \"Smlouvy\".");
						break;
					case "o":
						numObjednavkyRoks = out.size();
						logger.info("Got " + numObjednavkyRoks + " links to yearly lists of \"Objednávky\".");
						break;
					case "p":
						numPlneniRoks = out.size();
						logger.info("Got " + numPlneniRoks + " links to yearly lists of \"Plnění\".");
						break;
				}
			} catch (IOException e1) {
				logger.error(e1.getLocalizedMessage());
			} catch (SAXException e1) {
				logger.error(e1.getLocalizedMessage());
			}

        }
        if (docType.startsWith("seznamrok"))
        {
        	XMLReader xr = null;
			try {
				SAXParserFactory factory = SAXParserFactory.newInstance();
			    SAXParser sp = factory.newSAXParser();
			    xr = sp.getXMLReader(); 

			    XMLYearListParser handler = new XMLYearListParser(out, type);
			    xr.setContentHandler(handler);				
			} catch (SAXException e) {
				logger.error(e.getLocalizedMessage());
			} catch (ParserConfigurationException e) {
				logger.error(e.getLocalizedMessage());
			}
			
			try {
				xr.parse(new InputSource(new StringReader(doc)));
				int newlinks = out.size();
				switch (type) {
				case "s":
					numSmlouvy += newlinks;
					logger.info("Got " + newlinks + " new links to \"Smlouvy\", " + numSmlouvy + " total" );
					break;
				case "o":
					numObjednavky += newlinks;
					logger.info("Got " + newlinks + " new links to \"Objednávky\", " + numObjednavky + " total" );
					break;
				case "p":
					numPlneni += newlinks;
					logger.info("Got " + newlinks + " new links to \"Plnění\", " + numPlneni + " total" );
					break;
			}
				
			} catch (IOException e1) {
				logger.error(e1.getLocalizedMessage());
			} catch (SAXException e1) {
				logger.error(e1.getLocalizedMessage());
			}

        }
        return out;
    }
    
    @Override
    protected void parse(String doc, String docType, URL url) {
    	switch (docType) {
    		case "detail-s":
        		logger.debug("Processing smlouva " + ++currentSmlouvy + "/" + numSmlouvy + ": " + url.toString());
        		smlouvy.addTriple(smlouvy.createURI("http://linked.opendata.cz/ontology/odcs/DataUnit"), smlouvy.createURI("http://linked.opendata.cz/ontology/odcs/xmlValue"),smlouvy.createLiteral(doc));
        		break;
    		case "detail-o":
        		logger.debug("Processing objednávka " + ++currentObjednavky + "/" + numObjednavky + ": " + url.toString());
        		objednavky.addTriple(objednavky.createURI("http://linked.opendata.cz/ontology/odcs/DataUnit"), objednavky.createURI("http://linked.opendata.cz/ontology/odcs/xmlValue"),objednavky.createLiteral(doc));
        		break;
    		case "detail-p":
        		logger.debug("Processing plnění " + ++currentPlneni + "/" + numPlneni + ": " + url.toString());
        		plneni.addTriple(smlouvy.createURI("http://linked.opendata.cz/ontology/odcs/DataUnit"), plneni.createURI("http://linked.opendata.cz/ontology/odcs/xmlValue"),plneni.createLiteral(doc));
        		break;
    		case "seznamrok-s":
        		logger.debug("Processing yearly list of smlouva " + ++currentSmlouvyRoks + "/" + numSmlouvyRoks + ": " + url.toString());
        		smlouvy_roky.addTriple(smlouvy_roky.createURI("http://linked.opendata.cz/ontology/odcs/DataUnit"), smlouvy_roky.createURI("http://linked.opendata.cz/ontology/odcs/xmlValue"),smlouvy_roky.createLiteral(doc));
        		break;
    		case "seznamrok-o":
        		logger.debug("Processing yearly list of objednávka " + ++currentObjednavkyRoks + "/" + numObjednavkyRoks + ": " + url.toString());
        		objednavky_roky.addTriple(objednavky_roky.createURI("http://linked.opendata.cz/ontology/odcs/DataUnit"), objednavky_roky.createURI("http://linked.opendata.cz/ontology/odcs/xmlValue"),objednavky_roky.createLiteral(doc));
        		break;
    		case "seznamrok-p":
        		logger.debug("Processing yearly list of plnění " + ++currentPlneniRoks + "/" + numPlneniRoks + ": " + url.toString());
        		plneni_roky.addTriple(smlouvy_roky.createURI("http://linked.opendata.cz/ontology/odcs/DataUnit"), plneni_roky.createURI("http://linked.opendata.cz/ontology/odcs/xmlValue"),plneni_roky.createLiteral(doc));
        		break;
    	}
    }
}
