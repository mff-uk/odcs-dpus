package cz.cuni.mff.xrg.uv.postaladdress.to.ruain.knowledge;

import java.io.*;
import java.util.HashMap;
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

    private final Map<String, String> streets = new HashMap<>();

    private final Map<String, String> towns = new HashMap<>();

    public KnowledgeBase(File streetNameFile, File townNameFile) {
        // load streets
        try (FileInputStream is = new FileInputStream(streetNameFile);
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(is, "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                streets.put(line.toLowerCase(), line);
            }
        } catch (IOException e) {
            LOG.error("Failed to load address cache.", e);
        }
        // load towns
        try (FileInputStream is = new FileInputStream(townNameFile);
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(is, "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                towns.put(line.toLowerCase(), line);
            }
        } catch (IOException e) {
            LOG.error("Failed to load address cache.", e);
        }
    }

    /**
     * Check is given streetName represents street name. Check is case
     * insensitive.
     *
     * If the streetName match existing street name then the street name is
     * returned, as extracted from knowledge base.
     *
     * @param streetName
     * @return Null is no street of given name exists.
     */
    public String checkStreetName(String streetName) {
        if (streetName == null) {
            return null;
        }
        return streets.get(streetName.toLowerCase());
    }

    public String checkTownName(String townName) {
        if (townName == null) {
            return null;
        }
        return towns.get(townName.toLowerCase());
    }

}
