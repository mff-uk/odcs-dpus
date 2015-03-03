package cz.opendata.linked.cz.gov.smlouvy;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.FileUtils;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import cz.cuni.mff.xrg.scraper.lib.template.ParseEntry;
import cz.cuni.mff.xrg.scraper.lib.template.ScrapingTemplate;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.extension.files.simple.WritableSimpleFiles;
import eu.unifiedviews.helpers.dpu.extension.rdf.simple.SimpleRdfException;
import eu.unifiedviews.helpers.dpu.extension.rdf.simple.WritableSimpleRdf;

/**
 * Specificky scraper pro statni spravu.
 * 
 * @author Jakub Klímek
 */

public class Scraper_parser extends ScrapingTemplate{
    
    private static final Logger LOG = LoggerFactory.getLogger(Scraper_parser.class);

    public WritableSimpleFiles smlouvy, objednavky, plneni, smlouvy_roky, objednavky_roky, plneni_roky;
    public int numSmlouvy = 0, numObjednavky = 0, numPlneni = 0;
    public int numSmlouvyRoks = 0, numObjednavkyRoks = 0, numPlneniRoks = 0;
    public int currentSmlouvy = 0, currentObjednavky = 0, currentPlneni = 0;
    public int currentSmlouvyRoks = 0, currentObjednavkyRoks = 0, currentPlneniRoks = 0;
    
    public WritableSimpleRdf metadata;

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

                    File fs = smlouvy.create(url.toString());
                    FileUtils.writeStringToFile(fs, doc, "UTF-8");
                    addXsltParameter(url.toString(), "recordid", url.toString().replaceAll(".*rec-([0-9]+)\\.xml", "$1"));

                    break;
                case "detail-o":
                    logger.debug("Processing objednávka " + ++currentObjednavky + "/" + numObjednavky + ": " + url.toString());

                    File fo = objednavky.create(url.toString());
                    FileUtils.writeStringToFile(fo, doc, "UTF-8");
                    addXsltParameter(url.toString(), "recordid", url.toString().replaceAll(".*rec-([0-9]+)\\.xml", "$1"));
                    
                    break;
                case "detail-p":
                    logger.debug("Processing plnění " + ++currentPlneni + "/" + numPlneni + ": " + url.toString());

                    File fp = plneni.create(url.toString());
                    FileUtils.writeStringToFile(fp, doc, "UTF-8");
                    addXsltParameter(url.toString(), "recordid", url.toString().replaceAll(".*rec-([0-9]+)\\.xml", "$1"));
                    
                    break;
                case "seznamrok-s":
                    logger.debug("Processing yearly list of smlouva " + ++currentSmlouvyRoks + "/" + numSmlouvyRoks + ": " + url.toString());

                    File fss = smlouvy_roky.create(url.toString());
                    FileUtils.writeStringToFile(fss, doc, "UTF-8");

                    break;
                case "seznamrok-o":
                    logger.debug("Processing yearly list of objednávka " + ++currentObjednavkyRoks + "/" + numObjednavkyRoks + ": " + url.toString());

                    File fso = objednavky_roky.create(url.toString());
                    FileUtils.writeStringToFile(fso, doc, "UTF-8");

                    break;
                case "seznamrok-p":
                    logger.debug("Processing yearly list of plnění " + ++currentPlneniRoks + "/" + numPlneniRoks + ": " + url.toString());

                    File fsp = plneni_roky.create(url.toString());
                    FileUtils.writeStringToFile(fsp, doc, "UTF-8");

                    break;
            }
        }
        catch (Exception e)
        {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

protected void addXsltParameter(String symbolicName, String key, String value) throws SimpleRdfException, DPUException {
        LOG.info("addXsltParameter: {} {} {}", symbolicName, key, value);
        final ValueFactory valueFactory = ValueFactoryImpl.getInstance();
        // Configuration class.
        URI root = valueFactory.createURI("http://localhost/resource/Metadata");
        metadata.add(root,
                valueFactory.createURI("http://www.w3.org/2000/01/rdf-schema#type"),
                ExtractorOntology.XSLT_CLASS);
        // File info.
        URI fileInfo = valueFactory.createURI(symbolicName);
        metadata.add(fileInfo,
                valueFactory.createURI("http://www.w3.org/2000/01/rdf-schema#type"),
                ExtractorOntology.XSLT_FILEINFO_CLASS);
        metadata.add(fileInfo,
                ExtractorOntology.XSLT_FILEINFO_SYMBOLICNAME_PREDICATE,
                valueFactory.createLiteral(symbolicName));
        metadata.add(fileInfo,
                valueFactory.createURI("http://www.w3.org/2000/01/rdf-schema#type"),
                ExtractorOntology.XSLT_FILEINFO_CLASS);
        // Xslt parameters.
        URI parameters = valueFactory.createURI(symbolicName + "/" + key);
        metadata.add(parameters,
                valueFactory.createURI("http://www.w3.org/2000/01/rdf-schema#type"),
                ExtractorOntology.XSLT_PARAM_CLASS);
        metadata.add(parameters,
                ExtractorOntology.XSLT_PARAM_NAME_PREDICATE,
                valueFactory.createLiteral(key));
        metadata.add(parameters,
                ExtractorOntology.XSLT_PARAM_VALUE_PREDICATE,
                valueFactory.createLiteral(value));
        // Class connection.
        metadata.add(root,
                ExtractorOntology.XSLT_FILEINFO_PREDICATE,
                fileInfo);
        metadata.add(fileInfo,
                ExtractorOntology.XSLT_FILEINFO_PARAM_PREDICATE,
                parameters);
   }


}
