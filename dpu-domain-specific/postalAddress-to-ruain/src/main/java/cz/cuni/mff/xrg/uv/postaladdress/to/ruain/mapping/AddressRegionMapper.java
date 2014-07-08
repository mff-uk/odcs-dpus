package cz.cuni.mff.xrg.uv.postaladdress.to.ruain.mapping;

import cz.cuni.mff.xrg.uv.postaladdress.to.ruain.knowledge.KnowledgeBase;
import cz.cuni.mff.xrg.uv.postaladdress.to.ruain.query.Requirement;
import cz.cuni.mff.xrg.uv.postaladdress.to.ruain.ontology.Subject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Škoda Petr
 */
public class AddressRegionMapper extends StatementMapper {

    public static final String NAME = "kraj";
    
    private final Map<String, String> vuscMap = new HashMap<>();

    AddressRegionMapper() { }
    
    @Override
    public void bind(ErrorLogger errorLogger, List<String> uri,
            KnowledgeBase knowledgeBase) {
        super.bind(errorLogger, uri, knowledgeBase);
        for (String item : knowledgeBase.getRegions()) {
            vuscMap.put(item.toLowerCase(), item);
        }
    }
    
    @Override
    public String getName() {
        return NAME;
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
