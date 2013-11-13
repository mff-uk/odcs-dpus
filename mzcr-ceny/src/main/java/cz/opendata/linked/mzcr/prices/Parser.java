package cz.opendata.linked.mzcr.prices;

import java.util.LinkedList;

import org.openrdf.model.URI;
import org.slf4j.Logger;

import cz.cuni.mff.scraper.lib.selector.CssSelector;
import cz.cuni.mff.scraper.lib.template.ParseEntry;
import cz.cuni.mff.scraper.lib.template.ScrapingTemplate;
import cz.cuni.mff.xrg.odcs.rdf.interfaces.RDFDataUnit;

/**
 * Specificky scraper pro statni spravu.
 * 
 * @author Jakub Starka
 */

public class Parser extends ScrapingTemplate{
    
	public Logger logger;
	public RDFDataUnit output;
    
    @Override
    protected LinkedList<ParseEntry> getLinks(org.jsoup.nodes.Document doc, String docType) {
        LinkedList<ParseEntry> out = new LinkedList<>();
        return out;
    }
    
   
    @Override
    protected void parse(org.jsoup.nodes.Document doc, String docType) {
        if (docType == "tab") {
        	
        	CssSelector rowsSelector;
            int i = 0;
            while ((rowsSelector = new CssSelector(doc, "div#ctl00_cphMiddle_lntTabulka_pnlTabulka table tbody tr", i+1)).getValue() != null)
            {
            	String kod = new CssSelector(doc, "div#ctl00_cphMiddle_lntTabulka_pnlTabulka table tbody tr td:eq(0) span", i).getValue();
            	String nazev = new CssSelector(doc, "div#ctl00_cphMiddle_lntTabulka_pnlTabulka table tbody tr td:eq(1) span", i).getValue();
            	String doplnek = new CssSelector(doc, "div#ctl00_cphMiddle_lntTabulka_pnlTabulka table tbody tr td:eq(2) span", i).getValue();
            	String price = new CssSelector(doc, "div#ctl00_cphMiddle_lntTabulka_pnlTabulka table tbody tr td:eq(3) span", i).getValue().replace(',', '.').replaceFirst(" Kč", "");
            	
            	URI rowURI = output.createURI("http://linked.opendata.cz/resource/sukl/active-ingredient/" + kod);
            	URI priceURI = output.createURI("http://linked.opendata.cz/resource/sukl/active-ingredient/" + kod + "/average-price");
            	URI xsdDecimal = output.createURI("http://www.w3.org/2001/XMLSchema#decimal");
            	URI xsdBoolean = output.createURI("http://www.w3.org/2001/XMLSchema#boolean");
            	output.addTriple(rowURI, output.createURI("http://purl.org/dc/terms/title"), output.createLiteral(nazev));
            	output.addTriple(rowURI, output.createURI("http://purl.org/dc/terms/description"), output.createLiteral(doplnek));
            	output.addTriple(rowURI, output.createURI("http://www.w3.org/2004/02/skos/core#notation"), output.createLiteral(kod));
            	output.addTriple(rowURI, output.createURI("http://linked.opendata.cz/ontology/sukl/isOnMarket"), output.createLiteral("true",xsdBoolean));
            	if (!price.equals("-"))
            	{
		        	output.addTriple(rowURI, output.createURI("http://purl.org/goodrelations/v1#hasPriceSpecification"), priceURI);
		        	output.addTriple(priceURI, output.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), output.createURI("http://purl.org/goodrelations/v1#PriceSpecification"));
		        	output.addTriple(priceURI, output.createURI("http://purl.org/goodrelations/v1#hasCurrencyValue"), output.createLiteral(price,xsdDecimal));
		        	output.addTriple(priceURI, output.createURI("http://purl.org/goodrelations/v1#hasCurrency"), output.createLiteral("CZK"));
            	}
            	i++;
            }
        }
    }
}
