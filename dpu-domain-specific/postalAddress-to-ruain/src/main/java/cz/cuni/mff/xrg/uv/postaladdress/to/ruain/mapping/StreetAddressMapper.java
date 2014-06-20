package cz.cuni.mff.xrg.uv.postaladdress.to.ruain.mapping;

import cz.cuni.mff.xrg.uv.postaladdress.to.ruain.streetAddress.StreetAddress;
import cz.cuni.mff.xrg.uv.postaladdress.to.ruain.query.Requirement;
import cz.cuni.mff.xrg.uv.postaladdress.to.ruain.query.Subject;
import cz.cuni.mff.xrg.uv.postaladdress.to.ruain.streetAddress.StreetAddressParser;
import cz.cuni.mff.xrg.uv.postaladdress.to.ruain.streetAddress.WrongAddressFormatException;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Škoda Petr
 */
public class StreetAddressMapper extends StatementMapper {

    private final StreetAddressParser parser = new StreetAddressParser();
    
    @Override
    public boolean canMap(String predicate) {
        return predicate.compareTo("http://schema.org/streetAddress") == 0;
    }

    @Override
    public List<Requirement> map(String predicate, String object) throws MappingException {
        final String value = object.trim();
        final List<Requirement> results = new LinkedList<>();
        // map the value
        StreetAddress address; 
        try {
        address = parser.parse(value);
        }catch (WrongAddressFormatException ex) {
            throw new MappingException("Failed to parse address.", ex);
        }
        // add to requirments - results
        if (address.getTownName() != null) {
            
        }
        if (address.getStreetName() != null) {
            // TODO In some cases this can be name of "obec" we can use
            // KnowlegneBase to determine that
            results.add(new Requirement(Subject.ULICE, "s:name", 
                    String.format("\"%s\"",address.getStreetName())));
        }
        
        if (address.getLandRegistryNumber() != null) {
            // landNumber is a number
            final Integer number;
            try {
                number = Integer.parseInt(address.getLandRegistryNumber()
                        .replaceAll("\\s", ""));
            } catch (NumberFormatException ex) {
                throw new MappingException("Failed to parse 'číslo popisené'", ex);
            }

            results.add(new Requirement(Subject.ADRESNI_MISTO, "r:cisloDomovni",
                    number.toString()));
        }
        return results;
    }

}
