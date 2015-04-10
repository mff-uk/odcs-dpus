package cz.opendata.linked.cz.gov.nkod;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.LinkedList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.FileUtils;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
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

    public WritableSimpleFiles nkod, nkod_roky;
    public int numNkod = 0;
    public int numNkodRoks = 0;
    public int currentNkod = 0;
    public int currentNkodRoks = 0;
    
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
                        numNkodRoks = out.size();
                        logger.info("Got " + numNkodRoks + " links to yearly lists of \"Nkod\".");
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
                    numNkod += newlinks;
                    logger.info("Got " + newlinks + " new links to \"Nkod\", " + numNkod + " total" );
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
                    logger.debug("Processing smlouva " + ++currentNkod + "/" + numNkod + ": " + url.toString());

                    File fs = nkod.create(url.toString());
                    FileUtils.writeStringToFile(fs, doc, "UTF-8");
                    addXsltParameter(url.toString(), "recordid", url.toString().replaceAll(".*rec-([^\\.]+)\\.xml", "$1"));

                    break;
                case "seznamrok-s":
                    logger.debug("Processing yearly list of smlouva " + ++currentNkodRoks + "/" + numNkodRoks + ": " + url.toString());

                    File fss = nkod_roky.create(url.toString());
                    FileUtils.writeStringToFile(fss, doc, "UTF-8");

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
        metadata.add(root, RDF.TYPE, ExtractorOntology.XSLT_CLASS);
        // File info.
        URI fileInfo = valueFactory.createURI(symbolicName);
        metadata.add(fileInfo, RDF.TYPE, ExtractorOntology.XSLT_FILEINFO_CLASS);
        metadata.add(fileInfo,
                ExtractorOntology.XSLT_FILEINFO_SYMBOLICNAME_PREDICATE,
                valueFactory.createLiteral(symbolicName));
        metadata.add(fileInfo, RDF.TYPE, ExtractorOntology.XSLT_FILEINFO_CLASS);
        // Xslt parameters.
        URI parameters = valueFactory.createURI(symbolicName + "/" + key);
        metadata.add(parameters, RDF.TYPE, ExtractorOntology.XSLT_PARAM_CLASS);
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
