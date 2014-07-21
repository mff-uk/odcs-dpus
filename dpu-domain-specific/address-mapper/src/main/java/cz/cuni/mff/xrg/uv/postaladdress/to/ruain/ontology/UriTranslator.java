package cz.cuni.mff.xrg.uv.postaladdress.to.ruain.ontology;

import java.util.HashMap;
import java.util.Map;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;

/**
 *
 * @author Škoda Petr
 */
public class UriTranslator {
    
    private final static Map<String, URI> map = new HashMap<>();
    
    public static void add(ValueFactory factory, String uriAsString) {
        map.put(uriAsString, factory.createURI(uriAsString));
    }
    
    public static URI toUri(String uriAsString) {
        return map.get(uriAsString);
    }
    
}
