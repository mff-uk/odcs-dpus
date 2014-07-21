package cz.cuni.mff.xrg.uv.postaladdress.to.ruain.mapping;

import cz.cuni.mff.xrg.uv.postaladdress.to.ruain.ontology.Ruian;
import cz.cuni.mff.xrg.uv.postaladdress.to.ruain.streetAddress.StreetAddress;
import cz.cuni.mff.xrg.uv.postaladdress.to.ruain.query.Requirement;
import cz.cuni.mff.xrg.uv.postaladdress.to.ruain.ontology.Subject;
import cz.cuni.mff.xrg.uv.postaladdress.to.ruain.streetAddress.StreetAddressParser;
import cz.cuni.mff.xrg.uv.postaladdress.to.ruain.streetAddress.WrongAddressFormatException;
import java.util.*;

/**
 *
 * @author Škoda Petr
 */
public class StreetAddressMapper extends StatementMapper {

    public static final String NAME = "jméno ulice";
    
    private final StreetAddressParser parser = new StreetAddressParser();

    StreetAddressMapper() { }
    
    @Override
    public String getName() {
        return NAME;
    }    

    @Override
    public List<Requirement> map(String predicate, String object) {
        final String value = object.trim();
        final List<Requirement> results = new LinkedList<>();
        // map the value
        StreetAddress address; 
        try {
            address = parser.parse(value);
        }catch (WrongAddressFormatException ex) {
            errorLogger.failedToMap(predicate, object, "Failed to parse address", ex);
            return Collections.EMPTY_LIST;
        }
        
        // do we have knowledge base?
        if (knowledgeBase == null) {
            mapWithoutKnowledge(address.getTownName(), address.getStreetName(),
                    results);
        } else {
            mapWithKnowledge(address.getTownName(), address.getStreetName(),
                    results);
        }
                
        if (address.getLandRegistryNumber() != null) {
            // check that it's number
            final Integer number;
            try {
                number = Integer.parseInt(address.getLandRegistryNumber()
                        .replaceAll("\\s", ""));
                results.add(new Requirement(Subject.ADRESNI_MISTO, 
                        "<" + Ruian.P_CISLO_DOMOVNI + ">", number.toString()));
            } catch (NumberFormatException ex) {
                errorLogger.failedToMap(predicate, object, "Failed to parse 'číslo popisené'");
            }
        }
        
        if (address.getHouseNumber() != null) {
            // house number can contains multiple records separated with comma
            // in such case we failed .. 
            String houseNumber = address.getHouseNumber().replaceAll("\\s", "");
            if (houseNumber.contains(",")) {
                errorLogger.failedToMap(predicate, object, 
                        "'číslo orientační' contans multiple records so it's ignored");
            } else {
                String houseNumberLetter = null;
                char lastChar = houseNumber.charAt(houseNumber.length() - 1);
                if (Character.isLetter(lastChar)) {
                    // last character is letter
                    houseNumberLetter = "\"" + lastChar + "\"";
                    houseNumber = houseNumber.substring(0, 
                            houseNumber.length() - 1);                    
                }                
                Integer number = parseNumber(houseNumber);
                if (number == null) {
                    errorLogger.failedToMap(predicate, object, 
                            "Failed to parse 'číslo orientační'");
                } else {
                    results.add(new Requirement(Subject.ADRESNI_MISTO, 
                        "<" + Ruian.P_CISLO_ORIENTACNI + ">", number.toString()));
                    // if houseNumberLetter is null then this is considered
                    // to be not exist condition
                    results.add(new Requirement(Subject.ADRESNI_MISTO, 
                        "<" + Ruian.P_CISLO_ORIENTACNI_PISMENO + ">", 
                        houseNumberLetter));
                }
            }
        }        
        return results;
    }

    /**
     * Parse given string as a number.
     * 
     * @param psc
     * @return Null in case of an error.
     */
    private Integer parseNumber(String numberAsString) {
        try {
            return Integer.parseInt(numberAsString);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
    
    private void mapWithoutKnowledge(String townName, String streetName, 
            List<Requirement> results) {
        if (townName != null) {
            results.addAll(createRequirementTownName(
                    Arrays.asList(townName)));
        }
        if (streetName != null) {
            results.addAll(createRequirementStreetName(
                    Arrays.asList(streetName)));
        }        
    }

    /**
     * Use {@link #knowledgeBase} to check the validity of given names.
     * 
     * @param townName
     * @param streetName
     * @param results 
     */
    private void mapWithKnowledge(String townName, String streetName, 
            List<Requirement> results) {
        
        List<String> checkedTownName = knowledgeBase.checkTownName(townName);
        List<String> checkedStreetName = knowledgeBase.checkStreetName(streetName);
        
        if (checkedStreetName != null) {
            // it's street name
            results.addAll(createRequirementStreetName(checkedStreetName));
        }
        
        if (checkedTownName != null) {
            // it's town name
            results.addAll(createRequirementTownName(checkedTownName));
        }
        
        // if checked town or street name is null, we check for
        // switch        
        if (checkedStreetName == null) {
            List<String> streetAsTown = knowledgeBase.checkTownName(streetName);
            if (streetAsTown != null) {
                if (checkedTownName == null) {
                    // street name is in fact town name, and original town name
                    // is missing
                    results.addAll(createRequirementTownName(streetAsTown));
                } else {
                    // TODO street name is twice here
                }
            }
        }
        
        if (checkedTownName == null) {
            List<String> townAsStreet = knowledgeBase.checkStreetName(townName);
            if (townAsStreet != null) {
                if (checkedTownName == null) {
                    // town name is street name
                    results.addAll(createRequirementStreetName(townAsStreet));
                } else {
                    // TODO town name is twice here
                }
            }
        }        
    }    
    
    private List<Requirement> createRequirementTownName(List<String> townName) {
        List<Requirement> result = new ArrayList<>(townName.size());
        for (String item : townName) {
            result.add(new Requirement(Subject.OBEC, "<" + Ruian.P_NAME + ">",
                    String.format("\"%s\"", item)));
        }
        return result;
    }
    
    private List<Requirement> createRequirementStreetName(List<String> streetName) {
        List<Requirement> result = new ArrayList<>(streetName.size());
        for (String item : streetName) {
            result.add(new Requirement(Subject.ULICE, "<" + Ruian.P_NAME + ">",
                    String.format("\"%s\"", item)));
        }
        return result;        
    }
    
    
}
