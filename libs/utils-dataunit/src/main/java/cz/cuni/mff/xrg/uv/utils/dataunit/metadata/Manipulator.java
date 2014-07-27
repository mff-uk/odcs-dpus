package cz.cuni.mff.xrg.uv.utils.dataunit.metadata;

import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.MetadataDataUnit;
import eu.unifiedviews.dataunit.WritableMetadataDataUnit;
import eu.unifiedviews.helpers.dataunit.dataset.CleverDataset;
import java.util.Set;
import org.openrdf.model.*;
import org.openrdf.query.*;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides easy way how to set/get metadata (predicate/object) for given
 * symbolic name.
 *
 * @author Škoda Petr
 */
public class Manipulator {

    private static final Logger LOG = LoggerFactory.getLogger(Manipulator.class);

    private static final String SYMBOLIC_NAME_BINDING = "symbolicName";

    private static final String PREDICATE_BINDING = "predicate";

    private static final String OBJECT_BINDING = "object";

    private static final String SELECT
            = "SELECT ?" + OBJECT_BINDING + " WHERE { "
            + "?s <" + MetadataDataUnit.PREDICATE_SYMBOLIC_NAME + "> ?" + SYMBOLIC_NAME_BINDING + ";"
            + "?" + PREDICATE_BINDING + " ?" + OBJECT_BINDING + ". "
            + "}";

    private static final String UPDATE
            = "DELETE {?s ?" + PREDICATE_BINDING + " ?o} "
            + "INSERT {?s ?" + PREDICATE_BINDING + " ?" + OBJECT_BINDING + "} "
            + "WHERE { "
            + "?s <" + MetadataDataUnit.PREDICATE_SYMBOLIC_NAME + "> ?" + SYMBOLIC_NAME_BINDING + ". "
            + "OPTIONAL {?s ?" + PREDICATE_BINDING + " ?o} "
            + " } ";

    private static final String INSERT
            = "INSERT {?s ?" + PREDICATE_BINDING + " ?" + OBJECT_BINDING + "} "
            + "WHERE { "
            + "?s <" + MetadataDataUnit.PREDICATE_SYMBOLIC_NAME + "> ?" + SYMBOLIC_NAME_BINDING + ". "
            + " } ";

    private Manipulator() {

    }

    public static String get(MetadataDataUnit dataUnit, String symbolicName,
            String predicate) throws DataUnitException {
        final String result;
        RepositoryConnection connection = null;
        try {
            connection = dataUnit.getConnection();
            // execute query
            result = get(connection, dataUnit.getMetadataGraphnames(),
                    symbolicName, predicate);
        } finally {
            if (connection != null) {
                try {
                    connection.close();;
                } catch (RepositoryException ex) {
                    LOG.warn("Error in close.", ex);
                }
            }
        }
        return result;
    }

    private static String get(RepositoryConnection connection, Set<URI> uris,
            String symbolicName, String predicate) throws DataUnitException {
        try {
            final ValueFactory valueFactory = connection.getValueFactory();
            final TupleQuery tupleQuery
                    = connection.prepareTupleQuery(QueryLanguage.SPARQL, SELECT);
            tupleQuery.setBinding(SYMBOLIC_NAME_BINDING,
                    valueFactory.createLiteral(symbolicName));
            tupleQuery.setBinding(PREDICATE_BINDING,
                    valueFactory.createURI(predicate));

            final CleverDataset dataset = new CleverDataset();
            dataset.addDefaultGraphs(uris);
            tupleQuery.setDataset(dataset);

            final TupleQueryResult result = tupleQuery.evaluate();
            if (result.hasNext()) {
                return result.next().getBinding(OBJECT_BINDING).getValue()
                        .stringValue();
            }
        } catch (MalformedQueryException | QueryEvaluationException | RepositoryException ex) {
            throw new DataUnitException("Failed to execute get-query.", ex);
        }
        return null;
    }

    /**
     * Set metadata under given predicate If the predicate is already set then
     * is replaced. To add multiple metadata under same predicate use
     * {@link #add(eu.unifiedviews.dataunit.WritableMetadataDataUnit, java.lang.String, java.lang.String, java.lang.String)}.
     *
     * @param dataUnit
     * @param symbolicName
     * @param predicate
     * @param object
     * @throws DataUnitException
     */
    public static void set(WritableMetadataDataUnit dataUnit,
            String symbolicName,
            String predicate, String object) throws DataUnitException {
        RepositoryConnection connection = null;
        try {
            connection = dataUnit.getConnection();
            // execute query

// TODO use some method to get writable graph
            final URI writeGraph = dataUnit.getMetadataGraphnames().iterator()
                    .next();
            set(connection, writeGraph, symbolicName,
                    predicate, object);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (RepositoryException ex) {
                    LOG.warn("Error in close.", ex);
                }
            }
        }
    }

    private static void set(RepositoryConnection connection, URI uri,
            String symbolicName, String predicate, String object) throws DataUnitException {
        try {
            final ValueFactory valueFactory = connection.getValueFactory();
            final Update update
                    = connection.prepareUpdate(QueryLanguage.SPARQL, UPDATE);
            update.setBinding(SYMBOLIC_NAME_BINDING,
                    valueFactory.createLiteral(symbolicName));
            update.setBinding(PREDICATE_BINDING,
                    valueFactory.createURI(predicate));
            update.setBinding(OBJECT_BINDING,
                    valueFactory.createLiteral(object));

            DatasetImpl dataset = new DatasetImpl();
            dataset.setDefaultInsertGraph(uri);
            dataset.addDefaultRemoveGraph(uri);
            update.setDataset(dataset);

            update.execute();

        } catch (MalformedQueryException | RepositoryException | UpdateExecutionException ex) {
            throw new DataUnitException("Failed to execute update.", ex);
        }
    }

    /**
     * Add metadata for given symbolic name. The old data under same predicate
     * are not deleted. Use to add multiple metadata of same meaning.
     *
     * @param dataUnit
     * @param symbolicName
     * @param predicate
     * @param object
     * @throws DataUnitException
     */
    public static void add(WritableMetadataDataUnit dataUnit,
            String symbolicName,
            String predicate, String object) throws DataUnitException {
        RepositoryConnection connection = null;
        try {
            connection = dataUnit.getConnection();
            // execute query

// TODO use some method to get writable graph
            final URI writeGraph = dataUnit.getMetadataGraphnames().iterator()
                    .next();
            add(connection, writeGraph, symbolicName,
                    predicate, object);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (RepositoryException ex) {
                    LOG.warn("Error in close.", ex);
                }
            }
        }
    }

    private static void add(RepositoryConnection connection, URI uri,
            String symbolicName, String predicate, String object) throws DataUnitException {
        try {
            final ValueFactory valueFactory = connection.getValueFactory();
            final Update update
                    = connection.prepareUpdate(QueryLanguage.SPARQL, INSERT);
            update.setBinding(SYMBOLIC_NAME_BINDING,
                    valueFactory.createLiteral(symbolicName));
            update.setBinding(PREDICATE_BINDING,
                    valueFactory.createURI(predicate));
            update.setBinding(OBJECT_BINDING,
                    valueFactory.createLiteral(object));

            DatasetImpl dataset = new DatasetImpl();
            dataset.setDefaultInsertGraph(uri);
            dataset.addDefaultRemoveGraph(uri);
            update.setDataset(dataset);

            update.execute();

        } catch (MalformedQueryException | RepositoryException | UpdateExecutionException ex) {
            throw new DataUnitException("Failed to execute update.", ex);
        }
    }    
    
    /**
     * Dump content of metadata graphs into logs.
     *
     * @param dataUnit
     * @throws DataUnitException
     */
    public static void dump(MetadataDataUnit dataUnit) throws DataUnitException {
        RepositoryConnection connection = null;
        try {
            connection = dataUnit.getConnection();
            dump(connection, dataUnit.getMetadataGraphnames());
        } finally {
            if (connection != null) {
                try {
                    connection.close();;
                } catch (RepositoryException ex) {
                    LOG.warn("Error in close.", ex);
                }
            }
        }
    }

    /**
     * For debug purpose.
     *
     * @param connection
     * @param uris
     * @throws DataUnitException
     */
    static void dump(RepositoryConnection connection, Set<URI> uris)
            throws DataUnitException {
        final StringBuilder message = new StringBuilder();
        message.append("\n\tGraphs: ");
        for (URI uri : uris) {
            message.append(uri.stringValue());
            message.append(" ");
        }
        message.append("\n");
        try {
            RepositoryResult r = connection.getStatements(null, null, null,
                    true, uris.toArray(new URI[0]));
            while (r.hasNext()) {
                Statement s = (Statement) r.next();

                message.append("'");
                message.append(s.getSubject().stringValue());
                message.append("' <");
                message.append(s.getPredicate().stringValue());
                message.append("> '");
                message.append(s.getObject().stringValue());
                message.append("'\n");
            }
            message.append("------------");
            LOG.debug("{}", message.toString());
        } catch (RepositoryException ex) {
            throw new DataUnitException("Dump failed.", ex);
        }
    }

}
