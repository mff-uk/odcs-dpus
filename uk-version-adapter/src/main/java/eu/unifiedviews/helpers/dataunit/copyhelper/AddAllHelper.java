package eu.unifiedviews.helpers.dataunit.copyhelper;

import org.openrdf.model.URI;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.helpers.dataunit.rdfhelper.RDFHelper;

/**
 * Helper to add all triples from one data unit into destination data unit under the "all" symbolic name.
 * Addition is done using succesive {@code "ADD <> TO <>"} SPARQL update queries on the storage.
 * <p>
 * Example usage:
 * <p><blockquote><pre>
 * AddAllHelper.addAll(inputDataUnit, outputDataUnit);
 * // outputDataUnit now contains all triples from all data graphs from input data unit, stored as one graph with symbolic name "all"
 * </pre></blockquote></p>
 */
public class AddAllHelper {
    private static final Logger LOG = LoggerFactory.getLogger(AddAllHelper.class);

    /**
     * Add all data from source DataUnit into destination DataUnit into one data graph with symbolic name "all".
     * The method does not modify sources.
     *
     * @param source {@link eu.unifiedviews.dataunit.rdf.RDFDataUnit} to add from
     * @param destination {@link RDFDataUnit} to add to
     * @throws eu.unifiedviews.dataunit.DataUnitException
     */
    public static void addAll(RDFDataUnit source, WritableRDFDataUnit destination) throws DataUnitException {
        RepositoryConnection connection = null;
        try {
            connection = destination.getConnection();

            String targetGraphName = destination.addNewDataGraph("all").stringValue();
            for (URI sourceGraph : RDFHelper.getGraphsURISet(source)) {
                String sourceGraphName = sourceGraph.stringValue();

                LOG.info("Trying to merge {} triples from <{}> to <{}>.",
                        connection.size(sourceGraph), sourceGraphName,
                        targetGraphName);

                String mergeQuery = String.format("ADD <%s> TO <%s>", sourceGraphName,
                        targetGraphName);

                Update update = connection.prepareUpdate(
                        QueryLanguage.SPARQL, mergeQuery);

                update.execute();

                LOG.info("Merged {} triples from <{}> to <{}>.",
                        connection.size(sourceGraph), sourceGraphName,
                        targetGraphName);
            }
        } catch (MalformedQueryException ex) {
            LOG.error("NOT VALID QUERY: {}", ex);
        } catch (RepositoryException | DataUnitException | UpdateExecutionException ex) {
            LOG.error(ex.getMessage(), ex);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (RepositoryException ex) {
                    LOG.warn("Error when closing connection", ex);
                    // eat close exception, we cannot do anything clever here
                }
            }
        }
    }
}
