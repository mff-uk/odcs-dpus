package cz.cuni.mff.xrg.uv.utils.dataunit;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.MetadataDataUnit;

/**
 *
 * @author Škoda Petr
 */
public class DataUnitUtils {

    /**
     * Prefix for generated symbolic names.
     */
    public static final String GENERATED_SYMBOLIC_NAME_PREFIX = "http://unifiedviews.eu/resource/generated/";

    private DataUnitUtils() {
        
    }

    /**
     * Generate a new and unique symbolic name.
     *
     * @param dpuClass
     * @return
     */
    public static String generateSymbolicName(Class<?> dpuClass) {
        return GENERATED_SYMBOLIC_NAME_PREFIX + dpuClass.getSimpleName() +
                Long.toString((new Date()).getTime());
    }
    
    /**
     * Load entries into memory. Should be used only with reasonable small number of entries.
     * 
     * @param <T> DataUnit type.
     * @param dataUnit
     * @param resultClass Type of entries to retrieve.
     * @return List of entries. 
     * @throws eu.unifiedviews.dataunit.DataUnitException 
     */
    public static <T extends MetadataDataUnit, E extends T.Entry> List<E> getEntries(T dataUnit,
            Class<E> resultClass)
            throws DataUnitException {
        final List<E> result = new LinkedList<>();
        try (MetadataDataUnit.Iteration iter = dataUnit.getIteration()) {
            while (iter.hasNext()) {
                result.add((E)iter.next());
            }
        }
        return result;
    }

}
