package cz.opendata.linked.cz.mzp.ippc;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.SKOS;
import org.slf4j.Logger;

import cz.cuni.mff.xrg.scraper.lib.template.ParseEntry;
import cz.cuni.mff.xrg.scraper.lib.template.ScrapingTemplate;

/**
 * 
 * 
 * @author Jakub Klímek
 */

public class Parser extends ScrapingTemplate{
    
	public Logger logger;
    private int count = 0;
    private int total;
    
    static final String schemaorg = "http://schema.org/";
    static final String dcterms = "http://purl.org/dc/terms/";
    static final String owl = "http://www.w3.org/2002/07/owl#";
    static final String mzpbrid = "http://linked.opendata.cz/resource/domain/mzp.cz/zarizeni/id/";
    static final String mzpbr = "http://linked.opendata.cz/resource/domain/mzp.cz/zarizeni/";
    static final String mzponto = "http://linked.opendata.cz/ontology/domain/mzp.cz/";
    static final String ldbe = "http://linked.opendata.cz/resource/business-entity/CZ";
    static final String mzpbe = "http://linked.opendata.cz/resource/domain/mzp.cz/provozovatel/";
    static final String gr = "http://purl.org/goodrelations/v1#";
    static final String adms = "http://www.w3.org/ns/adms#";
    static final String xsd = "http://www.w3.org/2001/XMLSchema#";

    @Override
    protected LinkedList<ParseEntry> getLinks(org.jsoup.nodes.Document doc, String docType, URL url) {
        LinkedList<ParseEntry> out = new LinkedList<>();
        switch (docType) {
        case "docview":
        	try {
				out.add(new ParseEntry(new URL("http://www.mzp.cz" + doc.getElementById("view:_id1:_id2:facetMiddle:_id19:link1").attr("href")), "doc"));
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	break;
        case "provozovna":
        	
        	break;
        }
        return out;
    }

    private String uriSlug(String input)
    {
    	return input.toLowerCase().replace(" ", "-").replace(".", "-").replace("–", "-").replace(",", "-").replace("(", "-").replace("§", "-").replace("*", "-").replace("/", "-").replace(")", "-").replace("--", "-").replace("--", "-");
    }
    
    @Override
    protected void parse(org.jsoup.nodes.Document doc, String docType, URL url) {
        URI s_Organization = outputDataUnit.createURI(schemaorg + "Organization");
        URI gr_BusinessEntity = outputDataUnit.createURI(gr + "BusinessEntity");
        URI gr_legalName = outputDataUnit.createURI(gr + "legalName");
        URI adms_Identifier = outputDataUnit.createURI(adms + "Identifier");
        URI adms_identifier = outputDataUnit.createURI(adms + "identifier");
        URI s_Place = outputDataUnit.createURI(schemaorg + "Place");
        URI s_name = outputDataUnit.createURI(schemaorg + "name");
        URI s_hasPOS = outputDataUnit.createURI(schemaorg + "hasPOS");
        URI s_CheckAction = outputDataUnit.createURI(schemaorg + "CheckAction");
        URI s_PostalAddress = outputDataUnit.createURI(schemaorg + "PostalAddress");
        URI s_object = outputDataUnit.createURI(schemaorg + "object");
        URI s_geo = outputDataUnit.createURI(schemaorg + "geo");
        //URI s_result = outputDataUnit.createURI(schemaorg + "result");
        URI s_address = outputDataUnit.createURI(schemaorg + "address");
        URI s_addressLocality = outputDataUnit.createURI(schemaorg + "addressLocality");
        URI s_addressRegion = outputDataUnit.createURI(schemaorg + "addressRegion");
        URI s_postalCode = outputDataUnit.createURI(schemaorg + "postalCode");
        URI s_streetAddress = outputDataUnit.createURI(schemaorg + "streetAddress");
        URI s_longitude = outputDataUnit.createURI(schemaorg + "longitude");
        URI s_latitude = outputDataUnit.createURI(schemaorg + "latitude");
        URI s_GeoCoordinates = outputDataUnit.createURI(schemaorg + "GeoCoordinates");
        URI s_location = outputDataUnit.createURI(schemaorg + "location");
        URI s_startTime = outputDataUnit.createURI(schemaorg + "startTime");
        URI s_endTime = outputDataUnit.createURI(schemaorg + "endTime");
        URI s_instrument = outputDataUnit.createURI(schemaorg + "instrument");
        URI s_additionalType = outputDataUnit.createURI(schemaorg + "additionalType");
        URI owl_sameAs = outputDataUnit.createURI(owl + "sameAs");
        URI xsd_decimal = outputDataUnit.createURI(xsd + "decimal");
        URI xsd_dateTime = outputDataUnit.createURI(xsd + "dateTime");
        URI xsd_gYear = outputDataUnit.createURI(xsd + "gYear");
        //URI dcterms_source = outputDataUnit.createURI(dcterms + "source");
        URI ICScheme = outputDataUnit.createURI("http://linked.opendata.cz/resource/concept-scheme/CZ-ICO");
        URI chemScheme = outputDataUnit.createURI(mzponto + "chemicals/ConceptScheme");
    	
		URI ovzdusiURI = outputDataUnit.createURI(mzponto + "UnikDoOvzdusi");
		URI vodaURI = outputDataUnit.createURI(mzponto + "UnikDoVody");
		URI pudaURI = outputDataUnit.createURI(mzponto + "UnikDoPudy");
		URI prenosOdpadVodaURI = outputDataUnit.createURI(mzponto + "PrenosOdpadniVody");
		URI prenosOdpadyURI = outputDataUnit.createURI(mzponto + "PrenosOdpady");

		URI mereniURI = outputDataUnit.createURI(mzponto + "Mereni");
		URI odhadURI = outputDataUnit.createURI(mzponto + "Odhad");
		URI vypocetURI = outputDataUnit.createURI(mzponto + "Vypocet");
		
		URI recyklaceURI = outputDataUnit.createURI(mzponto + "Recyklace");
		URI odstraneniURI = outputDataUnit.createURI(mzponto + "Odstraneni");
		URI urceni_odpadu = outputDataUnit.createURI(mzponto + "urceniOdpadu");
		
		logger.trace("Processing: " + url.toString());
		
		switch (docType) {
        case "doc":
            count++;
            logger.debug("Parsing doc " + count + "/" + total + ": " + url.toString());
            
            String regCode = doc.getElementById("view:_id1:_id2:facetMiddle:pid").text();
            String name = doc.getElementById("view:_id1:_id2:facetMiddle:Title").text();
            String mainCategory = doc.getElementsByAttributeValue("id", "view:_id1:_id2:facetMiddle:Equipment_Categories").val();
            String additionalCategoriesConcat = doc.getElementById("view:_id1:_id2:facetMiddle:Equipment_Categories_Other").val();
            String additionalCategories[] = additionalCategoriesConcat.split(";");
            String mainCategoryLabel = StringEscapeUtils.unescapeJava(doc.getElementsByAttributeValue("id", "view:_id1:_id2:facetMiddle:Equipment_Categories").attr("labels"));
            String additionalCategoriesLabelsConcat = StringEscapeUtils.unescapeJava(doc.getElementsByAttributeValue("id", "view:_id1:_id2:facetMiddle:Equipment_Categories_Other").attr("labels"));
            String additionalCategoriesLabels[] = null;
            if (!additionalCategoriesLabelsConcat.isEmpty() && !additionalCategoriesLabelsConcat.equals("{}")) additionalCategories = additionalCategoriesLabelsConcat.substring(2, additionalCategoriesLabelsConcat.length() - 2).split("\",\"");
            String provozovatelName = doc.getElementById("view:_id1:_id2:facetMiddle:link3").text();
            String provozovatelIC = doc.getElementById("view:_id1:_id2:facetMiddle:ic").text();
            String pravniForma = doc.getElementById("view:_id1:_id2:facetMiddle:companyForm").text();
            String p_uliceCislo = doc.getElementById("view:_id1:_id2:facetMiddle:CompanyStreetAddress").text();
            String p_mesto = doc.getElementById("view:_id1:_id2:facetMiddle:CompanyCity").text();
            String p_psc = doc.getElementById("view:_id1:_id2:facetMiddle:CompanyZip").text();
            String p_kraj = doc.getElementById("view:_id1:_id2:facetMiddle:CompanyState").text();

            String z_uliceCislo = doc.getElementById("view:_id1:_id2:facetMiddle:streetAddress").text();
            String z_mesto = doc.getElementById("view:_id1:_id2:facetMiddle:city").text();
            String z_psc = doc.getElementById("view:_id1:_id2:facetMiddle:zip").text();
            String z_kraj = doc.getElementById("view:_id1:_id2:facetMiddle:state").text();
            String x = doc.getElementById("view:_id1:_id2:facetMiddle:XWGS84").text().replace(",",".");
            String y = doc.getElementById("view:_id1:_id2:facetMiddle:YWGS84").text().replace(",",".");
            
            String povolovaciUrad = doc.getElementById("view:_id1:_id2:facetMiddle:computedField27").text();
            String kontrolniOrganyConcat = doc.getElementById("view:_id1:_id2:facetMiddle:PID_Office_Check").val();
            String kontrolniOrgany[] = kontrolniOrganyConcat.split(";");
            //String povolovaciUrad = doc.getElementById("view:_id1:_id2:facetMiddle:computedField27").text();
            
    		URI entityURI = outputDataUnit.createURI(ldbe + provozovatelIC);
    		URI entityIDURI = outputDataUnit.createURI(entityURI.toString() + "/identifier/mzp.cz");
    		URI entityAddressURI = outputDataUnit.createURI(mzpbe + provozovatelIC + "/adresa");
    		URI branchURI = outputDataUnit.createURI(mzpbr + regCode);
    		URI branchAddressURI = outputDataUnit.createURI(branchURI.toString() + "/adresa");
    		URI branchGeoURI = outputDataUnit.createURI(branchURI.toString() + "/geo");
            
            outputDataUnit.addTriple(entityURI, RDF.TYPE, s_Organization);
    		outputDataUnit.addTriple(entityURI, RDF.TYPE, gr_BusinessEntity);
    		outputDataUnit.addTriple(entityURI, s_name, outputDataUnit.createLiteral(provozovatelName));
    		outputDataUnit.addTriple(entityURI, gr_legalName, outputDataUnit.createLiteral(provozovatelName));
    		outputDataUnit.addTriple(entityURI, adms_identifier, entityIDURI);
    		outputDataUnit.addTriple(entityURI, s_hasPOS, branchURI);

    		outputDataUnit.addTriple(entityIDURI, RDF.TYPE, adms_Identifier);
    		outputDataUnit.addTriple(entityIDURI, SKOS.NOTATION, outputDataUnit.createLiteral(provozovatelIC));
    		outputDataUnit.addTriple(entityIDURI, SKOS.PREF_LABEL, outputDataUnit.createLiteral(provozovatelIC));
    		outputDataUnit.addTriple(entityIDURI, SKOS.IN_SCHEME, ICScheme);

    		outputDataUnit.addTriple(entityURI, s_address, entityAddressURI);
    		outputDataUnit.addTriple(entityAddressURI, RDF.TYPE, s_PostalAddress);
    		if (!p_uliceCislo.isEmpty()) outputDataUnit.addTriple(entityAddressURI, s_streetAddress, outputDataUnit.createLiteral(p_uliceCislo));
    		if (!p_psc.isEmpty()) outputDataUnit.addTriple(entityAddressURI, s_postalCode, outputDataUnit.createLiteral(p_psc));
    		if (!p_kraj.isEmpty()) outputDataUnit.addTriple(entityAddressURI, s_addressRegion, outputDataUnit.createLiteral(p_kraj));
    		if (!p_mesto.isEmpty()) outputDataUnit.addTriple(entityAddressURI, s_addressLocality, outputDataUnit.createLiteral(p_mesto));

    		outputDataUnit.addTriple(branchURI, RDF.TYPE, s_Place);
    		outputDataUnit.addTriple(branchURI, s_name, outputDataUnit.createLiteral(name));
    		
    		outputDataUnit.addTriple(branchURI, s_address, branchAddressURI);
    		outputDataUnit.addTriple(branchAddressURI, RDF.TYPE, s_PostalAddress);
    		if (!z_uliceCislo.isEmpty()) outputDataUnit.addTriple(branchAddressURI, s_streetAddress, outputDataUnit.createLiteral(z_uliceCislo));
    		if (!z_psc.isEmpty()) outputDataUnit.addTriple(branchAddressURI, s_postalCode, outputDataUnit.createLiteral(z_psc));
    		if (!z_kraj.isEmpty()) outputDataUnit.addTriple(branchAddressURI, s_addressRegion, outputDataUnit.createLiteral(z_kraj));
    		if (!z_mesto.isEmpty()) outputDataUnit.addTriple(branchAddressURI, s_addressLocality, outputDataUnit.createLiteral(z_mesto));
    		
    		if (!x.isEmpty() && !y.isEmpty()) {
	    		outputDataUnit.addTriple(branchURI, s_geo, branchGeoURI);
	    		outputDataUnit.addTriple(branchGeoURI, RDF.TYPE, s_GeoCoordinates);
	    		outputDataUnit.addTriple(branchGeoURI, s_latitude, outputDataUnit.createLiteral(x));
	    		outputDataUnit.addTriple(branchGeoURI, s_longitude, outputDataUnit.createLiteral(y));
    		}
            
            break;
        case "provozovna":

        	break;
        }
    }
}
