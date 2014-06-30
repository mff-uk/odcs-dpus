package cz.cuni.mff.xrg.uv.postaladdress.to.ruain.knowledge;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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

    public KnowledgeBase(File streetNameFile, File townNameFile) {
        loadCache(streetNameFile, streets);
        loadCache(townNameFile, towns);
        //
        generateStreetNameAlternatives();
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

    public List<String> checkTownName(String townName) {
        if (townName == null) {
            return null;
        }
        return towns.get(townName.toLowerCase());
    }

    private void loadCache(File file, Map<String, List<String>> cache) {
        try (FileInputStream is = new FileInputStream(file);
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(is, "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                final String lineLower = line.toLowerCase();
                if (!cache.containsKey(lineLower)) {
                    // add as alternative
                    cache.put(lineLower, new LinkedList<String>());
                }
                cache.get(line.toLowerCase()).add(line);
            }
        } catch (IOException e) {
            LOG.error("Failed to load address cache.", e);
        }
    }

    /**
     * Generates additional keys in {@link #streets} to solve problems with
     * shortcuts etc. 
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
                streets.get(key).addAll(toAdd.get(key));
            } else {
                // add new
                streets.put(key, toAdd.get(key));
            }
        }
    }
    
}
