package cz.opendata.linked.cz.cenia.irz;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;

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
    static final String owl = "http://www.w3.org/2002/07/owl#";
    static final String irzbrid = "http://linked.opendata.cz/resource/domain/cenia.cz/provozovny/id/";
    static final String irzbr = "http://linked.opendata.cz/resource/domain/cenia.cz/provozovny/";
    static final String irzonto = "http://linked.opendata.cz/ontology/domain/cenia.cz/";
    static final String ldbe = "http://linked.opendata.cz/resource/business-entity/CZ";
    static final String irzbe = "http://linked.opendata.cz/resource/domain/cenia.cz/organizace/";
    static final String gr = "http://purl.org/goodrelations/v1#";
    static final String adms = "http://www.w3.org/ns/adms#";
    static final String xsd = "http://www.w3.org/2001/XMLSchema#";

    public int badPostals = 0;
    public int switchedGPS = 0;
    
    /*private String turtleEscape(String input)
    {
    	return input.replace("\\", "\\\\").replace("\"", "\\\"");
    }*/
    
    @Override
    protected LinkedList<ParseEntry> getLinks(org.jsoup.nodes.Document doc, String docType, URL url) {
        LinkedList<ParseEntry> out = new LinkedList<>();
        switch (docType) {
        case "list":
        	Elements as = doc.select("table.list tr td a");
        	for (Element link : as)
        	{
        		try {
					out.add(new ParseEntry(new URL("http://portal.cenia.cz/irz/" + link.attr("href")), "provozovna"));
				} catch (MalformedURLException e) {
					logger.error(e.getLocalizedMessage());
				}
        	}
        	break;
        case "provozovna":
        	
        	break;
        }
        return out;
    }

    private int getState(Element row) {
    	Elements children = row.getElementsByTag("td");
    	switch (children.size())
    	{
    	case 0:
    		return 0;
    	case 1:
    		return 1;
    	case 2:
    		return 2;
    	case 8:
    		return 3;
    	default:
    		return 0;
    	}
    }
    
    private String uriSlug(String input)
    {
    	return input.toLowerCase().replace(" ", "-").replace(".", "-").replace("–", "-").replace(",", "-").replace("(", "-").replace("§", "-").replace("*", "-").replace("/", "-").replace(")", "-").replace("--", "-").replace("--", "-");
    }
    
    private String geoClean(String input) {
    	if (input.contains("°")) {
    		String deg = input.substring(0, input.indexOf('°'));
    		String min = input.substring(input.indexOf('°') + 1, input.indexOf('\''));
    		String sec = input.substring(input.indexOf('\'')).replace("\'", "");
    		
    		Double ddeg = Double.parseDouble(deg);
    		Double dmin = Double.parseDouble(min);
    		Double dsec = Double.parseDouble(sec);
    		
    		Double dnew = ddeg + dmin/60 + dsec/3600;
    		
    		return Double.toString(dnew);
    	}
    	else return input;
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
        URI ICScheme = outputDataUnit.createURI("http://linked.opendata.cz/resource/concept-scheme/CZ-ICO");
        URI chemScheme = outputDataUnit.createURI(irzonto + "chemicals/ConceptScheme");
    	
		URI ovzdusiURI = outputDataUnit.createURI(irzonto + "UnikDoOvzdusi");
		URI vodaURI = outputDataUnit.createURI(irzonto + "UnikDoVody");
		URI pudaURI = outputDataUnit.createURI(irzonto + "UnikDoPudy");
		URI prenosOdpadVodaURI = outputDataUnit.createURI(irzonto + "PrenosOdpadniVody");
		URI prenosOdpadyURI = outputDataUnit.createURI(irzonto + "PrenosOdpady");

		URI mereniURI = outputDataUnit.createURI(irzonto + "Mereni");
		URI odhadURI = outputDataUnit.createURI(irzonto + "Odhad");
		URI vypocetURI = outputDataUnit.createURI(irzonto + "Vypocet");
		
		URI recyklaceURI = outputDataUnit.createURI(irzonto + "Recyklace");
		URI odstraneniURI = outputDataUnit.createURI(irzonto + "Odstraneni");
		URI urceni_odpadu = outputDataUnit.createURI(irzonto + "urceniOdpadu");
		
		logger.trace("Processing: " + url.toString());
		
		switch (docType) {
        case "list":
            count++;
            logger.debug("Parsing list " + count + "/" + total);

            //String rok = ((TextNode)(doc.childNode(1).childNode(2).childNode(5).childNode(1).childNode(1).childNode(3).childNode(0))).text();// .select("body p+p+table tbody tr:eq(0) td:eq(1)").text();
            //String rok = doc.select("body p+p+table tbody tr:eq(0) td:eq(1)").text();
            String rok = url.toString().substring(url.toString().indexOf('=') + 1, url.toString().indexOf('&'));
            Elements rows = doc.select("table.list tr");
            
            URI currentBranch = null;
            for (Element row : rows)
            {
            	int state = getState(row);
            	switch (state)
            	{
            	case 2:
            		String href = row.select("td a").attr("href"); 
            		String bid = href.substring(href.lastIndexOf('=') + 1);
            		
            		currentBranch = outputDataUnit.createURI(irzbrid + rok + '/' + bid);
            		
            		break;
            	case 3:
            		Elements tds = row.getElementsByTag("td");
            		String currentChemical = tds.get(2).ownText();
            		String ovzdusi = tds.get(3).ownText().replace(',', '.');
            		String voda = tds.get(4).ownText().replace(',', '.');
            		String puda = tds.get(5).ownText().replace(',', '.');
            		String prenosVoda = tds.get(6).ownText().replace(',', '.');
            		String prenosOdpad = tds.get(7).ownText().replace(',', '.');
            		
            		URI currentCheck = outputDataUnit.createURI(currentBranch.toString() + "/checks/" + rok);

            		URI currentChemicalCheckURI = outputDataUnit.createURI(currentCheck.toString() + "/" + uriSlug(currentChemical));
            		URI currentChemicalURI = outputDataUnit.createURI(irzonto + "/chemicals/" + uriSlug(currentChemical));
            		
            		URI currentVoda = outputDataUnit.createURI(currentChemicalCheckURI.toString() + "/voda");
            		URI currentOvzdusi = outputDataUnit.createURI(currentChemicalCheckURI.toString() + "/ovzdusi");
            		URI currentPuda = outputDataUnit.createURI(currentChemicalCheckURI.toString() + "/puda");
            		URI currentPrenosOdpVody = outputDataUnit.createURI(currentChemicalCheckURI.toString() + "/prenos-voda");
            		URI currentPrenosOdpad = outputDataUnit.createURI(currentChemicalCheckURI.toString() + "/prenos-odpad");

            		outputDataUnit.addTriple(currentChemicalURI, RDF.TYPE, SKOS.CONCEPT);
            		outputDataUnit.addTriple(currentChemicalURI, SKOS.PREF_LABEL, outputDataUnit.createLiteral(currentChemical, "cs"));
            		outputDataUnit.addTriple(currentChemicalURI, SKOS.IN_SCHEME, chemScheme);
            		
            		if (!"0".equals(voda)) {
		        		outputDataUnit.addTriple(currentVoda, RDF.TYPE, s_CheckAction);
		        		outputDataUnit.addTriple(currentVoda, s_additionalType, vodaURI);
		        		outputDataUnit.addTriple(currentVoda, s_location, currentBranch);
		        		outputDataUnit.addTriple(currentVoda, s_startTime, outputDataUnit.createLiteral(rok+"-01-01T00:00:00", xsd_dateTime));
		        		outputDataUnit.addTriple(currentVoda, s_endTime, outputDataUnit.createLiteral(rok+"-12-31T23:59:59", xsd_dateTime));
		        		outputDataUnit.addTriple(currentVoda, RDF.VALUE, outputDataUnit.createLiteral(voda.substring(0, voda.indexOf(' ')), xsd_decimal));
		        		outputDataUnit.addTriple(currentVoda, s_object, currentChemicalURI);
		        		if (voda.contains("[C]")) outputDataUnit.addTriple(currentVoda, s_instrument, vypocetURI);  
		        		if (voda.contains("[E]")) outputDataUnit.addTriple(currentVoda, s_instrument, odhadURI);  
		        		if (voda.contains("[M]")) outputDataUnit.addTriple(currentVoda, s_instrument, mereniURI);
            		}

            		if (!"0".equals(ovzdusi)) {
	            		outputDataUnit.addTriple(currentOvzdusi, RDF.TYPE, s_CheckAction);
	            		outputDataUnit.addTriple(currentOvzdusi, s_additionalType, ovzdusiURI);
	            		outputDataUnit.addTriple(currentOvzdusi, s_location, currentBranch);
	            		outputDataUnit.addTriple(currentOvzdusi, s_startTime, outputDataUnit.createLiteral(rok+"-01-01T00:00:00", xsd_dateTime));
	            		outputDataUnit.addTriple(currentOvzdusi, s_endTime, outputDataUnit.createLiteral(rok+"-12-31T23:59:59", xsd_dateTime));
	            		outputDataUnit.addTriple(currentOvzdusi, RDF.VALUE, outputDataUnit.createLiteral(ovzdusi.substring(0, ovzdusi.indexOf(' ')), xsd_decimal));
	            		outputDataUnit.addTriple(currentOvzdusi, s_object, currentChemicalURI);
	            		if (ovzdusi.contains("[C]")) outputDataUnit.addTriple(currentOvzdusi, s_instrument, vypocetURI);  
	            		if (ovzdusi.contains("[E]")) outputDataUnit.addTriple(currentOvzdusi, s_instrument, odhadURI);  
	            		if (ovzdusi.contains("[M]")) outputDataUnit.addTriple(currentOvzdusi, s_instrument, mereniURI);
            		}

            		if (!"0".equals(puda)) {
	            		outputDataUnit.addTriple(currentPuda, RDF.TYPE, s_CheckAction);
	            		outputDataUnit.addTriple(currentPuda, s_additionalType, pudaURI);
	            		outputDataUnit.addTriple(currentPuda, s_location, currentBranch);
	            		outputDataUnit.addTriple(currentPuda, s_startTime, outputDataUnit.createLiteral(rok+"-01-01T00:00:00", xsd_dateTime));
	            		outputDataUnit.addTriple(currentPuda, s_endTime, outputDataUnit.createLiteral(rok+"-12-31T23:59:59", xsd_dateTime));
	            		outputDataUnit.addTriple(currentPuda, RDF.VALUE, outputDataUnit.createLiteral(puda.substring(0, puda.indexOf(' ')), xsd_decimal));
	            		outputDataUnit.addTriple(currentPuda, s_object, currentChemicalURI);
	            		if (puda.contains("[C]")) outputDataUnit.addTriple(currentPuda, s_instrument, vypocetURI);  
	            		if (puda.contains("[E]")) outputDataUnit.addTriple(currentPuda, s_instrument, odhadURI);  
	            		if (puda.contains("[M]")) outputDataUnit.addTriple(currentPuda, s_instrument, mereniURI);  
            		}
            		
            		if (!"0".equals(prenosVoda)) {
	            		outputDataUnit.addTriple(currentPrenosOdpVody, RDF.TYPE, s_CheckAction);
	            		outputDataUnit.addTriple(currentPrenosOdpVody, s_additionalType, prenosOdpadVodaURI);
	            		outputDataUnit.addTriple(currentPrenosOdpVody, s_location, currentBranch);
	            		outputDataUnit.addTriple(currentPrenosOdpVody, s_startTime, outputDataUnit.createLiteral(rok+"-01-01T00:00:00", xsd_dateTime));
	            		outputDataUnit.addTriple(currentPrenosOdpVody, s_endTime, outputDataUnit.createLiteral(rok+"-12-31T23:59:59", xsd_dateTime));
	            		outputDataUnit.addTriple(currentPrenosOdpVody, RDF.VALUE, outputDataUnit.createLiteral(prenosVoda.substring(0, prenosVoda.indexOf(' ')), xsd_decimal));
	            		outputDataUnit.addTriple(currentPrenosOdpVody, s_object, currentChemicalURI);
	            		if (prenosVoda.contains("[C]")) outputDataUnit.addTriple(currentPrenosOdpVody, s_instrument, vypocetURI);  
	            		if (prenosVoda.contains("[E]")) outputDataUnit.addTriple(currentPrenosOdpVody, s_instrument, odhadURI);  
	            		if (prenosVoda.contains("[M]")) outputDataUnit.addTriple(currentPrenosOdpVody, s_instrument, mereniURI);  
	            		if (prenosVoda.contains("[R]")) outputDataUnit.addTriple(currentPrenosOdpVody, urceni_odpadu, recyklaceURI);  
	            		if (prenosVoda.contains("[D]")) outputDataUnit.addTriple(currentPrenosOdpVody, urceni_odpadu, odstraneniURI);  
            		}
            		
            		if (!"0".equals(prenosOdpad)) {
	            		outputDataUnit.addTriple(currentPrenosOdpad, RDF.TYPE, s_CheckAction);
	            		outputDataUnit.addTriple(currentPrenosOdpad, s_additionalType, prenosOdpadyURI);
	            		outputDataUnit.addTriple(currentPrenosOdpad, s_location, currentBranch);
	            		outputDataUnit.addTriple(currentPrenosOdpad, s_startTime, outputDataUnit.createLiteral(rok+"-01-01T00:00:00", xsd_dateTime));
	            		outputDataUnit.addTriple(currentPrenosOdpad, s_endTime, outputDataUnit.createLiteral(rok+"-12-31T23:59:59", xsd_dateTime));
	            		outputDataUnit.addTriple(currentPrenosOdpad, RDF.VALUE, outputDataUnit.createLiteral(prenosOdpad.substring(0, prenosOdpad.indexOf(' ')), xsd_decimal));
	            		outputDataUnit.addTriple(currentPrenosOdpad, s_object, currentChemicalURI);
	            		if (prenosOdpad.contains("[C]")) outputDataUnit.addTriple(currentPrenosOdpad, s_instrument, vypocetURI);  
	            		if (prenosOdpad.contains("[E]")) outputDataUnit.addTriple(currentPrenosOdpad, s_instrument, odhadURI);  
	            		if (prenosOdpad.contains("[M]")) outputDataUnit.addTriple(currentPrenosOdpad, s_instrument, mereniURI);  
	            		if (prenosOdpad.contains("[R]")) outputDataUnit.addTriple(currentPrenosOdpad, urceni_odpadu, recyklaceURI);  
	            		if (prenosOdpad.contains("[D]")) outputDataUnit.addTriple(currentPrenosOdpad, urceni_odpadu, odstraneniURI);  
            		}
            		break;
            	}
            	
            }
            break;
        case "provozovna":
        	Element entityP = doc.select("p:eq(3)").first();
        	Element branchP = doc.select("p:eq(5)").first();
        	
        	String ename = entityP.getElementsByTag("span").first().text();
        	String eic = ename.substring(ename.lastIndexOf(' ') + 1, ename.length() - 1);
        	ename = ename.substring(0, ename.indexOf('(') - 1);
        	String eaddress = null;
        	String eokres = null;
        	String eaddress_street = null;
        	String eaddress_postal = null;
        	String eaddress_city = null;
        	int current = 0;
        	for (Node child : entityP.childNodes()) {
        		if (child instanceof TextNode) {
        			current++;
        		    switch (current) {
        		    case 2:
        		    	eaddress = ((TextNode) child).text();
        		    	int carka = eaddress.indexOf(',');
        		    	if (eaddress.contains("null")) {
	        		    	eaddress_street = "";
	        		    	eaddress_postal = "";
	        		    	eaddress_city = "";
        		    	}
        		    	else if (eaddress.contains(",")) {
	        		    	eaddress_street = eaddress.substring(0, carka).trim();
	        		    	eaddress_postal = eaddress.replaceAll(".*([0-9]{5}).*", "$1");
	        		    	if (!eaddress_postal.matches("[0-9]{5}")) {
	        		    		eaddress_postal = "";
	        		    		String badpostal = eaddress.replaceAll(".*([0-9]{3} [0-9]).*", "$1");
	        		    		eaddress_city = eaddress.substring(eaddress.indexOf(badpostal) + 6);
	        		    		logger.debug("Dropping bad postal " + badpostal + " in " + url.toString());
	        		    		badPostals++;
	        		    	}
	        		    	else {
	        		    		eaddress_city = eaddress.substring(eaddress.indexOf(eaddress_postal) + 6);
	        		    	}
        		    	}
        		    	else {
        		    		eaddress_street = "";
        		    		eaddress_postal = eaddress.substring(0, 5);
	        		    	eaddress_city = eaddress.substring(6);
        		    	}
        		    	break;
        		    case 3:
        		    	eokres = ((TextNode) child).text();
        		    	eokres = eokres.substring(eokres.indexOf(':') + 2);
        		    	break;
        		    }
        		}
        	}
        	
        	String bid = url.toString().substring(url.toString().lastIndexOf('=') + 1);
            String rok2 = url.toString().substring(url.toString().indexOf('=') + 1, url.toString().indexOf('&'));
        	String bname = branchP.getElementsByTag("span").first().text();
        	String bicp = bname.substring(bname.lastIndexOf(' ') + 1, bname.length() - 1);
        	bname = bname.substring(0, bname.indexOf('(') - 1);
        	String baddress = null;
        	String baddress_street = null;
        	String baddress_postal = null;
        	String baddress_city = null;
        	String bokres = null;
        	String bjtsk = null;
        	String bwgs = null;
        	String bwgs_lng = null;
        	String bwgs_lat = null;
        	current = 0;
        	for (Node child : branchP.childNodes()) {
        		if (child instanceof TextNode) {
        			current++;
        		    switch (current) {
        		    case 2:
        		    	baddress = ((TextNode) child).text();
        		    	int carka = baddress.lastIndexOf(',');
        		    	if (baddress.contains("null")) {
	        		    	baddress_street = "";
	        		    	baddress_postal = "";
	        		    	baddress_city = "";
        		    	}
        		    	else if (baddress.contains(",")) {
	        		    	baddress_street = baddress.substring(0, carka).trim();
	        		    	baddress_postal = baddress.replaceAll(".*([0-9]{5}).*", "$1");
	        		    	if (!baddress_postal.matches("[0-9]{5}")) {
	        		    		baddress_postal = "";
	        		    		String badpostal = baddress.replaceAll(".*([0-9]{3} [0-9]).*", "$1");
	        		    		baddress_city = baddress.substring(baddress.indexOf(badpostal) + 6);
	        		    		logger.debug("Dropping bad postal " + badpostal + " in " + url.toString());
	        		    		badPostals++;
	        		    	}
	        		    	else {
	        		    		baddress_city = baddress.substring(baddress.indexOf(baddress_postal) + 6);
	        		    	}
        		    	}
        		    	else {
        		    		baddress_street = "";
        		    		baddress_postal = baddress.substring(0, 5);
	        		    	baddress_city = baddress.substring(6);
        		    	}
        		    	break;
        		    case 3:
        		    	bokres = ((TextNode) child).text();
        		    	bokres = bokres.substring(bokres.indexOf(':') + 2);
        		    	break;
        		    case 4:
        		    	bjtsk = ((TextNode) child).text();
        		    	bjtsk = bjtsk.substring(bjtsk.indexOf(':') + 2);
        		    	break;
        		    case 5:
        		    	bwgs = ((TextNode) child).text();
        		    	bwgs = bwgs.substring(bwgs.indexOf(':') + 2);
        		    	String[] bwgs_coords = bwgs.split(" "); 
        		    	bwgs_lng = geoClean(bwgs_coords[0].replace(";", ""));
        		    	bwgs_lat = geoClean(bwgs_coords[1].replace(";", ""));
        		    	
        		    	if (bwgs_lng.startsWith("1")) {
        		    		//SWITCH!
        		    		logger.debug("Switching longitude and latitude: " + url.toString());
        		    		switchedGPS++;
        		    		String temp = bwgs_lat;
        		    		bwgs_lat = bwgs_lng;
        		    		bwgs_lng = temp;
        		    	}
        		    	
        		    	break;
        		    }
        		}
        	}
        	
    		URI branchIDURI = outputDataUnit.createURI(irzbrid + rok2 + '/' + bid);
    		URI entityURI = outputDataUnit.createURI(ldbe + eic);
    		URI entityIDURI = outputDataUnit.createURI(entityURI.toString() + "/identifier/cenia.cz");
    		URI entityAddressURI = outputDataUnit.createURI(irzbe + eic + "/adresa/" + rok2);
    		URI branchURI = outputDataUnit.createURI(irzbr + bicp);
    		URI branchAddressURI = outputDataUnit.createURI(branchURI.toString() + "/adresa/" + rok2);
    		URI branchGeoURI = outputDataUnit.createURI(branchURI.toString() + "/geo/" + rok2);
    		
    		outputDataUnit.addTriple(entityURI, RDF.TYPE, s_Organization);
    		outputDataUnit.addTriple(entityURI, RDF.TYPE, gr_BusinessEntity);
    		outputDataUnit.addTriple(entityURI, s_name, outputDataUnit.createLiteral(ename));
    		outputDataUnit.addTriple(entityURI, gr_legalName, outputDataUnit.createLiteral(ename));
    		outputDataUnit.addTriple(entityURI, adms_identifier, entityIDURI);
    		outputDataUnit.addTriple(entityURI, s_hasPOS, branchURI);

    		outputDataUnit.addTriple(entityIDURI, RDF.TYPE, adms_Identifier);
    		outputDataUnit.addTriple(entityIDURI, SKOS.NOTATION, outputDataUnit.createLiteral(eic));
    		outputDataUnit.addTriple(entityIDURI, SKOS.PREF_LABEL, outputDataUnit.createLiteral(eic));
    		outputDataUnit.addTriple(entityIDURI, SKOS.IN_SCHEME, ICScheme);

    		outputDataUnit.addTriple(entityURI, s_address, entityAddressURI);
    		outputDataUnit.addTriple(entityAddressURI, RDF.TYPE, s_PostalAddress);
    		outputDataUnit.addTriple(entityAddressURI, DCTERMS.VALID, outputDataUnit.createLiteral(rok2, xsd_gYear));
    		if (!eaddress_street.isEmpty()) outputDataUnit.addTriple(entityAddressURI, s_streetAddress, outputDataUnit.createLiteral(eaddress_street));
    		if (!eaddress_postal.isEmpty()) outputDataUnit.addTriple(entityAddressURI, s_postalCode, outputDataUnit.createLiteral(eaddress_postal));
    		outputDataUnit.addTriple(entityAddressURI, s_addressRegion, outputDataUnit.createLiteral(eokres));
    		if (!eaddress_city.isEmpty()) outputDataUnit.addTriple(entityAddressURI, s_addressLocality, outputDataUnit.createLiteral(eaddress_city));

    		outputDataUnit.addTriple(branchURI, RDF.TYPE, s_Place);
    		outputDataUnit.addTriple(branchURI, s_name, outputDataUnit.createLiteral(bname));
    		outputDataUnit.addTriple(branchURI, s_geo, branchGeoURI);
    		outputDataUnit.addTriple(branchURI, owl_sameAs, branchIDURI);
    		
    		outputDataUnit.addTriple(branchURI, s_address, branchAddressURI);
    		outputDataUnit.addTriple(branchAddressURI, RDF.TYPE, s_PostalAddress);
    		outputDataUnit.addTriple(branchAddressURI, DCTERMS.VALID, outputDataUnit.createLiteral(rok2, xsd_gYear));
    		if (!baddress_street.isEmpty()) outputDataUnit.addTriple(branchAddressURI, s_streetAddress, outputDataUnit.createLiteral(baddress_street));
    		if (!baddress_postal.isEmpty()) outputDataUnit.addTriple(branchAddressURI, s_postalCode, outputDataUnit.createLiteral(baddress_postal));
    		outputDataUnit.addTriple(branchAddressURI, s_addressRegion, outputDataUnit.createLiteral(bokres));
    		if (!baddress_city.isEmpty()) outputDataUnit.addTriple(branchAddressURI, s_addressLocality, outputDataUnit.createLiteral(baddress_city));
    		
    		outputDataUnit.addTriple(branchGeoURI, RDF.TYPE, s_GeoCoordinates);
    		outputDataUnit.addTriple(branchGeoURI, s_latitude, outputDataUnit.createLiteral(bwgs_lat));
    		outputDataUnit.addTriple(branchGeoURI, s_longitude, outputDataUnit.createLiteral(bwgs_lng));
        	
        	break;
        }
    }
}
