package cz.cuni.mff.xrg.uv.postaladdress.to.ruain.knowledge;

import cz.cuni.mff.xrg.uv.rdf.simple.ConnectionPair;
import cz.cuni.mff.xrg.uv.rdf.simple.SimpleRdfRead;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.openrdf.model.Value;
import org.openrdf.query.TupleQueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Škoda Petr
 */
public class KnowledgeBase {

    private static final Logger LOG = LoggerFactory.getLogger(
            KnowledgeBase.class);

    private final Map<String, List<String>> streets = new HashMap<>();

    private final Map<String, List<String>> towns = new HashMap<>();

    private final Map<String, List<String>> townParts = new HashMap<>();
    
    private final List<String> regions = new LinkedList<>();
    
    public KnowledgeBase() {

    }

    public boolean isStreetNameEmpty() {
        return streets.isEmpty();
    }

    public boolean isTownNameEmpty() {
        return towns.isEmpty();
    }
   
    /**
     * Check is given streetName represents street name. Check is case
     * insensitive.
     *
     * If the streetName match existing street name then the street name is
     * returned, as extracted from knowledge base.
     *
     * @param streetName
     * @return KnowledgeValue.value = null if no street of given name exists.
     */
    public List<String> checkStreetName(String streetName) {
        if (streetName == null) {
            return null;
        }
        return streets.get(streetName.toLowerCase());
    }

    /**
     * Works same as {@link #checkStreetName(java.lang.String)} but only for
     * town names.
     *
     * @param townName
     * @return
     */
    public List<String> checkTownName(String townName) {
        if (townName == null) {
            return null;
        }
        return towns.get(townName.toLowerCase());
    }

    public List<String> checkTownPartName(String townPartName) {
        if (townParts == null) {
            return null;
        }
        return townParts.get(townPartName.toLowerCase());
    }
    
    /**
     *
     * @param rdf
     * @param genAlternatives If true then alternatives are generated to stored
     *                        name, should be true only for lass call of this
     *                        function.
     * @throws Exception
     */
    public void loadStreetNames(SimpleRdfRead rdf, boolean genAlternatives)
            throws Exception {
        loadCache(rdf, streets);
        LOG.info("Street name cache size: {}++", streets.size());
        if (genAlternatives) {
            generateStreetNameAlternatives();
        }
    }

    public void loadTownNames(SimpleRdfRead rdf) throws Exception {
        loadCache(rdf, towns);
        LOG.info("Town name cache size: {}", towns.size());
    }

    public void loadRegionNames(SimpleRdfRead rdf) throws Exception {
        final String query = "SELECT DISTINCT ?s WHERE {[] <http://schema.org/name> ?s}";

        try (ConnectionPair<TupleQueryResult> conn = rdf.executeSelectQuery(
                query)) {
            TupleQueryResult iter = conn.getObject();
            while (iter.hasNext()) {
                final Value value = iter.next().getBinding("s").getValue();
                regions.add(value.stringValue());
            }
        }
        
        LOG.info("Region name cache size: {}", regions.size());
    }

    public void loadTownPartNames(SimpleRdfRead rdf) throws Exception {
        loadCache(rdf, townParts);
        LOG.info("Town-parts name cache size: {}", townParts.size());
    }    
    
    public List<String> getRegions() {
        return regions;
    }
    
    private void loadCache(SimpleRdfRead rdf, Map<String, List<String>> cache)
            throws Exception {
        final String query = "SELECT DISTINCT ?s WHERE {[] <http://schema.org/name> ?s}";

        try (ConnectionPair<TupleQueryResult> conn = rdf.executeSelectQuery(
                query)) {
            TupleQueryResult iter = conn.getObject();
            while (iter.hasNext()) {
                final Value value = iter.next().getBinding("s").getValue();
                final String valueStr = value.stringValue().trim();
                final String valueStrLower = valueStr.toLowerCase();

                if (!cache.containsKey(valueStrLower)) {
                    // new key, add and create
                    cache.put(valueStrLower, new LinkedList<String>());
                }
                cache.get(valueStr.toLowerCase()).add(valueStr);
            }
        }
    }

    /**
     * Generates additional keys in {@link #streets} to solve problems with
     * shortcuts etc. Should be called only once.
     */
    private void generateStreetNameAlternatives() {
        Map<String, List<String>> toAdd = new HashMap<>();

        for (String key : streets.keySet()) {
            // add alternatives
            if (key.contains("náměstí")) {
                String newKey = key.replace("náměstí", "nám.");
                toAdd.put(newKey, streets.get(key));
            }
        }

        // add
        for (String key : toAdd.keySet()) {
            if (streets.containsKey(key)) {
                // append to existing
                // TODO secure that the instances are not added twice here!
                streets.get(key).addAll(toAdd.get(key));
            } else {
                // add new
                streets.put(key, toAdd.get(key));
            }
        }
    }

}
