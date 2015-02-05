package cz.cuni.mff.xrg.uv.utils.dataunit;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.openrdf.model.URI;

import cz.cuni.mff.xrg.uv.utils.dataunit.rdf.RdfDataUnitUtils;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.MetadataDataUnit;
import eu.unifiedviews.dataunit.WritableMetadataDataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;

/**
 *
 * @author Škoda Petr
 */
public class DataUnitUtils {

    /**
     * Prefix for generated symbolic names.
     */
    public static final String GENERATED_SYMBOLIC_NAME_PREFIX = "http://unifiedviews.eu/resource/generated/";

    public static final String METADATA_GRAPH_SYMBOLIC_NAME = "metadata";

    public static final String WRITABLE_METADATA_GRAPH_SYMBOLIC_NAME = "writable-metadata";

    private DataUnitUtils() {

    }

    /**
     * Generate a new and unique symbolic name.
     *
     * @param dpuClass
     * @return
     */
    public static String generateSymbolicName(Class<?> dpuClass) {
        return GENERATED_SYMBOLIC_NAME_PREFIX + dpuClass.getSimpleName()
                + Long.toString((new Date()).getTime());
    }

    /**
     * Load entries into memory. Should be used only with reasonable small number of entries.
     *
     * @param <T>         DataUnit type.
     * @param dataUnit
     * @param resultClass Type of entries to retrieve.
     * @return List of entries.
     * @throws eu.unifiedviews.dataunit.DataUnitException
     */
    public static <T extends MetadataDataUnit, E extends T.Entry> List<E> getEntries(T dataUnit,
            Class<E> resultClass) throws DataUnitException {
        final List<E> result = new LinkedList<>();
        try (MetadataDataUnit.Iteration iter = dataUnit.getIteration()) {
            while (iter.hasNext()) {
                result.add((E) iter.next());
            }
        }
        return result;
    }

    /**
     * Provide access to metadata in same form as to any other graph. As a symbolic name fixed value is used.
     * Do not write into input metadata graphs as it would corrupt data for other DPUs.
     *
     * @param dataUnit
     * @return
     */
    public static List<RDFDataUnit.Entry> getMetadataEntries(MetadataDataUnit dataUnit)
            throws DataUnitException {
        final List<RDFDataUnit.Entry> result = new LinkedList<>();
        for (URI graphName : dataUnit.getMetadataGraphnames()) {
            result.add(new RdfDataUnitUtils.InMemoryEntry(graphName, METADATA_GRAPH_SYMBOLIC_NAME));
        }
        return result;
    }

    /**
     * Provide access to metadata in the same form as to any other graph. This graph can be used to write
     * operation but it should be used with caution.
     *
     * @param dataUnit
     * @return
     * @throws DataUnitException
     */
    public static RDFDataUnit.Entry getWritableMetadataEntry(WritableMetadataDataUnit dataUnit) 
            throws DataUnitException {
        return new RdfDataUnitUtils.InMemoryEntry(dataUnit.getMetadataWriteGraphname(),
                WRITABLE_METADATA_GRAPH_SYMBOLIC_NAME);
    }

}
