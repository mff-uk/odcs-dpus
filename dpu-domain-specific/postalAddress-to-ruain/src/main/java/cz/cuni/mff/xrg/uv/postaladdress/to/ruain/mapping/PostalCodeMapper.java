package cz.cuni.mff.xrg.uv.postaladdress.to.ruain.mapping;

import cz.cuni.mff.xrg.uv.postaladdress.to.ruain.query.Requirement;
import cz.cuni.mff.xrg.uv.postaladdress.to.ruain.ontology.Subject;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Škoda Petr
 */
public class PostalCodeMapper extends StatementMapper {

    public static final String NAME = "psč";
        
    PostalCodeMapper() {}
    
    @Override
    public String getName() {
        return NAME;
    }    

    @Override
    public List<Requirement> map(String predicate, String object) {
        final Integer value;
        try {
            value = Integer.parseInt(object.replaceAll("\\s", ""));
        } catch (NumberFormatException ex) {
            errorLogger.failedToMap(predicate, object, "Can't parse 'psč'");
            return Collections.EMPTY_LIST;
        }
        
        final List<Requirement> results = new LinkedList<>();
        results.add(new Requirement(Subject.ADRESNI_MISTO, "r:psc", value.toString()));
        return results;
    }

}
