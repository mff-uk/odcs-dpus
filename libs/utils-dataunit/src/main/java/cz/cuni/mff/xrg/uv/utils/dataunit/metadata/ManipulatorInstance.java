package cz.cuni.mff.xrg.uv.utils.dataunit.metadata;

import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.MetadataDataUnit;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.*;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Can read metadata.
 *
 * @author Škoda Petr
 */
public class ManipulatorInstance implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(ManipulatorInstance.class);

    protected static final String SYMBOLIC_NAME_BINDING = "symbolicName";

    protected static final String PREDICATE_BINDING = "predicate";

    protected static final String OBJECT_BINDING = "object";

    private static final String SELECT_QUERY
            = "SELECT ?" + OBJECT_BINDING + " WHERE { "
            + "?s <" + MetadataDataUnit.PREDICATE_SYMBOLIC_NAME + "> ?" + SYMBOLIC_NAME_BINDING + ";"
            + "?" + PREDICATE_BINDING + " ?" + OBJECT_BINDING + ". "
            + "}";

    /**
     * Used repository connection.
     */
    protected final RepositoryConnection connection;

    /**
     * Symbolic name of used metadata.
     */
    protected String symbolicName;

    /**
     * If true then given connection is closed when this class is closed.
     */
    protected final boolean closeConnectionOnClose;

    /**
     * Dataset used for queries.
     */
    protected final DatasetImpl dataset;

    /**
     *
     * @param connection
     * @param readGraph
     * @param symbolicName           Symbolic name to which this instance is bound. Can be changed later.
     * @param closeConnectionOnClose If true then given connection is close one this instance is closed.
     * @throws DataUnitException
     */
    ManipulatorInstance(RepositoryConnection connection, Set<URI> readGraph, String symbolicName,
            boolean closeConnectionOnClose) throws DataUnitException {
        this.connection = connection;
        this.symbolicName = symbolicName;
        this.closeConnectionOnClose = closeConnectionOnClose;
        this.dataset = new DatasetImpl();
        // Add read graphs.
        for (URI uri : readGraph) {
            this.dataset.addDefaultGraph(uri);
        }
    }

    /**
     * Get a strings stored under given predicate. For metadata under current {@link #symbolicName}.
     *
     * If more strings are stored under given predicate then one of them is returned.
     *
     * @param predicate Must be valid URI in string form.
     * @return
     * @throws DataUnitException
     */
    public String getFirst(String predicate) throws DataUnitException {
        try {
            final ValueFactory valueFactory = connection.getValueFactory();
            final TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, SELECT_QUERY);
            tupleQuery.setBinding(SYMBOLIC_NAME_BINDING, valueFactory.createLiteral(symbolicName));
            tupleQuery.setBinding(PREDICATE_BINDING, valueFactory.createURI(predicate));
            tupleQuery.setDataset(dataset);
            // Return first result.
            final TupleQueryResult result = tupleQuery.evaluate();
            if (result.hasNext()) {
                return result.next().getBinding(OBJECT_BINDING).getValue().stringValue();
            }
            return null;
        } catch (MalformedQueryException | QueryEvaluationException | RepositoryException ex) {
            throw new DataUnitException("Failed to execute get-query.", ex);
        }
    }

    /**
     * Get all strings stored under given predicate. For metadata under current {@link #symbolicName}.
     *
     * @param predicate Must be valid URI in string form.
     * @return
     * @throws DataUnitException
     */
    public List<String> getAll(String predicate) throws DataUnitException {
        try {
            final ValueFactory valueFactory = connection.getValueFactory();
            final TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, SELECT_QUERY);
            tupleQuery.setBinding(SYMBOLIC_NAME_BINDING, valueFactory.createLiteral(symbolicName));
            tupleQuery.setBinding(PREDICATE_BINDING, valueFactory.createURI(predicate));
            tupleQuery.setDataset(dataset);
            // Store all the results into list.
            final TupleQueryResult result = tupleQuery.evaluate();
            final List<String> resultList = new LinkedList<>();
            while (result.hasNext()) {
                final String value = result.next().getBinding(OBJECT_BINDING).getValue().stringValue();
                resultList.add(value);
            }
            return resultList;
        } catch (MalformedQueryException | QueryEvaluationException | RepositoryException ex) {
            throw new DataUnitException("Failed to execute get-query.", ex);
        }
    }

    /**
     * Change used symbolic name. By this operation {@link ManipulatorInstance} can be modified to work with
     * other metadata object.
     *
     * @param symbolicName
     */
    public void setSymbolicName(String symbolicName) {
        this.symbolicName = symbolicName;
    }

    @Override
    public void close() throws DataUnitException {
        if (closeConnectionOnClose) {
            try {
                connection.close();
            } catch (RepositoryException ex) {
                LOG.warn("Connection.close failed.", ex);
            }
        }
    }

}
