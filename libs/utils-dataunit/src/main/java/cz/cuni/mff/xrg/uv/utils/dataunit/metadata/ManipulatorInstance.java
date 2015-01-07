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
 * Sample usage:
 * <pre>
 * {@code
 * ManipulatorInstance manipulator = Manipulator.create(filesDataUnit, null);
 * // Read virtual path for
 * manipulator.setEntry(fileEntry).getFirst(VirtualPathHelper.PREDICATE_VIRTUAL_PATH);
 * }
 * </pre>
 *
 * @author Škoda Petr
 * @param <THIS> Type of the Manipulator, used in setEntry as a return type to enable chaining.
 */
public class ManipulatorInstance<THIS extends ManipulatorInstance> implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(ManipulatorInstance.class);

    protected static final String SYMBOLIC_NAME_BINDING = "symbolicName";

    protected static final String PREDICATE_BINDING = "predicate";

    protected static final String OBJECT_BINDING = "object";

    /**
     * %s - place for using clause
     */
    private static final String SELECT_QUERY
            = "SELECT ?" + OBJECT_BINDING + " WHERE %s { "
            + "?s <" + MetadataDataUnit.PREDICATE_SYMBOLIC_NAME + "> ?" + SYMBOLIC_NAME_BINDING + ";"
            + "?" + PREDICATE_BINDING + " ?" + OBJECT_BINDING + ". "
            + "}";

    /**
     * If env. property of this name is set, then dataset is not used.
     */
    private static final String ENV_PROP_VIRTUOSO = "virtuoso_used";

    /**
     * Used repository connection.
     */
    protected RepositoryConnection connection;

    /**
     * Symbolic name of used metadata.
     */
    protected String symbolicName;

    /**
     * If true then given connection is closed when this class is closed.
     */
    protected boolean closeConnectionOnClose;

    /**
     * Dataset used for queries.
     */
    protected final DatasetImpl dataset;

    /**
     * String version of {@link #dataset}.
     */
    protected final String datasetUsingClause;

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
        // Add read graphs.
        if (useDataset()) {
            this.dataset = new DatasetImpl();
            for (URI uri : readGraph) {
                this.dataset.addDefaultGraph(uri);
            }
            this.datasetUsingClause = null;
        } else {
            this.dataset = null;
            // Build USING clause
            final StringBuilder clauseBuilder = new StringBuilder(readGraph.size() * 15);
            for (URI uri : readGraph) {
                clauseBuilder.append("USING <");
                clauseBuilder.append(uri.toString());
                clauseBuilder.append(">\n");
            }
            this.datasetUsingClause = clauseBuilder.toString();
        }
    }

    /**
     * Replace current connection with given one. Close the old connection if needed (if not given by user).
     * Given connection is not closed by the helper.
     *
     * Can be used to replace corrupted connection in {@link ManipulatorInstance} without the need
     * of new instance construction.
     *
     * @param newConnection New connection that should be used, will not be closed by helper.
     */
    public void replaceConnection(RepositoryConnection newConnection) {
        if (closeConnectionOnClose) {
            try {
                this.connection.close();
            } catch (RepositoryException ex) {
                LOG.warn("Can't close old connection.");
            }
        }
        this.connection = newConnection;
        this.closeConnectionOnClose = false;
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
            final TupleQueryResult result = executeSelectQuery(predicate);
            // Return first result.
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
            final TupleQueryResult result = executeSelectQuery(predicate);
            // Dump result list.
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
     * Change used entry(symbolic name). By this operation {@link ManipulatorInstance}
     * can be modified to work with other metadata object.
     *
     * @param symbolicName
     * @return
     */
    public THIS setEntry(String symbolicName) {
        this.symbolicName = symbolicName;
        return (THIS)this;
    }

    /**
     * Change used entry(symbolic name). By this operation {@link ManipulatorInstance} 
     * can be modified to work with other metadata object.
     *
     * @param entry
     * @return
     * @throws eu.unifiedviews.dataunit.DataUnitException
     */
    public THIS setEntry(MetadataDataUnit.Entry entry) throws DataUnitException {
        this.symbolicName = entry.getSymbolicName();
        return (THIS)this;
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

    /**
     *
     * @return True if we should use DataSet class.
     */
    protected final boolean useDataset() {
        return System.getProperty(ENV_PROP_VIRTUOSO) != null;
    }

    /**
     * Execute {@link #SELECT_QUERY} for given predicate.
     *
     * @param predicate
     * @return
     * @throws RepositoryException
     * @throws MalformedQueryException
     * @throws QueryEvaluationException
     */
    private TupleQueryResult executeSelectQuery(String predicate) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        final ValueFactory valueFactory = connection.getValueFactory();
        // Prepare query. Add clause if dataset is not used.
        final String query;
        if (useDataset()) {
            query = String.format(SELECT_QUERY, "");
        } else {
            query = String.format(SELECT_QUERY, datasetUsingClause);
        }            
        final TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);            
        tupleQuery.setBinding(SYMBOLIC_NAME_BINDING, valueFactory.createLiteral(symbolicName));
        tupleQuery.setBinding(PREDICATE_BINDING, valueFactory.createURI(predicate));
        // Use dataset if set.
        if (useDataset()) {
            tupleQuery.setDataset(dataset);
        }
        // Evaluate and return tuple query result.
        return tupleQuery.evaluate();
    }

}
