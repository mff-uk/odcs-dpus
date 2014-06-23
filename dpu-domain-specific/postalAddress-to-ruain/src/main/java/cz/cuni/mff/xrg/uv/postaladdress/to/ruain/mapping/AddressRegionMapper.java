package cz.cuni.mff.xrg.uv.postaladdress.to.ruain.mapping;

import cz.cuni.mff.xrg.uv.postaladdress.to.ruain.query.Requirement;
import cz.cuni.mff.xrg.uv.postaladdress.to.ruain.query.Subject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Škoda Petr
 */
public class AddressRegionMapper extends StatementMapper {

    private final String[] vuscSource = {
        "Kraj Vysočina",
        "Jihomoravský kraj",
        "Olomoucký kraj",
        "Moravskoslezský kraj",
        "Zlínský kraj",
        "Hlavní město Praha",
        "Středočeský kraj",
        "Jihočeský kraj",
        "Plzeňský kraj",
        "Karlovarský kraj",
        "Ústecký kraj",
        "Liberecký kraj",
        "Královéhradecký kraj",
        "Pardubický kraj"};

    private final Map<String, String> vuscMap = new HashMap<>();

    public AddressRegionMapper(ErrorLogger errorLogger) {
        super(errorLogger);
        // we convert vuscSource into vuscMap
        // so we can map in lowerCase
        for (String item : vuscSource) {
            vuscMap.put(item.toLowerCase(), item);
        }
    }

    @Override
    public boolean canMap(String predicate) {
        return predicate.compareTo("http://schema.org/addressRegion") == 0;
    }

    @Override
    public List<Requirement> map(String predicate, String object) {
        final List<Requirement> results = new LinkedList<>();

        if (vuscMap.containsKey(object.toLowerCase())) {
            results.add(new Requirement(Subject.VUSC, "s:name", "\""
                    + vuscMap.get(object.toLowerCase()) + "\""));
            return results;
        } else {
            // iterate over list and search for match
            // helps in cases where the "Kraj" "kraj" is omited
            // specially in case of coi.cz Kraj Vysočina is denoted as Vysočina
            String objectLowerCase = object.toLowerCase();
            for (String key : vuscMap.keySet()) {
                if (key.contains(objectLowerCase)) {
                    results.add(new Requirement(Subject.VUSC, "s:name", "\""
                        + vuscMap.get(key) + "\""));
                    return results;
                }
            }
        }

        // we do not know what to map ..        
        return results;
    }

}
