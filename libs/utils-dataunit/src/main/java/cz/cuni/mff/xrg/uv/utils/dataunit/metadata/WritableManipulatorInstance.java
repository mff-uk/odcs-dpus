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
     * %s stand for write graph name.
     */
    private static final String UPDATE_QUERY = "WITH <%s> \n"
            + "DELETE { ?entry ?" + PREDICATE_BINDING + " ?value }\n"
            + "INSERT { ?entry ?" + PREDICATE_BINDING + " ?" + OBJECT_BINDING + " }\n"
            + "WHERE { "
            + "?entry <" + MetadataDataUnit.PREDICATE_SYMBOLIC_NAME + "> ?" + SYMBOLIC_NAME_BINDING + " ; "
            + "?" + PREDICATE_BINDING + " ?value . "
            + "}";


    /**
     * %s stand for write graph name.
     */
    private static final String INSERT_QUERY = "WITH <%s>\n"
            + "INSERT { ?entry ?" + PREDICATE_BINDING + " ?" + OBJECT_BINDING + " }\n"
            + "WHERE { "
            + "?entry <" + MetadataDataUnit.PREDICATE_SYMBOLIC_NAME + "> ?" + SYMBOLIC_NAME_BINDING + " . "
            + " } ";

    /**
     * Update query with proper graph.
     */
    private final String updateWithGraph;
    
    /**
     * Insert query with proper graph.
     */
    private final String insertWithGraph;

    WritableManipulatorInstance(RepositoryConnection connection, Set<URI> readGraphs, URI writeGraph,
            String symbolicName, boolean closeConnectionOnClose) throws DataUnitException {
        super(connection, readGraphs, symbolicName, closeConnectionOnClose);
        this.updateWithGraph = String.format(UPDATE_QUERY, writeGraph.stringValue());
        this.insertWithGraph = String.format(INSERT_QUERY, writeGraph.stringValue());
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
            final ValueFactory valueFactory = connection.getValueFactory();
            final Update update = connection.prepareUpdate(QueryLanguage.SPARQL, insertWithGraph);
            update.setBinding(SYMBOLIC_NAME_BINDING, valueFactory.createLiteral(symbolicName));
            update.setBinding(PREDICATE_BINDING, valueFactory.createURI(predicate));
            update.setBinding(OBJECT_BINDING, valueFactory.createLiteral(value));
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
            final ValueFactory valueFactory = connection.getValueFactory();
            final Update update = connection.prepareUpdate(QueryLanguage.SPARQL, updateWithGraph);
            update.setBinding(SYMBOLIC_NAME_BINDING, valueFactory.createLiteral(symbolicName));
            update.setBinding(PREDICATE_BINDING, valueFactory.createURI(predicate));
            update.setBinding(OBJECT_BINDING, valueFactory.createLiteral(value));
            update.execute();
        } catch (MalformedQueryException | RepositoryException | UpdateExecutionException ex) {
            throw new DataUnitException("Failed to execute update.", ex);
        }
        return this;
    }

}
