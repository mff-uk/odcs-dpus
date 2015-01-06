package cz.cuni.mff.xrg.uv.utils.dataunit.metadata;

import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.MetadataDataUnit;
import java.util.Set;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.*;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 * Manipulator for writing metadata.
 *
 * @author Škoda Petr
 */
public class WritableManipulatorInstance extends ManipulatorInstance {

    /**
     * %s - with clause
     * %s - using clause
     */
    private static final String UPDATE_QUERY
            = "%s DELETE {?s ?" + PREDICATE_BINDING + " ?o} "
            + "INSERT {?s ?" + PREDICATE_BINDING + " ?" + OBJECT_BINDING + "} "
            + "%s WHERE { "
            + "?s <" + MetadataDataUnit.PREDICATE_SYMBOLIC_NAME + "> ?" + SYMBOLIC_NAME_BINDING + ". "
            + "OPTIONAL {?s ?" + PREDICATE_BINDING + " ?o} "
            + " } ";

    /**
     * %s - with clause
     * %s - using clause
     */
    private static final String INSERT_QUERY
            = "%s INSERT {?s ?" + PREDICATE_BINDING + " ?" + OBJECT_BINDING + "} "
            + "%s WHERE { "
            + "?s <" + MetadataDataUnit.PREDICATE_SYMBOLIC_NAME + "> ?" + SYMBOLIC_NAME_BINDING + ". "
            + " } ";

    /**
     * String version of {@link #dataset}.
     */
    private final String datasetWithClause;

    WritableManipulatorInstance(RepositoryConnection connection, Set<URI> readGraphs, URI writeGraph,
            String symbolicName, boolean closeConnectionOnClose) throws DataUnitException {
        super(connection, readGraphs, symbolicName, closeConnectionOnClose);
        if (useDataset()) {
            this.dataset.setDefaultInsertGraph(writeGraph);
            this.dataset.addDefaultRemoveGraph(writeGraph);
            datasetWithClause = null;
        } else {
            datasetWithClause = "WITH <" + writeGraph.toString() + ">\n";
        }
    }

    /**
     * Add string (object) and predicate to metadata of current {@link #symbolicName}. If called multiple
     * times with same parameters (or predicate) then triples are only added, never deleted.
     *
     * This function can be used to add multiple informations under a single predicate.
     *
     * Also as this method return 'this' pointer it can be used in chained expressions.
     *
     * @param predicate Must be valid URI in string form.
     * @param value
     * @return
     * @throws DataUnitException
     */
    public WritableManipulatorInstance add(String predicate, String value) throws DataUnitException {
        try {
            final String query;
            if (useDataset()) {
                query = String.format(INSERT_QUERY, "", "");
            } else {
                query = String.format(INSERT_QUERY, datasetWithClause, datasetUsingClause);
            }
            final ValueFactory valueFactory = connection.getValueFactory();
            final Update update = connection.prepareUpdate(QueryLanguage.SPARQL, query);
            update.setBinding(SYMBOLIC_NAME_BINDING, valueFactory.createLiteral(symbolicName));
            update.setBinding(PREDICATE_BINDING, valueFactory.createURI(predicate));
            update.setBinding(OBJECT_BINDING, valueFactory.createLiteral(value));
            if (useDataset()) {
                update.setDataset(dataset);
            }
            update.execute();
        } catch (MalformedQueryException | RepositoryException | UpdateExecutionException ex) {
            throw new DataUnitException("Failed to execute update.", ex);
        }
        return this;
    }

    /**
     * Set string (object) for given predicate. For metadata under current {@link #symbolicName}. All data
     * under the same predicate and for same {@link #symbolicName} are deleted.
     *
     * Use this if you want the given string to be the only object (information) stored under given predicate.
     *
     * Also as this method return 'this' pointer it can be used in chained expressions.
     *
     * @param predicate Must be valid URI in string form.
     * @param value
     * @return
     * @throws DataUnitException
     */
    public WritableManipulatorInstance set(String predicate, String value) throws DataUnitException {
        try {
            final String query;
            if (useDataset()) {
                query = String.format(UPDATE_QUERY, "", "");
            } else {
                query = String.format(UPDATE_QUERY, datasetWithClause, datasetUsingClause);
            }
            final ValueFactory valueFactory = connection.getValueFactory();
            final Update update = connection.prepareUpdate(QueryLanguage.SPARQL, query);
            update.setBinding(SYMBOLIC_NAME_BINDING, valueFactory.createLiteral(symbolicName));
            update.setBinding(PREDICATE_BINDING, valueFactory.createURI(predicate));
            if (useDataset()) {
                update.setDataset(dataset);
            }
            update.execute();
        } catch (MalformedQueryException | RepositoryException | UpdateExecutionException ex) {
            throw new DataUnitException("Failed to execute update.", ex);
        }
        return this;
    }

}
