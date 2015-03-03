package eu.unifiedviews.helpers.dataunit.internal.metadata;

import java.util.Set;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.MetadataDataUnit;
import eu.unifiedviews.dataunit.WritableMetadataDataUnit;

/**
 * Provides easy way how to set/get metadata (predicate/object) for given
 * symbolic name.
 *
 * <strong>This class in not accessible in UV environment, do not use!</strong>
 */
public class MetadataHelpers {

    private static final Logger LOG = LoggerFactory.getLogger(MetadataHelpers.class);

    private static final MetadataHelpers selfie = new MetadataHelpers();

    private MetadataHelpers() {
    }

    public static MetadataHelper create(MetadataDataUnit dataUnit) {
        return selfie.new MetadataHelperImpl(dataUnit);
    }

    public static MetadataHelper create(WritableMetadataDataUnit writableDataUnit) {
        return selfie.new WritableMetadataHelperImpl(writableDataUnit);
    }

    /**
     * Get the metadata under given predicate.
     * If the data is not set, null is returned.
     * @param dataUnit
     * @param symbolicName
     * @param predicate
     * @return the data or null if not set.
     * @throws DataUnitException
     */
    public static String get(MetadataDataUnit dataUnit, String symbolicName, String predicate) throws DataUnitException {
        String result = null;
        MetadataHelper helper = null;
        try {
            helper = create(dataUnit);
            result = helper.get(symbolicName, predicate);
        } finally {
            if (helper != null) {
                try {
                    helper.close();
                } catch (DataUnitException ex) {
                    LOG.warn("Error in close.", ex);
                }
            }
        }
        return result;
    }

    /**
     * Set metadata under given predicate.
     * If the predicate is already set then the value is replaced. To add multiple metadata under the same predicate use
     * {@link #add(eu.unifiedviews.dataunit.WritableMetadataDataUnit, java.lang.String, java.lang.String, java.lang.String)}.
     *
     * @param dataUnit
     * @param symbolicName
     * @param predicate
     * @param newValue
     * @throws DataUnitException
     */
    public static void set(WritableMetadataDataUnit dataUnit, String symbolicName, String predicate, String newValue) throws DataUnitException {
        MetadataHelper helper = null;
        try {
            helper = create(dataUnit);
            helper.set(symbolicName, predicate, newValue);
        } finally {
            if (helper != null) {
                try {
                    helper.close();
                } catch (DataUnitException ex) {
                    LOG.warn("Error in close.", ex);
                }
            }
        }
    }

    /**
     * Add metadata for given symbolic name. The old data under same predicate
     * are not deleted. Use to add multiple metadata of same meaning.
     *
     * @param dataUnit
     * @param symbolicName
     * @param predicate
     * @param newValue
     * @throws DataUnitException
     */
    public static void add(WritableMetadataDataUnit dataUnit, String symbolicName, String predicate, String newValue) throws DataUnitException {
        MetadataHelper helper = null;
        try {
            helper = create(dataUnit);
            helper.add(symbolicName, predicate, newValue);
        } finally {
            if (helper != null) {
                try {
                    helper.close();
                } catch (DataUnitException ex) {
                    LOG.warn("Error in close.", ex);
                }
            }
        }
    }

    /**
     * For debug purpose.
     *
     * @param dataUnit
     * @throws DataUnitException
     */
    public static void dump(MetadataDataUnit dataUnit) throws DataUnitException {
        final StringBuilder message = new StringBuilder();
        message.append("\n\tGraphs: ");
        Set<URI> uris = dataUnit.getMetadataGraphnames();
        for (URI uri : uris) {
            message.append(uri.stringValue());
            message.append(" ");
        }
        message.append("\n");
        RepositoryResult<Statement> r = null;
        RepositoryConnection connection = null;
        try {
            connection = dataUnit.getConnection();
            r = connection.getStatements(null, null, null,
                    true, uris.toArray(new URI[0]));
            while (r.hasNext()) {
                Statement s = r.next();

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
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (RepositoryException ex) {
                    LOG.warn("Error in close", ex);
                }
            }
            if (r != null) {
                try {
                    r.close();
                } catch (RepositoryException ex) {
                    LOG.warn("Error in close.", ex);
                }
            }
        }
    }

    private class MetadataHelperImpl implements MetadataHelper {

        private final Logger LOG = LoggerFactory.getLogger(MetadataHelperImpl.class);

        private final MetadataDataUnit dataUnit;

        protected RepositoryConnection connection = null;

        protected static final String SYMBOLIC_NAME_BINDING = "symbolicName";

        protected static final String PREDICATE_BINDING = "predicate";

        protected static final String OBJECT_BINDING = "object";

        protected static final String SELECT = "SELECT ?" + OBJECT_BINDING + " %s WHERE { "
                + "?s <" + MetadataDataUnit.PREDICATE_SYMBOLIC_NAME + "> ?" + SYMBOLIC_NAME_BINDING + " ;"
                + "?" + PREDICATE_BINDING + " ?" + OBJECT_BINDING + " . "
                + "}";

        public MetadataHelperImpl(MetadataDataUnit dataUnit) {
            this.dataUnit = dataUnit;
        }

        @Override
        public String get(String symbolicName, String predicate) throws DataUnitException {
            if (connection == null) {
                connection = dataUnit.getConnection();
            }
            String methodResult = null;
            TupleQueryResult queryResult = null;
            try {
                // In this case we can select from multiple graphs.
                final StringBuilder fromPart = new StringBuilder();
                for (URI graph : dataUnit.getMetadataGraphnames()) {
                    fromPart.append("FROM <");
                    fromPart.append(graph.stringValue());
                    fromPart.append("> ");
                }

                final String selectQuery = String.format(SELECT, fromPart.toString());
                LOG.debug("get({},{}) ->\n {}", symbolicName, predicate, selectQuery);

                final ValueFactory valueFactory = connection.getValueFactory();
                final TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, selectQuery);
                tupleQuery.setBinding(SYMBOLIC_NAME_BINDING, valueFactory.createLiteral(symbolicName));
                tupleQuery.setBinding(PREDICATE_BINDING, valueFactory.createURI(predicate));
                queryResult = tupleQuery.evaluate();
                if (queryResult.hasNext()) {
                    methodResult = queryResult.next().getBinding(OBJECT_BINDING).getValue()
                            .stringValue();
                }
            } catch (QueryEvaluationException | RepositoryException | MalformedQueryException ex) {
                throw new DataUnitException(ex);
            } finally {
                if (queryResult != null) {
                    try {
                        queryResult.close();
                    } catch (QueryEvaluationException ex) {
                        LOG.warn("Error in close.", ex);
                    }
                }
            }
            return methodResult;
        }

        @Override
        public void set(String symbolicName, String predicate, String newValue) throws DataUnitException {
            throw new UnsupportedOperationException();

        }

        @Override
        public void add(String symbolicName, String predicate, String newValue) throws DataUnitException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void close() throws DataUnitException {
            if (connection != null) {
                try {
                    connection.close();
                } catch (RepositoryException ex) {
                    LOG.warn("Error in close.", ex);
                }
            }
        }

    }

    private class WritableMetadataHelperImpl extends MetadataHelperImpl {

        private final WritableMetadataDataUnit writableDataUnit;

        private static final String INSERT = "WITH <%s>\n"
                + "INSERT { ?entry ?" + PREDICATE_BINDING + " ?" + OBJECT_BINDING + " }\n"
                + "WHERE { "
                + "?entry <" + MetadataDataUnit.PREDICATE_SYMBOLIC_NAME + "> ?" + SYMBOLIC_NAME_BINDING + " . "
                + " } ";

        private static final String UPDATE = "WITH <%s> \n"
                + "DELETE { ?entry ?" + PREDICATE_BINDING + " ?value }\n"
                + "INSERT { ?entry ?" + PREDICATE_BINDING + " ?" + OBJECT_BINDING + " }\n"
                + "WHERE { "
                + "?entry <" + MetadataDataUnit.PREDICATE_SYMBOLIC_NAME + "> ?" + SYMBOLIC_NAME_BINDING + " ; "
                + "?" + PREDICATE_BINDING + " ?value . "
                + "}";

        public WritableMetadataHelperImpl(WritableMetadataDataUnit writableDataUnit) {
            super(writableDataUnit);
            this.writableDataUnit = writableDataUnit;
        }

        @Override
        public String get(String symbolicName, String predicate) throws DataUnitException {
            // This initialization of connection may seem redundant with parent at first sight
            // But if the data unit returns read-only protected connection
            // then there is a difference between calling getConnection on writable data unit - we get read-write connection
            // and calling getConnection on data unit - we get read only connection
            if (connection == null) {
                connection = writableDataUnit.getConnection();
            }
            return super.get(symbolicName, predicate);
        }

        @Override
        public void set(String symbolicName, String predicate, String newValue) throws DataUnitException {
            if (connection == null) {
                connection = writableDataUnit.getConnection();
            }
            final ValueFactory valueFactory = connection.getValueFactory();
            Update update;
            try {
                final String updateSparql = String.format(UPDATE, writableDataUnit.getMetadataWriteGraphname());
                LOG.info("set({}, {}, {}) ->\n {}", symbolicName, predicate, newValue, updateSparql);

                update = connection.prepareUpdate(QueryLanguage.SPARQL, updateSparql);
                update.setBinding(SYMBOLIC_NAME_BINDING, valueFactory.createLiteral(symbolicName));
                update.setBinding(PREDICATE_BINDING, valueFactory.createURI(predicate));
                update.setBinding(OBJECT_BINDING, valueFactory.createLiteral(newValue));
                update.execute();
            } catch (RepositoryException | MalformedQueryException | UpdateExecutionException ex) {
                throw new DataUnitException(ex);
            }
            // We have removed the old value we can insert new.
            add(symbolicName, predicate, newValue);
        }

        @Override
        public void add(String symbolicName, String predicate, String newValue) throws DataUnitException {
            if (connection == null) {
                connection = writableDataUnit.getConnection();
            }
            try {
                final String updateSparql = String.format(INSERT, writableDataUnit.getMetadataWriteGraphname());
                final ValueFactory valueFactory = connection.getValueFactory();
                LOG.info("add({}, {}, {}) ->\n {}", symbolicName, predicate, newValue, updateSparql);

                final Update update = connection.prepareUpdate(QueryLanguage.SPARQL, updateSparql);
                update.setBinding(SYMBOLIC_NAME_BINDING, valueFactory.createLiteral(symbolicName));
                update.setBinding(PREDICATE_BINDING, valueFactory.createURI(predicate));
                update.setBinding(OBJECT_BINDING, valueFactory.createLiteral(newValue));
                update.execute();
            } catch (MalformedQueryException | RepositoryException | UpdateExecutionException ex) {
                throw new DataUnitException("Failed to execute update.", ex);
            }
        }
    }
}
