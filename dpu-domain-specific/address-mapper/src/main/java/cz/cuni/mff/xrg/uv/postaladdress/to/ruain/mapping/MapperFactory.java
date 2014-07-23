package cz.cuni.mff.xrg.uv.postaladdress.to.ruain.mapping;

import cz.cuni.mff.xrg.odcs.commons.configuration.ConfigException;
import cz.cuni.mff.xrg.uv.postaladdress.to.ruain.knowledge.KnowledgeBase;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Create {@link StatementMapper}s based on given configuration.
 * 
 * @author Škoda Petr
 */
public class MapperFactory {

    /**
     * List of all {@link StatementMapper}s that are available.
     */
    private static final StatementMapper [] MAPPERS = {
        new AddressRegionMapper(), 
        new PostalCodeMapper(), 
        new StreetAddressMapper(),
        new AddressLocalityMapper()};
    
    private MapperFactory() { }
    
    public static List<StatementMapper> construct(ErrorLogger errorLogger, 
            KnowledgeBase knowledgeBase, 
            Map<String, List<String>> configuration) throws ConfigException {
        if (configuration == null) {
            throw new ConfigException("No configuration for mappers!");
        }
        
        List<StatementMapper> usedMappers = new LinkedList<>();
                
        for (String name : configuration.keySet()) {
            StatementMapper mapper = null;
            for (StatementMapper item : MAPPERS) {
                if (item.getName().compareToIgnoreCase(name) == 0) {
                    mapper = item;
                    break;
                }
            }
            if (mapper == null) {
                throw new ConfigException(String.format("Unknown mapper name: %s", name));
            } else {
                // add, as the name is from map we can have the same 
                // mapper used twice
                mapper.bind(errorLogger, configuration.get(name), knowledgeBase);
                usedMappers.add(mapper);
            }            
        }        
        return usedMappers;
    }
    
    public static List<String> getNames() {
        List<String> names = new ArrayList<>(MAPPERS.length);
        
        for (StatementMapper mapper : MAPPERS) {
            names.add(mapper.getName());
        }
        
        return names;
    }
    
}
