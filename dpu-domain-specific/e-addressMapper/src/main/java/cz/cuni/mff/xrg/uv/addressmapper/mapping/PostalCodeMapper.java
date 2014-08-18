package cz.cuni.mff.xrg.uv.addressmapper.mapping;

import cz.cuni.mff.xrg.uv.addressmapper.ontology.Ruian;
import cz.cuni.mff.xrg.uv.addressmapper.query.Requirement;
import cz.cuni.mff.xrg.uv.addressmapper.ontology.Subject;
import java.util.Arrays;
import java.util.Collections;
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
        
        return Arrays.asList(new Requirement(Subject.ADRESNI_MISTO, 
                "<" + Ruian.P_PSC + ">", value.toString()));
    }

}
