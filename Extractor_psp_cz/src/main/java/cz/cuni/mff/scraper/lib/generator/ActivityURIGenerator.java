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
public class ActivityURIGenerator extends TemplateURIGenerator {

    public ActivityURIGenerator() {
        super(null);
    }

    @Override
    protected URL generateUrl() {
        try {
            return new URL("http://purl.org/procurement/public-contracts-activities#" + UUID.randomUUID());
        } catch (MalformedURLException ex) {
            Logger.getLogger(URIGenerator.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
}
