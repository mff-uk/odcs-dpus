package cz.cuni.mff.xrg.uv.addressmapper.mapping;

import cz.cuni.mff.xrg.uv.addressmapper.knowledge.KnowledgeBase;
import eu.unifiedviews.dpu.config.DPUConfigException;
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
            Map<String, List<String>> configuration) throws DPUConfigException {
        if (configuration == null) {
            throw new DPUConfigException("No configuration for mappers!");
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
                throw new DPUConfigException(
                        String.format("Unknown mapper name: %s", name));
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
