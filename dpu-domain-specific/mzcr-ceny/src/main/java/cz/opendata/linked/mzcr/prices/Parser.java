package cz.opendata.linked.mzcr.prices;

import java.util.LinkedList;

import org.openrdf.model.URI;
import org.slf4j.Logger;

import cz.cuni.mff.scraper.lib.selector.CssSelector;
import cz.cuni.mff.scraper.lib.template.ParseEntry;
import cz.cuni.mff.scraper.lib.template.ScrapingTemplate;
import org.openrdf.model.ValueFactory;

import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.extension.rdf.simple.WritableSimpleRdf;

/**
 * Specificky scraper pro statni spravu.
 * 
 * @author Jakub Starka
 */
public class Parser extends ScrapingTemplate{
    
    public Logger logger;

    public WritableSimpleRdf output;
    
    @Override
    protected LinkedList<ParseEntry> getLinks(org.jsoup.nodes.Document doc, String docType) {
        LinkedList<ParseEntry> out = new LinkedList<>();
        return out;
    }

    @Override
    protected void parse(org.jsoup.nodes.Document doc, String docType) {
        try {
            parseAndAdd(doc, docType);
        } catch (DPUException ex) {
            logger.error("RDF operation failed", ex);
        }        
    }    
   
    protected void parseAndAdd(org.jsoup.nodes.Document doc, String docType) throws DPUException {
        ValueFactory valueFactory = output.getValueFactory();        
        if (docType == "tab") {            
            CssSelector rowsSelector;
            int i = 0;
            while ((rowsSelector = new CssSelector(doc, "div#ctl00_cphMiddle_lntTabulka_pnlTabulka table tbody tr", i+1)).getValue() != null)
            {
                String kod = new CssSelector(doc, "div#ctl00_cphMiddle_lntTabulka_pnlTabulka table tbody tr td:eq(0) span", i).getValue();
                String nazev = new CssSelector(doc, "div#ctl00_cphMiddle_lntTabulka_pnlTabulka table tbody tr td:eq(1) span", i).getValue();
                String doplnek = new CssSelector(doc, "div#ctl00_cphMiddle_lntTabulka_pnlTabulka table tbody tr td:eq(2) span", i).getValue();
                String price = new CssSelector(doc, "div#ctl00_cphMiddle_lntTabulka_pnlTabulka table tbody tr td:eq(3) span", i).getValue().replace(',', '.').replaceFirst(" Kč", "");
                
                URI rowURI = valueFactory.createURI("http://linked.opendata.cz/resource/sukl/active-ingredient/" + kod);
                URI priceURI = valueFactory.createURI("http://linked.opendata.cz/resource/sukl/active-ingredient/" + kod + "/average-price");
                URI xsdDecimal = valueFactory.createURI("http://www.w3.org/2001/XMLSchema#decimal");
                URI xsdBoolean = valueFactory.createURI("http://www.w3.org/2001/XMLSchema#boolean");
                output.add(rowURI, valueFactory.createURI("http://purl.org/dc/terms/title"), valueFactory.createLiteral(nazev));
                output.add(rowURI, valueFactory.createURI("http://purl.org/dc/terms/description"), valueFactory.createLiteral(doplnek));
                output.add(rowURI, valueFactory.createURI("http://www.w3.org/2004/02/skos/core#notation"), valueFactory.createLiteral(kod));
                output.add(rowURI, valueFactory.createURI("http://linked.opendata.cz/ontology/sukl/isOnMarket"), valueFactory.createLiteral("true",xsdBoolean));
                if (!price.equals("-"))
                {
                    output.add(rowURI, valueFactory.createURI("http://purl.org/goodrelations/v1#hasPriceSpecification"), priceURI);
                    output.add(priceURI, valueFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), valueFactory.createURI("http://purl.org/goodrelations/v1#PriceSpecification"));
                    output.add(priceURI, valueFactory.createURI("http://purl.org/goodrelations/v1#hasCurrencyValue"), valueFactory.createLiteral(price,xsdDecimal));
                    output.add(priceURI, valueFactory.createURI("http://purl.org/goodrelations/v1#hasCurrency"), valueFactory.createLiteral("CZK"));
                }
                i++;
            }
        }
    }
}
