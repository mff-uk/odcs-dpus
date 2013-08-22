package cz.cuni.mff.scraper.lib.generator;

import cz.cuni.mff.scraper.lib.selector.CssSelector;
import cz.cuni.mff.scraper.lib.selector.Selector;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

/**
 *
 * @author Jakub Starka
 */
public class URIGenerator {
    
    public static String baseUri = "http://ld.opendata.cz/resource/isvzus.cz/";
    
    public static URL getCPVURI(String cpvIdS) throws MalformedURLException {
        if (cpvIdS.contains("-")) {
            cpvIdS = cpvIdS.substring(0, cpvIdS.indexOf("-")).trim();
        }
        cpvIdS = cpvIdS.replaceAll("[^0-9]", "");
        if (cpvIdS.equals("")) {
            return null;
        }
        Integer cpvId;
        try {
            cpvId = Integer.parseInt(cpvIdS);
        } catch (NumberFormatException ex) {
            return null;
        }
        String cpvOut;
        if (cpvId < 10000000) {
            cpvOut = String.format("%08d", cpvId);
        } else {
            cpvOut = cpvIdS;
        }
        return new URL("http://purl.org/weso/cpv/2008/" + cpvOut);
    }
    
    public static URL getCPVURI(CssSelector cpv) throws MalformedURLException {
        if (cpv == null || cpv.getValue() == null || cpv.getValue().equals("")) {
            return null;
        } else {
            String cpvIdS = cpv.getValue();
            return getCPVURI(cpvIdS);
        }
            
    }
    
    public static URL getNUTSURI(Selector nuts) throws MalformedURLException {
        if (nuts == null || nuts.getValue() == null || nuts.getValue().equals("")) {
            return null;
        }
        return new URL("http://ec.europa.eu/eurostat/ramon/rdfdata/nuts2008/" + nuts.getValue());
    }
    
    public static URL getURI(String address, CssSelector id) throws MalformedURLException {
	if (id == null || id.getValue().equals("")) {
	    return null;
	} else {
            String selector_part = id.getValue();
            
	    return new URL(address.concat(id.getValue()));
	}
    }

    private static int priceId = 1;
    private static int contactId = 1;
    private static int awardCombinationId = 1;
    private static int awardCriterionId = 1;
    private static int tenderId = 1;
    private static int placeId = 1;
    
    //private static Map<String, Int> increments;
    
    public static void reset() {
        priceId = 1;
        contactId = 1;
        awardCombinationId = 1;
        awardCriterionId = 1;
        tenderId = 1;
        placeId = 1;
    }
    
    /**
     * Depends on contract URI
     * @param contractId
     * @return
     * @throws MalformedURLException 
     */
    public static URL getContactURI(TemplateURIGenerator generator) throws MalformedURLException {
        return new URL(generator.getUrl().toString() + "/vcard-class/" + contactId++);
    }
    
    /**
     * Depends on BE URI
     * @param contractId
     * @return
     * @throws MalformedURLException 
     */
    public static URL getPlaceURI(TemplateURIGenerator generator) throws MalformedURLException {
        return new URL(generator.getUrl().toString() + "/place/" + placeId++);
    }
    
    /**
     * Depends on contract URI
     * @param contractId
     * @return
     * @throws MalformedURLException 
     */
    public static URL getPriceURI(TemplateURIGenerator generator) throws MalformedURLException {
        return new URL(generator.getUrl().toString() + "/price-specification/" + priceId++);
    }
    
    /**
     * Depends on contract URI
     * @param contractId
     * @return
     * @throws MalformedURLException 
     */
    public static URL getTenderURI(TemplateURIGenerator generator) throws MalformedURLException {
        return new URL(generator.getUrl().toString() + "/tender/" + tenderId++);
    }
    
    /**
     * No dependencies
     * @param contractId
     * @return
     * @throws MalformedURLException 
     */
    public static URL getContractURI(String contractId, String noticeId, String lotId) throws MalformedURLException {
        if (lotId != null) {
            String id = UUID.randomUUID().toString();
            return new URL(baseUri + "public-contract/" + contractId + '-' + noticeId + '-' + id);            
        } else {
            return new URL(baseUri + "public-contract/" + contractId + '-' + noticeId);
        }
    }
    
    /**
     * Depends on contract URI
     * @param contractId
     * @return
     * @throws MalformedURLException 
     */
    public static URL getAwardCriterionCombinationUri(TemplateURIGenerator generator) throws MalformedURLException {
        return new URL(generator.getUrl().toString() + "/combination-of-contract-award-criteria/" + awardCombinationId++);
    }
    
    /**
     * Depends on award criterion combination URI
     * @param contractId
     * @return
     * @throws MalformedURLException 
     */
    public static URL getAwardCriterionUri(TemplateURIGenerator generator) throws MalformedURLException {
        return new URL(generator.getUrl().toString() + "/contract-award-criterion/" +  awardCriterionId++);
    }

    /**
     * No dependencies
     * @param contractId
     * @return
     * @throws MalformedURLException 
     */
    public static URL getContractNoticeUri(String noticeId, String type) throws MalformedURLException {
        return new URL(baseUri  + type + "/" + noticeId);
    }    
    
    /**
     * Depends on contract URI
     * @param contractId
     * @return
     * @throws MalformedURLException 
     */
    public static URL getContractNoticeIdUri(TemplateURIGenerator generator) throws MalformedURLException {
        return new URL(generator.toString() + "/identifier/1");
    }
}
