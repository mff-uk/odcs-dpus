package cz.cuni.mff.scraper.lib.generator;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jakub Starka
 */
public class IdGenerator extends TemplateURIGenerator {

    private static HashMap<String, Integer> ids = new HashMap<String, Integer>();
        
    private TemplateURIGenerator gen;
    private String name;
    
    public static void reset() {
        ids = new HashMap<String, Integer>();
    }
    
    public IdGenerator(String name, TemplateURIGenerator gen) {
        super(null);
        this.name = name;
        this.gen = gen;
    }

    @Override
    protected URL generateUrl() {
        try {
            Integer id = 1;
            if (ids.containsKey(name)) {
                id = ids.get(name);
            }
            ids.put(name, id + 1);
            return new URL(gen.getUrl().toString() + "/identifier/" +  id);
        } catch (MalformedURLException ex) {
            Logger.getLogger(URIGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
}
