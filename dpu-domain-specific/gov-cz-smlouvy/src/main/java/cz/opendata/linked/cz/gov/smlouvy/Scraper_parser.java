package cz.opendata.linked.cz.gov.smlouvy;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.FileUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import cz.cuni.mff.xrg.scraper.lib.template.ParseEntry;
import cz.cuni.mff.xrg.scraper.lib.template.ScrapingTemplate;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.OperationFailedException;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.helpers.dataunit.maphelper.MapHelpers;

/**
 * Specificky scraper pro statni spravu.
 * 
 * @author Jakub Klímek
 */

public class Scraper_parser extends ScrapingTemplate{
    
    public WritableFilesDataUnit smlouvy, objednavky, plneni, smlouvy_roky, objednavky_roky, plneni_roky;
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
                
            } catch (IOException | SAXException e) {
                logger.error("Failed to parse document", e);
            }

        }
        return out;
    }
    
    @Override
    protected void parse(String doc, String docType, URL url) {
        try {
            switch (docType) {
                case "detail-s":
                    logger.debug("Processing smlouva " + ++currentSmlouvy + "/" + numSmlouvy + ": " + url.toString());
                    
                    File fs = new File(URI.create(smlouvy.addNewFile(url.toString())));
					FileUtils.writeStringToFile(fs, doc, "UTF-8");
                    
                    Map<String, String> xsltParamsMapS = new HashMap<String, String>();
                    xsltParamsMapS.put("recordid", url.toString().replaceAll(".*rec-([0-9]+)\\.xml", "$1"));
                    MapHelpers.putMap(smlouvy, url.toString(), "xsltParameters", xsltParamsMapS);
                    
                    break;
                case "detail-o":
                    logger.debug("Processing objednávka " + ++currentObjednavky + "/" + numObjednavky + ": " + url.toString());

                    File fo = new File(URI.create(objednavky.addNewFile(url.toString())));
					FileUtils.writeStringToFile(fo, doc, "UTF-8");
                    
                    Map<String, String> xsltParamsMapO = new HashMap<String, String>();
                    xsltParamsMapO.put("recordid", url.toString().replaceAll(".*rec-([0-9]+)\\.xml", "$1"));
                    MapHelpers.putMap(objednavky, url.toString(), "xsltParameters", xsltParamsMapO);
                    
                    break;
                case "detail-p":
                    logger.debug("Processing plnění " + ++currentPlneni + "/" + numPlneni + ": " + url.toString());

                    File fp = new File(URI.create(plneni.addNewFile(url.toString())));
					FileUtils.writeStringToFile(fp, doc, "UTF-8");
                    
                    Map<String, String> xsltParamsMapP = new HashMap<String, String>();
                    xsltParamsMapP.put("recordid", url.toString().replaceAll(".*rec-([0-9]+)\\.xml", "$1"));
                    MapHelpers.putMap(plneni, url.toString(), "xsltParameters", xsltParamsMapP);
                    
                    break;
                case "seznamrok-s":
                    logger.debug("Processing yearly list of smlouva " + ++currentSmlouvyRoks + "/" + numSmlouvyRoks + ": " + url.toString());
                    File fss = new File(URI.create(smlouvy_roky.addNewFile(url.toString())));
					FileUtils.writeStringToFile(fss, doc, "UTF-8");
                    break;
                case "seznamrok-o":
                    logger.debug("Processing yearly list of objednávka " + ++currentObjednavkyRoks + "/" + numObjednavkyRoks + ": " + url.toString());
                    File fso = new File(URI.create(objednavky_roky.addNewFile(url.toString())));
					FileUtils.writeStringToFile(fso, doc, "UTF-8");
                    break;
                case "seznamrok-p":
                    logger.debug("Processing yearly list of plnění " + ++currentPlneniRoks + "/" + numPlneniRoks + ": " + url.toString());
                    File fsp = new File(URI.create(plneni_roky.addNewFile(url.toString())));
					FileUtils.writeStringToFile(fsp, doc, "UTF-8");
                    break;
            }
        }
        catch (OperationFailedException ex) {
            logger.error("Failed to add triple into storage.", ex);
        }
        catch (Exception e)
        {
            logger.error(e.getLocalizedMessage(), e);
        }
    }
}
