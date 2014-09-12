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

import cz.cuni.mff.xrg.odcs.commons.ontology.OdcsTerms;
import cz.cuni.mff.xrg.odcs.dataunit.file.FileDataUnit;
import cz.cuni.mff.xrg.odcs.dataunit.file.handlers.FileHandler;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.OperationFailedException;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.SimpleRdfRead;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.SimpleRdfWrite;
import cz.cuni.mff.xrg.scraper.lib.template.ParseEntry;
import cz.cuni.mff.xrg.scraper.lib.template.ScrapingTemplate;
import org.openrdf.model.ValueFactory;

/**
 * Specificky scraper pro statni spravu.
 * 
 * @author Jakub Klímek
 */

public class Scraper_parser extends ScrapingTemplate{
    
    public FileDataUnit smlouvy, objednavky, plneni, smlouvy_roky, objednavky_roky, plneni_roky;
    public SimpleRdfWrite smlouvy_meta, objednavky_meta, plneni_meta;
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
        String paramUri = url.toString() + "/param";
        try {
            switch (docType) {
                case "detail-s":
                    logger.debug("Processing smlouva " + ++currentSmlouvy + "/" + numSmlouvy + ": " + url.toString());
                    FileHandler fhs = smlouvy.getRootDir().addNewFile(url.toString());
                    fhs.setContent(doc);
                    final ValueFactory smlouvyValueFactory = smlouvy_meta.getValueFactory();
                    smlouvy_meta.add(smlouvyValueFactory.createURI(url.toString()), 
                            smlouvyValueFactory.createURI(OdcsTerms.DATA_UNIT_FILE_PATH_PREDICATE), 
                            smlouvyValueFactory.createLiteral(fhs.getRootedPath()));
                    smlouvy_meta.add(
                            smlouvyValueFactory.createURI(url.toString()), 
                            smlouvyValueFactory.createURI(OdcsTerms.XSLT_PARAM_PREDICATE), 
                            smlouvyValueFactory.createURI(paramUri));
                    smlouvy_meta.add(smlouvyValueFactory.createURI(paramUri), 
                            smlouvyValueFactory.createURI(OdcsTerms.XSLT_PARAM_NAME_PREDICATE), 
                            smlouvyValueFactory.createLiteral("recordid"));
                    smlouvy_meta.add(smlouvyValueFactory.createURI(paramUri), 
                            smlouvyValueFactory.createURI(OdcsTerms.XSLT_PARAM_VALUE_PREDICATE), 
                            smlouvyValueFactory.createLiteral(url.toString().replaceAll(".*rec-([0-9]+)\\.xml", "$1")));
                    break;
                case "detail-o":
                    logger.debug("Processing objednávka " + ++currentObjednavky + "/" + numObjednavky + ": " + url.toString());
                    FileHandler fho = objednavky.getRootDir().addNewFile(url.toString());
                    fho.setContent(doc);
                    final ValueFactory objednavkyValueFactory = objednavky_meta.getValueFactory();
                    objednavky_meta.add(objednavkyValueFactory.createURI(url.toString()), 
                            objednavkyValueFactory.createURI(OdcsTerms.DATA_UNIT_FILE_PATH_PREDICATE), 
                            objednavkyValueFactory.createLiteral(fho.getRootedPath()));
                    objednavky_meta.add(objednavkyValueFactory.createURI(url.toString()), 
                            objednavkyValueFactory.createURI(OdcsTerms.XSLT_PARAM_PREDICATE), 
                            objednavkyValueFactory.createURI(paramUri));
                    objednavky_meta.add(objednavkyValueFactory.createURI(paramUri), 
                            objednavkyValueFactory.createURI(OdcsTerms.XSLT_PARAM_NAME_PREDICATE), 
                            objednavkyValueFactory.createLiteral("recordid"));
                    objednavky_meta.add(objednavkyValueFactory.createURI(paramUri), 
                            objednavkyValueFactory.createURI(OdcsTerms.XSLT_PARAM_VALUE_PREDICATE), 
                            objednavkyValueFactory.createLiteral(url.toString().replaceAll(".*rec-([0-9]+)\\.xml", "$1")));
                    //objednavky.addTriple(objednavky.createURI("http://linked.opendata.cz/ontology/odcs/DataUnit"), objednavky.createURI("http://linked.opendata.cz/ontology/odcs/xmlValue"),objednavky.createLiteral(doc));
                    break;
                case "detail-p":
                    logger.debug("Processing plnění " + ++currentPlneni + "/" + numPlneni + ": " + url.toString());
                    FileHandler fhp = plneni.getRootDir().addNewFile(url.toString());
                    fhp.setContent(doc);
                    final ValueFactory plneniValueFactory = plneni_meta.getValueFactory();
                    plneni_meta.add(plneniValueFactory.createURI(url.toString()), 
                            plneniValueFactory.createURI(OdcsTerms.DATA_UNIT_FILE_PATH_PREDICATE), 
                            plneniValueFactory.createLiteral(fhp.getRootedPath()));
                    plneni_meta.add(plneniValueFactory.createURI(url.toString()), 
                            plneniValueFactory.createURI(OdcsTerms.XSLT_PARAM_PREDICATE), 
                            plneniValueFactory.createURI(paramUri));
                    plneni_meta.add(plneniValueFactory.createURI(paramUri), 
                            plneniValueFactory.createURI(OdcsTerms.XSLT_PARAM_NAME_PREDICATE), 
                            plneniValueFactory.createLiteral("recordid"));
                    plneni_meta.add(plneniValueFactory.createURI(paramUri), 
                            plneniValueFactory.createURI(OdcsTerms.XSLT_PARAM_VALUE_PREDICATE), 
                            plneniValueFactory.createLiteral(url.toString().replaceAll(".*rec-([0-9]+)\\.xml", "$1")));
                    //plneni.addTriple(smlouvy.createURI("http://linked.opendata.cz/ontology/odcs/DataUnit"), plneni.createURI("http://linked.opendata.cz/ontology/odcs/xmlValue"),plneni.createLiteral(doc));
                    break;
                case "seznamrok-s":
                    logger.debug("Processing yearly list of smlouva " + ++currentSmlouvyRoks + "/" + numSmlouvyRoks + ": " + url.toString());
                    smlouvy_roky.getRootDir().addNewFile(url.toString()).setContent(doc);
                    //smlouvy_roky.addTriple(smlouvy_roky.createURI("http://linked.opendata.cz/ontology/odcs/DataUnit"), smlouvy_roky.createURI("http://linked.opendata.cz/ontology/odcs/xmlValue"),smlouvy_roky.createLiteral(doc));
                    break;
                case "seznamrok-o":
                    logger.debug("Processing yearly list of objednávka " + ++currentObjednavkyRoks + "/" + numObjednavkyRoks + ": " + url.toString());
                    objednavky_roky.getRootDir().addNewFile(url.toString()).setContent(doc);
                    //objednavky_roky.addTriple(objednavky_roky.createURI("http://linked.opendata.cz/ontology/odcs/DataUnit"), objednavky_roky.createURI("http://linked.opendata.cz/ontology/odcs/xmlValue"),objednavky_roky.createLiteral(doc));
                    break;
                case "seznamrok-p":
                    logger.debug("Processing yearly list of plnění " + ++currentPlneniRoks + "/" + numPlneniRoks + ": " + url.toString());
                    plneni_roky.getRootDir().addNewFile(url.toString()).setContent(doc);
                    //plneni_roky.addTriple(smlouvy_roky.createURI("http://linked.opendata.cz/ontology/odcs/DataUnit"), plneni_roky.createURI("http://linked.opendata.cz/ontology/odcs/xmlValue"),plneni_roky.createLiteral(doc));
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
