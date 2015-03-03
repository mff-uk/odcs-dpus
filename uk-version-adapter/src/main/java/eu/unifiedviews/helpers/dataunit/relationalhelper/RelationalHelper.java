package eu.unifiedviews.helpers.dataunit.relationalhelper;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.relational.RelationalDataUnit;

/**
 * Helper to make various tasks with {@link TabularDataUnit} friendly.
 */
public class RelationalHelper {
    
    /**
     * Exhaust {@link eu.unifiedviews.dataunit.relational.RelationalDataUnit.Iteration} (obtained using {@link eu.unifiedviews.dataunit.relational.RelationalDataUnit#getIteration()}) into one {@link Map} of entries.
     * Beware - if the {@link eu.unifiedviews.dataunit.relational.RelationalDataUnit} contains milions or more entries, storing all of this in single {@link Map} is not a good idea.
     * Only suitable for work with ~100000 of entries (tables)
     *
     * @param relationalDataUnit data unit from which the iteration will be obtained and exhausted
     * @return {@link Map} containing all entries, keys are symbolic names
     * @throws DataUnitException
     */
    public static Map<String, RelationalDataUnit.Entry> getTablesMap(RelationalDataUnit relationalDataUnit) throws DataUnitException {
        if (relationalDataUnit == null) {
            return new LinkedHashMap<>();
        }
        RelationalDataUnit.Iteration iteration = relationalDataUnit.getIteration();
        Map<String, RelationalDataUnit.Entry> resultSet = new LinkedHashMap<>();
        try {
            while (iteration.hasNext()) {
                RelationalDataUnit.Entry entry = iteration.next();
                resultSet.put(entry.getSymbolicName(), entry);
            }
        } finally {
            iteration.close();
        }
        return resultSet;
    }
    
    /**
     * Exhaust {@link eu.unifiedviews.dataunit.relational.RelationalDataUnit.Iteration} (obtained using {@link eu.unifiedviews.dataunit.relational.RelationalDataUnit#getIteration()}) into one {@link Set} of entries.
     * Beware - if the {@link eu.unifiedviews.dataunit.relational.RelationalDataUnit} contains milions or more entries, storing all of this in single {@link Set} is not a good idea.
     * Only suitable for work with ~100000 of entries (tables)
     *
     * @param relationalDataUnit data unit from which the iteration will be obtained and exhausted
     * @return {@link Set} containing all entries, keys are symbolic names
     * @throws DataUnitException
     */
    public static Set<RelationalDataUnit.Entry> getTables(RelationalDataUnit relationalDataUnit) throws DataUnitException {
        if (relationalDataUnit == null) {
            return new LinkedHashSet<>();
        }
        RelationalDataUnit.Iteration iteration = relationalDataUnit.getIteration();
        Set<RelationalDataUnit.Entry> resultSet = new LinkedHashSet<>();
        try {
            while (iteration.hasNext()) {
                RelationalDataUnit.Entry entry = iteration.next();
                resultSet.add(entry);
            }
        } finally {
            iteration.close();
        }
        return resultSet;
    }

}
