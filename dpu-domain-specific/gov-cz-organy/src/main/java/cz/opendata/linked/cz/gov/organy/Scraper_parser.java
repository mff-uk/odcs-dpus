package cz.opendata.linked.cz.gov.organy;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.util.LinkedList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.FileUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import cz.cuni.mff.xrg.scraper.lib.template.ParseEntry;
import cz.cuni.mff.xrg.scraper.lib.template.ScrapingTemplate;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;

public class Scraper_parser extends ScrapingTemplate {
    
    //public SimpleRdfWrite list, details;
    public WritableFilesDataUnit list, details;
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
                logger.error(e.getLocalizedMessage(), e);
            } catch (ParserConfigurationException e) {
                logger.error(e.getLocalizedMessage(), e);
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
        if (docType.equals("init"))
        {
        	try {
				//logger.debug(doc);
				File f = new File(URI.create(list.addNewFile(url.toString())));
				FileUtils.writeStringToFile(f, doc);
			} catch (DataUnitException e) {
				logger.error(e.getLocalizedMessage(),e);
			} catch (IOException e) {
				logger.error(e.getLocalizedMessage(),e);
			}
        }
        if (docType.equals("detail"))
        {
            logger.debug("Processing detail " + ++current + "/" + numDetails + ": " + url.toString());
        	try {
				//logger.debug(doc);
				File f = new File(URI.create(details.addNewFile(url.toString())));
				FileUtils.writeStringToFile(f, doc);
			} catch (DataUnitException e) {
				logger.error(e.getLocalizedMessage(),e);
			} catch (IOException e) {
				logger.error(e.getLocalizedMessage(),e);
			}
        }
    }
}
