package cz.opendata.linked.cz.cenia.irz;

import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.OperationFailedException;
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

public class Parser extends ScrapingTemplate {
    
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
        //URI s_result = valueFactory.createURI(schemaorg + "result");
        URI s_address = valueFactory.createURI(schemaorg + "address");
        URI s_addressLocality = valueFactory.createURI(schemaorg + "addressLocality");
        URI s_addressRegion = valueFactory.createURI(schemaorg + "addressRegion");
        URI s_postalCode = valueFactory.createURI(schemaorg + "postalCode");
        URI s_streetAddress = valueFactory.createURI(schemaorg + "streetAddress");
        URI s_longitude = valueFactory.createURI(schemaorg + "longitude");
        URI s_latitude = valueFactory.createURI(schemaorg + "latitude");
        URI s_GeoCoordinates = valueFactory.createURI(schemaorg + "GeoCoordinates");
        URI s_location = valueFactory.createURI(schemaorg + "location");
        URI s_startTime = valueFactory.createURI(schemaorg + "startTime");
        URI s_endTime = valueFactory.createURI(schemaorg + "endTime");
        URI s_instrument = valueFactory.createURI(schemaorg + "instrument");
        URI s_additionalType = valueFactory.createURI(schemaorg + "additionalType");
        URI owl_sameAs = valueFactory.createURI(owl + "sameAs");
        URI xsd_decimal = valueFactory.createURI(xsd + "decimal");
        URI xsd_dateTime = valueFactory.createURI(xsd + "dateTime");
        URI xsd_gYear = valueFactory.createURI(xsd + "gYear");
        URI ICScheme = valueFactory.createURI("http://linked.opendata.cz/resource/concept-scheme/CZ-ICO");
        URI chemScheme = valueFactory.createURI(irzonto + "chemicals/ConceptScheme");
        
        URI ovzdusiURI = valueFactory.createURI(irzonto + "UnikDoOvzdusi");
        URI vodaURI = valueFactory.createURI(irzonto + "UnikDoVody");
        URI pudaURI = valueFactory.createURI(irzonto + "UnikDoPudy");
        URI prenosOdpadVodaURI = valueFactory.createURI(irzonto + "PrenosOdpadniVody");
        URI prenosOdpadyURI = valueFactory.createURI(irzonto + "PrenosOdpady");

        URI mereniURI = valueFactory.createURI(irzonto + "Mereni");
        URI odhadURI = valueFactory.createURI(irzonto + "Odhad");
        URI vypocetURI = valueFactory.createURI(irzonto + "Vypocet");
        
        URI recyklaceURI = valueFactory.createURI(irzonto + "Recyklace");
        URI odstraneniURI = valueFactory.createURI(irzonto + "Odstraneni");
        URI urceni_odpadu = valueFactory.createURI(irzonto + "urceniOdpadu");
        
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
                    
                    currentBranch = valueFactory.createURI(irzbrid + rok + '/' + bid);
                    
                    break;
                case 3:
                    Elements tds = row.getElementsByTag("td");
                    String currentChemical = tds.get(2).ownText();
                    String ovzdusi = tds.get(3).ownText().replace(',', '.');
                    String voda = tds.get(4).ownText().replace(',', '.');
                    String puda = tds.get(5).ownText().replace(',', '.');
                    String prenosVoda = tds.get(6).ownText().replace(',', '.');
                    String prenosOdpad = tds.get(7).ownText().replace(',', '.');
                    
                    URI currentCheck = valueFactory.createURI(currentBranch.toString() + "/checks/" + rok);

                    URI currentChemicalCheckURI = valueFactory.createURI(currentCheck.toString() + "/" + uriSlug(currentChemical));
                    URI currentChemicalURI = valueFactory.createURI(irzonto + "chemicals/" + uriSlug(currentChemical));
                    
                    URI currentVoda = valueFactory.createURI(currentChemicalCheckURI.toString() + "/voda");
                    URI currentOvzdusi = valueFactory.createURI(currentChemicalCheckURI.toString() + "/ovzdusi");
                    URI currentPuda = valueFactory.createURI(currentChemicalCheckURI.toString() + "/puda");
                    URI currentPrenosOdpVody = valueFactory.createURI(currentChemicalCheckURI.toString() + "/prenos-voda");
                    URI currentPrenosOdpad = valueFactory.createURI(currentChemicalCheckURI.toString() + "/prenos-odpad");

                    outputDataUnit.add(currentChemicalURI, RDF.TYPE, SKOS.CONCEPT);
                    outputDataUnit.add(currentChemicalURI, SKOS.PREF_LABEL, valueFactory.createLiteral(currentChemical, "cs"));
                    outputDataUnit.add(currentChemicalURI, SKOS.IN_SCHEME, chemScheme);
                    
                    if (!"0".equals(voda)) {
                        outputDataUnit.add(currentVoda, RDF.TYPE, s_CheckAction);
                        outputDataUnit.add(currentVoda, s_additionalType, vodaURI);
                        outputDataUnit.add(currentVoda, s_location, currentBranch);
                        outputDataUnit.add(currentVoda, s_startTime, valueFactory.createLiteral(rok+"-01-01T00:00:00", xsd_dateTime));
                        outputDataUnit.add(currentVoda, s_endTime, valueFactory.createLiteral(rok+"-12-31T23:59:59", xsd_dateTime));
                        outputDataUnit.add(currentVoda, RDF.VALUE, valueFactory.createLiteral(voda.substring(0, voda.indexOf(' ')), xsd_decimal));
                        outputDataUnit.add(currentVoda, s_object, currentChemicalURI);
                        if (voda.contains("[C]")) outputDataUnit.add(currentVoda, s_instrument, vypocetURI);  
                        if (voda.contains("[E]")) outputDataUnit.add(currentVoda, s_instrument, odhadURI);  
                        if (voda.contains("[M]")) outputDataUnit.add(currentVoda, s_instrument, mereniURI);
                    }

                    if (!"0".equals(ovzdusi)) {
                        outputDataUnit.add(currentOvzdusi, RDF.TYPE, s_CheckAction);
                        outputDataUnit.add(currentOvzdusi, s_additionalType, ovzdusiURI);
                        outputDataUnit.add(currentOvzdusi, s_location, currentBranch);
                        outputDataUnit.add(currentOvzdusi, s_startTime, valueFactory.createLiteral(rok+"-01-01T00:00:00", xsd_dateTime));
                        outputDataUnit.add(currentOvzdusi, s_endTime, valueFactory.createLiteral(rok+"-12-31T23:59:59", xsd_dateTime));
                        outputDataUnit.add(currentOvzdusi, RDF.VALUE, valueFactory.createLiteral(ovzdusi.substring(0, ovzdusi.indexOf(' ')), xsd_decimal));
                        outputDataUnit.add(currentOvzdusi, s_object, currentChemicalURI);
                        if (ovzdusi.contains("[C]")) outputDataUnit.add(currentOvzdusi, s_instrument, vypocetURI);  
                        if (ovzdusi.contains("[E]")) outputDataUnit.add(currentOvzdusi, s_instrument, odhadURI);  
                        if (ovzdusi.contains("[M]")) outputDataUnit.add(currentOvzdusi, s_instrument, mereniURI);
                    }

                    if (!"0".equals(puda)) {
                        outputDataUnit.add(currentPuda, RDF.TYPE, s_CheckAction);
                        outputDataUnit.add(currentPuda, s_additionalType, pudaURI);
                        outputDataUnit.add(currentPuda, s_location, currentBranch);
                        outputDataUnit.add(currentPuda, s_startTime, valueFactory.createLiteral(rok+"-01-01T00:00:00", xsd_dateTime));
                        outputDataUnit.add(currentPuda, s_endTime, valueFactory.createLiteral(rok+"-12-31T23:59:59", xsd_dateTime));
                        outputDataUnit.add(currentPuda, RDF.VALUE, valueFactory.createLiteral(puda.substring(0, puda.indexOf(' ')), xsd_decimal));
                        outputDataUnit.add(currentPuda, s_object, currentChemicalURI);
                        if (puda.contains("[C]")) outputDataUnit.add(currentPuda, s_instrument, vypocetURI);  
                        if (puda.contains("[E]")) outputDataUnit.add(currentPuda, s_instrument, odhadURI);  
                        if (puda.contains("[M]")) outputDataUnit.add(currentPuda, s_instrument, mereniURI);  
                    }
                    
                    if (!"0".equals(prenosVoda)) {
                        outputDataUnit.add(currentPrenosOdpVody, RDF.TYPE, s_CheckAction);
                        outputDataUnit.add(currentPrenosOdpVody, s_additionalType, prenosOdpadVodaURI);
                        outputDataUnit.add(currentPrenosOdpVody, s_location, currentBranch);
                        outputDataUnit.add(currentPrenosOdpVody, s_startTime, valueFactory.createLiteral(rok+"-01-01T00:00:00", xsd_dateTime));
                        outputDataUnit.add(currentPrenosOdpVody, s_endTime, valueFactory.createLiteral(rok+"-12-31T23:59:59", xsd_dateTime));
                        outputDataUnit.add(currentPrenosOdpVody, RDF.VALUE, valueFactory.createLiteral(prenosVoda.substring(0, prenosVoda.indexOf(' ')), xsd_decimal));
                        outputDataUnit.add(currentPrenosOdpVody, s_object, currentChemicalURI);
                        if (prenosVoda.contains("[C]")) outputDataUnit.add(currentPrenosOdpVody, s_instrument, vypocetURI);  
                        if (prenosVoda.contains("[E]")) outputDataUnit.add(currentPrenosOdpVody, s_instrument, odhadURI);  
                        if (prenosVoda.contains("[M]")) outputDataUnit.add(currentPrenosOdpVody, s_instrument, mereniURI);  
                        if (prenosVoda.contains("[R]")) outputDataUnit.add(currentPrenosOdpVody, urceni_odpadu, recyklaceURI);  
                        if (prenosVoda.contains("[D]")) outputDataUnit.add(currentPrenosOdpVody, urceni_odpadu, odstraneniURI);  
                    }
                    
                    if (!"0".equals(prenosOdpad)) {
                        outputDataUnit.add(currentPrenosOdpad, RDF.TYPE, s_CheckAction);
                        outputDataUnit.add(currentPrenosOdpad, s_additionalType, prenosOdpadyURI);
                        outputDataUnit.add(currentPrenosOdpad, s_location, currentBranch);
                        outputDataUnit.add(currentPrenosOdpad, s_startTime, valueFactory.createLiteral(rok+"-01-01T00:00:00", xsd_dateTime));
                        outputDataUnit.add(currentPrenosOdpad, s_endTime, valueFactory.createLiteral(rok+"-12-31T23:59:59", xsd_dateTime));
                        outputDataUnit.add(currentPrenosOdpad, RDF.VALUE, valueFactory.createLiteral(prenosOdpad.substring(0, prenosOdpad.indexOf(' ')), xsd_decimal));
                        outputDataUnit.add(currentPrenosOdpad, s_object, currentChemicalURI);
                        if (prenosOdpad.contains("[C]")) outputDataUnit.add(currentPrenosOdpad, s_instrument, vypocetURI);  
                        if (prenosOdpad.contains("[E]")) outputDataUnit.add(currentPrenosOdpad, s_instrument, odhadURI);  
                        if (prenosOdpad.contains("[M]")) outputDataUnit.add(currentPrenosOdpad, s_instrument, mereniURI);  
                        if (prenosOdpad.contains("[R]")) outputDataUnit.add(currentPrenosOdpad, urceni_odpadu, recyklaceURI);  
                        if (prenosOdpad.contains("[D]")) outputDataUnit.add(currentPrenosOdpad, urceni_odpadu, odstraneniURI);  
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
            
            URI branchIDURI = valueFactory.createURI(irzbrid + rok2 + '/' + bid);
            URI entityURI = valueFactory.createURI(ldbe + eic);
            URI entityIDURI = valueFactory.createURI(entityURI.toString() + "/identifier/cenia.cz");
            URI entityAddressURI = valueFactory.createURI(irzbe + eic + "/adresa/" + rok2);
            URI branchURI = valueFactory.createURI(irzbr + bicp);
            URI branchAddressURI = valueFactory.createURI(branchURI.toString() + "/adresa/" + rok2);
            URI branchGeoURI = valueFactory.createURI(branchURI.toString() + "/geo/" + rok2);
            
            outputDataUnit.add(entityURI, RDF.TYPE, s_Organization);
            outputDataUnit.add(entityURI, RDF.TYPE, gr_BusinessEntity);
            outputDataUnit.add(entityURI, s_name, valueFactory.createLiteral(ename));
            outputDataUnit.add(entityURI, gr_legalName, valueFactory.createLiteral(ename));
            outputDataUnit.add(entityURI, adms_identifier, entityIDURI);
            outputDataUnit.add(entityURI, s_hasPOS, branchURI);

            outputDataUnit.add(entityIDURI, RDF.TYPE, adms_Identifier);
            outputDataUnit.add(entityIDURI, SKOS.NOTATION, valueFactory.createLiteral(eic));
            outputDataUnit.add(entityIDURI, SKOS.PREF_LABEL, valueFactory.createLiteral(eic));
            outputDataUnit.add(entityIDURI, SKOS.IN_SCHEME, ICScheme);

            outputDataUnit.add(entityURI, s_address, entityAddressURI);
            outputDataUnit.add(entityAddressURI, RDF.TYPE, s_PostalAddress);
            outputDataUnit.add(entityAddressURI, DCTERMS.VALID, valueFactory.createLiteral(rok2, xsd_gYear));
            if (!eaddress_street.isEmpty()) outputDataUnit.add(entityAddressURI, s_streetAddress, valueFactory.createLiteral(eaddress_street));
            if (!eaddress_postal.isEmpty()) outputDataUnit.add(entityAddressURI, s_postalCode, valueFactory.createLiteral(eaddress_postal));
            outputDataUnit.add(entityAddressURI, s_addressRegion, valueFactory.createLiteral(eokres));
            if (!eaddress_city.isEmpty()) outputDataUnit.add(entityAddressURI, s_addressLocality, valueFactory.createLiteral(eaddress_city));

            outputDataUnit.add(branchURI, RDF.TYPE, s_Place);
            outputDataUnit.add(branchURI, s_name, valueFactory.createLiteral(bname));
            outputDataUnit.add(branchURI, s_geo, branchGeoURI);
            outputDataUnit.add(branchURI, owl_sameAs, branchIDURI);
            
            outputDataUnit.add(branchURI, s_address, branchAddressURI);
            outputDataUnit.add(branchAddressURI, RDF.TYPE, s_PostalAddress);
            outputDataUnit.add(branchAddressURI, DCTERMS.VALID, valueFactory.createLiteral(rok2, xsd_gYear));
            if (!baddress_street.isEmpty()) outputDataUnit.add(branchAddressURI, s_streetAddress, valueFactory.createLiteral(baddress_street));
            if (!baddress_postal.isEmpty()) outputDataUnit.add(branchAddressURI, s_postalCode, valueFactory.createLiteral(baddress_postal));
            outputDataUnit.add(branchAddressURI, s_addressRegion, valueFactory.createLiteral(bokres));
            if (!baddress_city.isEmpty()) outputDataUnit.add(branchAddressURI, s_addressLocality, valueFactory.createLiteral(baddress_city));
            
            outputDataUnit.add(branchGeoURI, RDF.TYPE, s_GeoCoordinates);
            outputDataUnit.add(branchGeoURI, s_latitude, valueFactory.createLiteral(bwgs_lat));
            outputDataUnit.add(branchGeoURI, s_longitude, valueFactory.createLiteral(bwgs_lng));
            
            break;
        }
    }
}
