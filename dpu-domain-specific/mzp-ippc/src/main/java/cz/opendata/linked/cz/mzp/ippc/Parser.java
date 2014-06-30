package cz.opendata.linked.cz.mzp.ippc;

import cz.cuni.mff.xrg.uv.rdf.simple.OperationFailedException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.nodes.Element;
import org.openrdf.model.URI;
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
    protected void parse(org.jsoup.nodes.Document doc, String docType, URL url) throws OperationFailedException {
        URI s_Organization = valueFactory.createURI(schemaorg + "Organization");
        URI gr_BusinessEntity = valueFactory.createURI(gr + "BusinessEntity");
        URI gr_legalName = valueFactory.createURI(gr + "legalName");
        URI adms_Identifier = valueFactory.createURI(adms + "Identifier");
        URI adms_identifier = valueFactory.createURI(adms + "identifier");
        URI s_Place = valueFactory.createURI(schemaorg + "Place");
        URI s_name = valueFactory.createURI(schemaorg + "name");
        URI s_hasPOS = valueFactory.createURI(schemaorg + "hasPOS");
        URI s_CheckAction = valueFactory.createURI(schemaorg + "CheckAction");
        URI s_PostalAddress = valueFactory.createURI(schemaorg + "PostalAddress");
        URI s_object = valueFactory.createURI(schemaorg + "object");
        URI s_geo = valueFactory.createURI(schemaorg + "geo");
        URI s_result = valueFactory.createURI(schemaorg + "result");
        URI s_address = valueFactory.createURI(schemaorg + "address");
        URI s_addressLocality = valueFactory.createURI(schemaorg + "addressLocality");
        URI s_addressRegion = valueFactory.createURI(schemaorg + "addressRegion");
        URI s_postalCode = valueFactory.createURI(schemaorg + "postalCode");
        URI s_streetAddress = valueFactory.createURI(schemaorg + "streetAddress");
        URI s_longitude = valueFactory.createURI(schemaorg + "longitude");
        URI s_latitude = valueFactory.createURI(schemaorg + "latitude");
        URI s_GeoCoordinates = valueFactory.createURI(schemaorg + "GeoCoordinates");
        URI s_location = valueFactory.createURI(schemaorg + "location");
        URI s_actionStatus = valueFactory.createURI(schemaorg + "actionStatus");
        URI s_startTime = valueFactory.createURI(schemaorg + "startTime");
        URI s_endTime = valueFactory.createURI(schemaorg + "endTime");
        URI s_instrument = valueFactory.createURI(schemaorg + "instrument");
        URI s_additionalType = valueFactory.createURI(schemaorg + "additionalType");
        URI owl_sameAs = valueFactory.createURI(owl + "sameAs");
        URI xsd_decimal = valueFactory.createURI(xsd + "decimal");
        URI xsd_dateTime = valueFactory.createURI(xsd + "dateTime");
        URI xsd_gYear = valueFactory.createURI(xsd + "gYear");
        //URI dcterms_source = valueFactory.createURI(dcterms + "source");
        URI ICScheme = valueFactory.createURI("http://linked.opendata.cz/resource/concept-scheme/CZ-ICO");
        URI catScheme = valueFactory.createURI(mzponto + "categories/ConceptScheme");
    	URI s_ActionStatusType = valueFactory.createURI(schemaorg + "ActionStatusType");
    	
		URI mainCategoryURI = valueFactory.createURI(mzponto + "hlavniKategorie");
		URI additionalCategoryURI = valueFactory.createURI(mzponto + "vedlejsiKategorie");
		
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
            
    		URI bURI = valueFactory.createURI(mzpbr + bCode);
    		URI checkURI = valueFactory.createURI(mzpbr + bCode + "/checks/" + checkCode);
            
            outputDataUnit.add(checkURI, RDF.TYPE, s_CheckAction);
    		outputDataUnit.add(checkURI, s_location, bURI);
    		
    		
    		String starttime = getDate(start) + "T00:00:00";
    		String endtime = getDate(end) + "T23:59:59";
    		outputDataUnit.add(checkURI, s_startTime, valueFactory.createLiteral(starttime, xsd_dateTime));
    		outputDataUnit.add(checkURI, s_endTime, valueFactory.createLiteral(endtime, xsd_dateTime));
    		if (!object.isEmpty()) outputDataUnit.add(checkURI, s_instrument, valueFactory.createLiteral(object));
    		if (!state.isEmpty()) {
    			URI actionStatus = valueFactory.createURI(mzponto + "action-states/" + uriSlug(state));
    			outputDataUnit.add(checkURI, s_actionStatus, actionStatus);
    			outputDataUnit.add(actionStatus, RDF.TYPE, s_ActionStatusType);
    			outputDataUnit.add(actionStatus, s_name, valueFactory.createLiteral(state));
    		}
    		if (!text.isEmpty()) outputDataUnit.add(checkURI, s_result, valueFactory.createLiteral(text));
    		
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
            
    		URI entityURI = valueFactory.createURI(ldbe + provozovatelIC);
    		URI entityIDURI = valueFactory.createURI(entityURI.toString() + "/identifier/mzp.cz");
    		URI entityAddressURI = valueFactory.createURI(mzpbe + provozovatelIC + "/adresa");
    		URI branchURI = valueFactory.createURI(mzpbr + regCode);
    		URI branchAddressURI = valueFactory.createURI(branchURI.toString() + "/adresa");
    		URI branchGeoURI = valueFactory.createURI(branchURI.toString() + "/geo");
            
            outputDataUnit.add(entityURI, RDF.TYPE, s_Organization);
    		outputDataUnit.add(entityURI, RDF.TYPE, gr_BusinessEntity);
    		outputDataUnit.add(entityURI, s_name, valueFactory.createLiteral(provozovatelName));
    		outputDataUnit.add(entityURI, gr_legalName, valueFactory.createLiteral(provozovatelName));
    		outputDataUnit.add(entityURI, adms_identifier, entityIDURI);
    		outputDataUnit.add(entityURI, s_hasPOS, branchURI);

    		outputDataUnit.add(entityIDURI, RDF.TYPE, adms_Identifier);
    		outputDataUnit.add(entityIDURI, SKOS.NOTATION, valueFactory.createLiteral(provozovatelIC));
    		outputDataUnit.add(entityIDURI, SKOS.PREF_LABEL, valueFactory.createLiteral(provozovatelIC));
    		outputDataUnit.add(entityIDURI, SKOS.IN_SCHEME, ICScheme);

    		outputDataUnit.add(entityURI, s_address, entityAddressURI);
    		outputDataUnit.add(entityAddressURI, RDF.TYPE, s_PostalAddress);
    		if (!p_uliceCislo.isEmpty()) outputDataUnit.add(entityAddressURI, s_streetAddress, valueFactory.createLiteral(p_uliceCislo));
    		if (!p_psc.isEmpty()) outputDataUnit.add(entityAddressURI, s_postalCode, valueFactory.createLiteral(p_psc));
    		if (!p_kraj.isEmpty()) outputDataUnit.add(entityAddressURI, s_addressRegion, valueFactory.createLiteral(p_kraj));
    		if (!p_mesto.isEmpty()) outputDataUnit.add(entityAddressURI, s_addressLocality, valueFactory.createLiteral(p_mesto));

    		outputDataUnit.add(branchURI, RDF.TYPE, s_Place);
    		outputDataUnit.add(branchURI, s_name, valueFactory.createLiteral(name));
    		
    		outputDataUnit.add(branchURI, s_address, branchAddressURI);
    		outputDataUnit.add(branchAddressURI, RDF.TYPE, s_PostalAddress);
    		if (!z_uliceCislo.isEmpty()) outputDataUnit.add(branchAddressURI, s_streetAddress, valueFactory.createLiteral(z_uliceCislo));
    		if (!z_psc.isEmpty()) outputDataUnit.add(branchAddressURI, s_postalCode, valueFactory.createLiteral(z_psc));
    		if (!z_kraj.isEmpty()) outputDataUnit.add(branchAddressURI, s_addressRegion, valueFactory.createLiteral(z_kraj));
    		if (!z_mesto.isEmpty()) outputDataUnit.add(branchAddressURI, s_addressLocality, valueFactory.createLiteral(z_mesto));
    		
    		if (!x.isEmpty() && !y.isEmpty()) {
	    		outputDataUnit.add(branchURI, s_geo, branchGeoURI);
	    		outputDataUnit.add(branchGeoURI, RDF.TYPE, s_GeoCoordinates);
	    		outputDataUnit.add(branchGeoURI, s_latitude, valueFactory.createLiteral(x));
	    		outputDataUnit.add(branchGeoURI, s_longitude, valueFactory.createLiteral(y));
    		}
    		
    		//categories
    		
    		if (mainCategory != null && !mainCategory.isEmpty()) {
	    		URI mCategory = valueFactory.createURI(mzponto + "categories/" + mainCategory);
	    		outputDataUnit.add(branchURI, mainCategoryURI, mCategory);
	    		outputDataUnit.add(mCategory, RDF.TYPE, SKOS.CONCEPT);
	    		if (mainCategoryLabel != null && !mainCategoryLabel.isEmpty()) outputDataUnit.add(mCategory, SKOS.PREF_LABEL, valueFactory.createLiteral(mainCategoryLabel.substring(mainCategoryLabel.lastIndexOf(':') + 2, mainCategoryLabel.length() - 2)));
	    		outputDataUnit.add(mCategory, SKOS.NOTATION, valueFactory.createLiteral(mainCategory));
	    		if (mainCategory.contains(".")) {
	    			URI broader = valueFactory.createURI(mzponto + "categories/" + mainCategory.substring(0, mainCategory.indexOf('.')));
	    			outputDataUnit.add(mCategory, SKOS.BROADER_TRANSITIVE, broader);
	    		}
	    		outputDataUnit.add(mCategory, SKOS.IN_SCHEME, catScheme);
    		}
    		
    		for (int i = 0; i < additionalCategories.length; i++) {
        		URI aCategory = valueFactory.createURI(mzponto + "categories/" + additionalCategories[i]);
        		outputDataUnit.add(branchURI, additionalCategoryURI, aCategory);
        		outputDataUnit.add(aCategory, RDF.TYPE, SKOS.CONCEPT);
        		if (additionalCategoriesLabels != null) {
        			String currentlabel = null;
        			for (String lbl: additionalCategoriesLabels)
        			{
        				if (lbl.contains(additionalCategories[i])) {
        					currentlabel = lbl.substring(lbl.lastIndexOf(':') + 2);
        					break;
        				}
        				
        			}
        			if (currentlabel != null) outputDataUnit.add(aCategory, SKOS.PREF_LABEL, valueFactory.createLiteral(currentlabel));
        		}
        		outputDataUnit.add(aCategory, SKOS.NOTATION, valueFactory.createLiteral(additionalCategories[i]));
	    		if (additionalCategories[i].contains(".")) {
	    			URI broader = valueFactory.createURI(mzponto + "categories/" + additionalCategories[i].substring(0, additionalCategories[i].indexOf('.')));
	    			outputDataUnit.add(aCategory, SKOS.BROADER_TRANSITIVE, broader);
	    		}
        		outputDataUnit.add(aCategory, SKOS.IN_SCHEME, catScheme);
    		}
    		
            break;
        case "provozovna":

        	break;
        }
    }
}
