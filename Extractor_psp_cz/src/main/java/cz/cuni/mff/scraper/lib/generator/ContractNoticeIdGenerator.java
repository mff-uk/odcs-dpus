package cz.cuni.mff.scraper.lib.generator;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jakub Starka
 */
public class ContractNoticeIdGenerator extends DerivedURIGenerator {

    public ContractNoticeIdGenerator(TemplateURIGenerator generator) {
        super(generator);
    }

    @Override
    protected URL generateUrl() {
        try {
            return URIGenerator.getContractNoticeIdUri(generator);
        } catch (MalformedURLException ex) {
            Logger.getLogger(ContractNoticeIdGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }  
}
