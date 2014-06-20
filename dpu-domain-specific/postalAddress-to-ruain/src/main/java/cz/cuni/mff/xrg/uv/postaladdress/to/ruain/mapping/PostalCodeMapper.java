package cz.cuni.mff.xrg.uv.postaladdress.to.ruain.mapping;

import cz.cuni.mff.xrg.uv.postaladdress.to.ruain.query.Requirement;
import cz.cuni.mff.xrg.uv.postaladdress.to.ruain.query.Subject;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Škoda Petr
 */
public class PostalCodeMapper extends StatementMapper {

    @Override
    public boolean canMap(String predicate) {
        return predicate.compareTo("http://schema.org/postalCode") == 0;
    }

    @Override
    public List<Requirement> map(String predicate, String object) throws MappingException {
        final Integer value;
        try {
            value = Integer.parseInt(object.replaceAll("\\s", ""));
        } catch (NumberFormatException ex) {
            throw new MappingException("Failed to parse 'psč'", ex);
        }
        
        final List<Requirement> results = new LinkedList<>();
        results.add(new Requirement(Subject.ADRESNI_MISTO, "r:psc", value.toString()));
        return results;
    }

}
