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
    private int kontrolacount = 0;
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
        case "doc":
        	for (Element a : doc.getElementById("view:_id1:_id2:facetMiddle:_id146:repeatLinks").select("a")) {
        		try {
					out.add(new ParseEntry(new URL("http://www.mzp.cz" + a.attr("href")), "kontrola"));
				} catch (MalformedURLException e) {
					logger.error(e.getLocalizedMessage(), e);
				}
        	}
        	break;
        case "docview":
        	try {
				out.add(new ParseEntry(new URL("http://www.mzp.cz" + doc.getElementById("view:_id1:_id2:facetMiddle:_id19:link1").attr("href")), "doc"));
			} catch (MalformedURLException e) {
				logger.error(e.getLocalizedMessage(), e);
			}
        	break;
        case "provozovna":
        	
        	break;
        }
        return out;
    }

    private String getMonth(String m) 
    {
    	switch (m) {
    	case "Jan": return "01";
    	case "Feb": return "02";
    	case "Mar": return "03";
    	case "Apr": return "04";
    	case "May": return "05";
    	case "Jun": return "06";
    	case "Jul": return "07";
    	case "Aug": return "08";
    	case "Sep": return "09";
    	case "Oct": return "10";
    	case "Nov": return "11";
    	case "Dec": return "12";
    	default: return "01";
    	}
    }
    
    private String getDate(String input) {
    	String out = null;
    	if (input.matches("[a-zA-Z]{3}")) {
    		out = input.replaceAll(".*([0-9]{4}).*","$1") + "-" + getMonth(input.replaceAll(".*([a-zA-Z]{3}).*","$1")) + "-" + input.replaceAll("[a-zA-Z]{3} ([^,]+).*","$1");
    	}
    	else if (input.contains(".")) {
    		String d = input.replaceAll("([0-9]+)\\.([0-9]+)\\.([0-9]{4})","$1");
    		if (d.length() == 1) d = "0" + d;
    		String m = input.replaceAll("([0-9]+)\\.([0-9]+)\\.([0-9]{4})","$2");
    		if (m.length() == 1) m = "0" + m;
    		String y = input.replaceAll("([0-9]+)\\.([0-9]+)\\.([0-9]{4})","$3");
    		out = y + "-" + m + "-" + d;
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
        URI s_result = outputDataUnit.createURI(schemaorg + "result");
        URI s_address = outputDataUnit.createURI(schemaorg + "address");
        URI s_addressLocality = outputDataUnit.createURI(schemaorg + "addressLocality");
        URI s_addressRegion = outputDataUnit.createURI(schemaorg + "addressRegion");
        URI s_postalCode = outputDataUnit.createURI(schemaorg + "postalCode");
        URI s_streetAddress = outputDataUnit.createURI(schemaorg + "streetAddress");
        URI s_longitude = outputDataUnit.createURI(schemaorg + "longitude");
        URI s_latitude = outputDataUnit.createURI(schemaorg + "latitude");
        URI s_GeoCoordinates = outputDataUnit.createURI(schemaorg + "GeoCoordinates");
        URI s_location = outputDataUnit.createURI(schemaorg + "location");
        URI s_actionStatus = outputDataUnit.createURI(schemaorg + "actionStatus");
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
        URI catScheme = outputDataUnit.createURI(mzponto + "categories/ConceptScheme");
    	URI s_ActionStatusType = outputDataUnit.createURI(schemaorg + "ActionStatusType");
    	
		URI mainCategoryURI = outputDataUnit.createURI(mzponto + "hlavniKategorie");
		URI additionalCategoryURI = outputDataUnit.createURI(mzponto + "vedlejsiKategorie");
		
		logger.trace("Processing: " + url.toString());
		
		switch (docType) {
		case "kontrola":
			kontrolacount++;
            logger.debug("Parsing kontrola " + kontrolacount + ": " + url.toString());
            
            String bCode = doc.getElementById("view:_id1:_id2:facetMiddle:_id7:link1").text();
            String checkCode = doc.getElementById("view:_id1:_id2:facetMiddle:_id7:pid").text();
			
            String start = doc.getElementById("view:_id1:_id2:facetMiddle:Date_Check_Start").text();
            String end = doc.getElementById("view:_id1:_id2:facetMiddle:Date_Check_End").text();
            String state = doc.getElementById("view:_id1:_id2:facetMiddle:radioGroup1").text();
            String object = doc.getElementById("view:_id1:_id2:facetMiddle:check_subject").text();
            String text = doc.getElementById("view:_id1:_id2:facetMiddle:Body").text();
            
    		URI bURI = outputDataUnit.createURI(mzpbr + bCode);
    		URI checkURI = outputDataUnit.createURI(mzpbr + bCode + "/checks/" + checkCode);
            
            outputDataUnit.addTriple(checkURI, RDF.TYPE, s_CheckAction);
    		outputDataUnit.addTriple(checkURI, s_location, bURI);
    		
    		
    		String starttime = getDate(start) + "T00:00:00";
    		String endtime = getDate(end) + "T23:59:59";
    		outputDataUnit.addTriple(checkURI, s_startTime, outputDataUnit.createLiteral(starttime, xsd_dateTime));
    		outputDataUnit.addTriple(checkURI, s_endTime, outputDataUnit.createLiteral(endtime, xsd_dateTime));
    		if (!object.isEmpty()) outputDataUnit.addTriple(checkURI, s_instrument, outputDataUnit.createLiteral(object));
    		if (!state.isEmpty()) {
    			URI actionStatus = outputDataUnit.createURI(mzponto + "action-states/" + uriSlug(state));
    			outputDataUnit.addTriple(checkURI, s_actionStatus, actionStatus);
    			outputDataUnit.addTriple(actionStatus, RDF.TYPE, s_ActionStatusType);
    			outputDataUnit.addTriple(actionStatus, s_name, outputDataUnit.createLiteral(state));
    		}
    		if (!text.isEmpty()) outputDataUnit.addTriple(checkURI, s_result, outputDataUnit.createLiteral(text));
    		
			break;
        case "doc":
            count++;
            logger.debug("Parsing doc " + count + ": " + url.toString());
            
            String regCode = doc.getElementById("view:_id1:_id2:facetMiddle:pid").text();
            String name = doc.getElementById("view:_id1:_id2:facetMiddle:Title").text();
            String mainCategory = doc.getElementsByAttributeValue("id", "view:_id1:_id2:facetMiddle:Equipment_Categories").val();
            String additionalCategoriesConcat = doc.getElementById("view:_id1:_id2:facetMiddle:Equipment_Categories_Other").val();
            String additionalCategories[] = additionalCategoriesConcat.split(";");
            String mainCategoryLabel = StringEscapeUtils.unescapeJava(doc.getElementsByAttributeValue("id", "view:_id1:_id2:facetMiddle:Equipment_Categories").attr("labels"));
            if (mainCategoryLabel.equals("{}")) mainCategoryLabel = null;
            String additionalCategoriesLabelsConcat = StringEscapeUtils.unescapeJava(doc.getElementsByAttributeValue("id", "view:_id1:_id2:facetMiddle:Equipment_Categories_Other").attr("labels"));
            String additionalCategoriesLabels[] = null;
            if (!additionalCategoriesLabelsConcat.isEmpty() && !additionalCategoriesLabelsConcat.equals("{}")) additionalCategoriesLabels = additionalCategoriesLabelsConcat.substring(2, additionalCategoriesLabelsConcat.length() - 2).split("\",\"");
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
    		
    		//categories
    		
    		if (mainCategory != null && !mainCategory.isEmpty()) {
	    		URI mCategory = outputDataUnit.createURI(mzponto + "categories/" + mainCategory);
	    		outputDataUnit.addTriple(branchURI, mainCategoryURI, mCategory);
	    		outputDataUnit.addTriple(mCategory, RDF.TYPE, SKOS.CONCEPT);
	    		if (mainCategoryLabel != null && !mainCategoryLabel.isEmpty()) outputDataUnit.addTriple(mCategory, SKOS.PREF_LABEL, outputDataUnit.createLiteral(mainCategoryLabel.substring(mainCategoryLabel.lastIndexOf(':') + 2, mainCategoryLabel.length() - 2)));
	    		outputDataUnit.addTriple(mCategory, SKOS.NOTATION, outputDataUnit.createLiteral(mainCategory));
	    		if (mainCategory.contains(".")) {
	    			URI broader = outputDataUnit.createURI(mzponto + "categories/" + mainCategory.substring(0, mainCategory.indexOf('.')));
	    			outputDataUnit.addTriple(mCategory, SKOS.BROADER_TRANSITIVE, broader);
	    		}
	    		outputDataUnit.addTriple(mCategory, SKOS.IN_SCHEME, catScheme);
    		}
    		
    		for (int i = 0; i < additionalCategories.length; i++) {
        		URI aCategory = outputDataUnit.createURI(mzponto + "categories/" + additionalCategories[i]);
        		outputDataUnit.addTriple(branchURI, additionalCategoryURI, aCategory);
        		outputDataUnit.addTriple(aCategory, RDF.TYPE, SKOS.CONCEPT);
        		if (additionalCategoriesLabels != null) {
        			String currentlabel = null;
        			for (String lbl: additionalCategoriesLabels)
        			{
        				if (lbl.contains(additionalCategories[i])) {
        					currentlabel = lbl.substring(lbl.lastIndexOf(':') + 2);
        					break;
        				}
        				
        			}
        			if (currentlabel != null) outputDataUnit.addTriple(aCategory, SKOS.PREF_LABEL, outputDataUnit.createLiteral(currentlabel));
        		}
        		outputDataUnit.addTriple(aCategory, SKOS.NOTATION, outputDataUnit.createLiteral(additionalCategories[i]));
	    		if (additionalCategories[i].contains(".")) {
	    			URI broader = outputDataUnit.createURI(mzponto + "categories/" + additionalCategories[i].substring(0, additionalCategories[i].indexOf('.')));
	    			outputDataUnit.addTriple(aCategory, SKOS.BROADER_TRANSITIVE, broader);
	    		}
        		outputDataUnit.addTriple(aCategory, SKOS.IN_SCHEME, catScheme);
    		}
    		
            break;
        case "provozovna":

        	break;
        }
    }
}
