package cz.cuni.mff.xrg.uv.utils.dataunit;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.MetadataDataUnit;
import eu.unifiedviews.dataunit.WritableMetadataDataUnit;

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

    /**
     * Copy content of metadata graphs from input to output.
     *
     * @param sourceDataUnit
     * @param targetDataUnit
     * @param connection
     * @throws DataUnitException
     * @throws RepositoryException
     * @throws FailedOperationException
     */
    public static void copyEntries(MetadataDataUnit sourceDataUnit,
            WritableMetadataDataUnit targetDataUnit, RepositoryConnection connection)
            throws DataUnitException, RepositoryException, FailedOperationException {
        final Set<URI> metadatagraphs = sourceDataUnit.getMetadataGraphnames();
        final URI target = targetDataUnit.getMetadataWriteGraphname();
        // Copy data from each source graph to target graph.
        for (URI source : metadatagraphs) {
            final String query = String.format("ADD <%s> TO <%s>", source.stringValue(), target.stringValue());
            try {
                connection.prepareUpdate(QueryLanguage.SPARQL, query).execute();
            } catch (MalformedQueryException ex) {
                throw new RuntimeException("Harcoded query is malformed!" , ex);
            } catch (UpdateExecutionException ex) {
                throw new FailedOperationException("Can't copy metadata.", ex);
            }
        }
    }

}
