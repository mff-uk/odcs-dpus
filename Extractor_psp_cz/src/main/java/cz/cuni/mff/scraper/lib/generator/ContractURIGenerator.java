package cz.cuni.mff.scraper.lib.generator;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jakub Starka
 */
public class ContractURIGenerator extends TemplateURIGenerator {

    String noticeId;
    String contractId;
    String lotId;
    
    
    public ContractURIGenerator(String contractId, String documentId) {
        this(contractId, documentId, null);
    }
    
    public ContractURIGenerator(String contractId, String documentId, String lotId) {
        super(null);
        this.contractId = contractId;
        this.noticeId = documentId;
        this.lotId = lotId;
    }
    
    public boolean exists = false;
    
    @Override
    protected URL generateUrl() {
        if (uri == null) {
            try {
                uri = URIGenerator.getContractURI(contractId, noticeId, lotId);
            } catch (MalformedURLException ex) {
                Logger.getLogger(ContractURIGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return uri;
    }   
}
