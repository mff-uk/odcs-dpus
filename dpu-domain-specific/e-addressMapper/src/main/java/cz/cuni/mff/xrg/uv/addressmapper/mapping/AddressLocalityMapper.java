package cz.cuni.mff.xrg.uv.addressmapper.mapping;

import cz.cuni.mff.xrg.uv.addressmapper.ontology.Ruian;
import cz.cuni.mff.xrg.uv.addressmapper.ontology.Subject;
import cz.cuni.mff.xrg.uv.addressmapper.query.Requirement;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Škoda Petr
 */
public class AddressLocalityMapper extends StatementMapper {
    
    public static final String NAME = "adresa regionu";
        
    AddressLocalityMapper() {}
    
    @Override
    public String getName() {
        return NAME;
    }    

    @Override
    public List<Requirement> map(String predicate, String object) {
        final List<Requirement> results = new LinkedList<>();
        final String[] objectSplit = object.split(",", 2);
        
        final List<String> obce = new LinkedList<>();
        final List<String> castiObci = new LinkedList<>();

        List<String> newObec = knowledgeBase.checkTownName(objectSplit[0].trim());
        if (newObec != null) {
            obce.addAll(newObec);
        }
        
        if (objectSplit.length > 1) {
            List<String> newCastiObeci = knowledgeBase.checkTownName(objectSplit[1].trim());
            if (newCastiObeci != null) {
                castiObci.addAll(newCastiObeci);
            }
        }
        
        // add to requirements
        for (String item : obce) {
            results.add(new Requirement(Subject.OBEC, "<" + Ruian.P_NAME + ">", 
                    "\"" + item + "\""));
        }
        for (String item : castiObci) {
            results.add(new Requirement(Subject.CASTIOBCI, 
                    "<" + Ruian.P_NAME + ">", "\"" + item + "\""));
        }
        return results;
    }    
    
}

/*
Praha, Praha 11 - Háje
Zlín, Malenovice
Tismice
Brno, Brno-střed - Štýřice
*/