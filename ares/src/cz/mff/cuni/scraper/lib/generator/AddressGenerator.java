package cz.mff.cuni.scraper.lib.generator;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jakub Starka
 */
public class AddressGenerator extends TemplateURIGenerator {

    private TemplateURIGenerator gen;
    
    public AddressGenerator(TemplateURIGenerator gen) {
        super(null);
        this.gen = gen;
    }

    @Override
    protected URL generateUrl() {
        try {
            return new URL(gen.getUrl().toString() + "/postal-address/1");
        } catch (MalformedURLException ex) {
            Logger.getLogger(URIGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
}
