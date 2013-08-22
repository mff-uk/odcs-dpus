package cz.cuni.mff.scraper.lib.generator;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jakub Starka
 */
public class TenderURIGenerator extends DerivedURIGenerator {
    
    public TenderURIGenerator(TemplateURIGenerator generator) {
        super(generator);
    }

    @Override
    protected URL generateUrl() {
        try {
            return URIGenerator.getTenderURI(generator);
        } catch (MalformedURLException ex) {
            Logger.getLogger(URIGenerator.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
}
