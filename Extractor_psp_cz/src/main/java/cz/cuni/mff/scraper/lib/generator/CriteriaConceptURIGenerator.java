package cz.cuni.mff.scraper.lib.generator;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jakub Starka
 */
public class CriteriaConceptURIGenerator extends TemplateURIGenerator {

    public CriteriaConceptURIGenerator() {
        super();
    }

    
    
    public CriteriaConceptURIGenerator(String contractId) {
        super(contractId);
    }

    @Override
    public Boolean isQName() {
        return false;
    }
    
    

    @Override
    protected URL generateUrl() {
        try {
            return new URL("http://purl.org/procurement/public-contracts-criteria#" + UUID.randomUUID());
        } catch (MalformedURLException ex) {
            Logger.getLogger(URIGenerator.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        //throw new Error("Should not be called");
    }
    
    public String getQName() {
        throw new Error("Should not be called");
        //return "criteria:" + UUID.randomUUID();
    }
    
}
