package eu.unifiedviews.helpers.dataunit.maphelper;

import java.util.LinkedHashMap;
import java.util.Map;

import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.MetadataDataUnit;
import eu.unifiedviews.dataunit.WritableMetadataDataUnit;
import eu.unifiedviews.helpers.dataunit.dataset.DatasetBuilder;

/**
 * Static helper nutshell for {@link MapHelper}
 * <p>
 * The helper can be used in two ways:
 * <ul>
 * <li>static (and ineffective), quick and dirty way {@code MapHelpers.getMap(dataUnit, "symbolicName", "mapName")}.
 * This does the job, but every call opens new connection to the underlying storage and then closes the connection adding a little overhead.</li>
 * <li>dynamic way,
 * <p><blockquote><pre>
 * //first create helper over dataunit
 * MapHelper helper = MapHelpers.create(dataUnit);
 * try {
 *   // use many times (helper holds its connections open)
 *   Map<String, String> map = helper.getMap("symbolicName", "mapName");
 *   helper.putMap("symbolicName", "mapName", map);
 * } finally {
 *   helper.close();
 * }
 * </pre></blockquote></p>
 * </ul>
 */
public class MapHelpers {
    private static final MapHelpers selfie = new MapHelpers();

    private MapHelpers() {

    }

    /**
     * Create read-only {@link MapHelper} using {@link MetadataDataUnit}, method {@link MapHelper#putMap(String, String, Map)} is unsupported in this instance.
     * @param dataUnit data unit to work with
     * @return instance of {@link MapHelper}, don't forget to close it after using it
     */
    public static MapHelper create(MetadataDataUnit dataUnit) {
        return selfie.new MapHelperImpl(dataUnit);
    }

    /**
     * Create read-write{@link MapHelper} using {@link WritableMetadataDataUnit}.
     * @param dataUnit data unit to work with
     * @return instance of {@link MapHelper}, don't forget to close it after using it
     */
    public static MapHelper create(WritableMetadataDataUnit dataUnit) {
        return selfie.new WritableMapHelperImpl(dataUnit);
    }

    /**
     * Just do the job, get map from given symbolicName saved under the mapName (id, key of the map).
     * Opens and closes connection to storage each time it is called.
     *
     * @param dataUnit unit to work with
     * @param symbolicName to which the map is binded
     * @param mapName key of the map
     * @return map
     * @throws DataUnitException
     */
    public static Map<String, String> getMap(MetadataDataUnit dataUnit, String symbolicName, String mapName) throws DataUnitException {
        MapHelper helper = null;
        Map<String, String> result = null;
        try {
            helper = create(dataUnit);
            result = helper.getMap(symbolicName, mapName);
        } finally {
            if (helper != null) {
                helper.close();
            }
        }
        return result;
    }

    /**
     * Just put the map to given symbolicName under the mapName.
     * @param dataUnit unit to work with
     * @param symbolicName to which the map is binded
     * @param mapName key of the map
     * @param map map to save
     * @throws DataUnitException
     */
    public static void putMap(WritableMetadataDataUnit dataUnit, String symbolicName, String mapName, Map<String, String> map) throws DataUnitException {
        MapHelper helper = null;
        try {
            helper = create(dataUnit);
            helper.putMap(symbolicName, mapName, map);
        } finally {
            if (helper != null) {
                helper.close();
            }
        }
    }

    private class MapHelperImpl implements MapHelper {
        private final Logger LOG = LoggerFactory.getLogger(MapHelperImpl.class);

        private MetadataDataUnit dataUnit;

        protected RepositoryConnection connection = null;

        protected static final String SYMBOLIC_NAME_BINDING = "symbolicName";

        protected static final String TITLE_BINDING = "title";

        protected static final String KEY_BINDING = "key";

        protected static final String VALUE_BINDING = "value";

        protected static final String SELECT =
                "SELECT ?key ?value WHERE { "
                        // use symbolic name to pick a node
                        + "?root <" + MetadataDataUnit.PREDICATE_SYMBOLIC_NAME + "> ?" + SYMBOLIC_NAME_BINDING + ". "
                        // select all it's maps
                        + "?root <" + MapHelper.PREDICATE_HAS_MAP + "> ?map. "
                        // select the map with given name
                        + "?map <" + MapHelper.PREDICATE_MAP_TITLE + "> ?" + TITLE_BINDING + ". "
                        // select pair
                        + "?map <" + MapHelper.PREDICATE_MAP_CONTAINS + "> ?entry. "
                        // select key and value
                        + "?entry <" + MapHelper.PREDICATE_MAP_ENTRY_KEY + "> ?" + KEY_BINDING + ". "
                        + "?entry <" + MapHelper.PREDICATE_MAP_ENTRY_VALUE + "> ?" + VALUE_BINDING + ". "
                        + "}";

        public MapHelperImpl(MetadataDataUnit dataUnit) {
            this.dataUnit = dataUnit;
        }

        @Override
        public Map<String, String> getMap(String symbolicName, String mapName) throws DataUnitException {
            if (connection == null) {
                connection = dataUnit.getConnection();
            }
            final Map<String, String> resultMap = new LinkedHashMap<>();
            final ValueFactory valueFactory = connection.getValueFactory();
            final Dataset dataset = new DatasetBuilder().withDefaultGraphs(dataUnit.getMetadataGraphnames()).build();

            TupleQueryResult queryResult = null;
            try {
                final TupleQuery query = connection.prepareTupleQuery(
                        QueryLanguage.SPARQL, SELECT);

                query.setBinding(SYMBOLIC_NAME_BINDING,
                        valueFactory.createLiteral(symbolicName));
                query.setBinding(TITLE_BINDING,
                        valueFactory.createLiteral(mapName));

                query.setDataset(dataset);

                queryResult = query.evaluate();
                while (queryResult.hasNext()) {
                    final BindingSet binding = queryResult.next();

                    resultMap.put(
                            binding.getBinding(KEY_BINDING).getValue().stringValue(),
                            binding.getBinding(VALUE_BINDING).getValue().stringValue());
                }

            } catch (MalformedQueryException | QueryEvaluationException | RepositoryException ex) {
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
            return resultMap;
        }

        @Override
        public void putMap(String symbolicName, String mapName, Map<String, String> map) throws DataUnitException {
            throw new UnsupportedOperationException("Cannot put map into read only dataunit");
        }

        @Override
        public void close() {
            if (connection != null) {
                try {
                    connection.close();
                } catch (RepositoryException ex) {
                    LOG.warn("Error in close.", ex);
                }
            }
        }
    }

    private class WritableMapHelperImpl extends MapHelperImpl {
        private WritableMetadataDataUnit writableDataUnit;

        protected static final String CREATE_MAP =
                "INSERT {"
                        + "?root <" + MapHelper.PREDICATE_HAS_MAP + "> _:map. "
                        + "_:map <" + MapHelper.PREDICATE_MAP_TITLE + "> ?" + TITLE_BINDING + ". "
                        + "} WHERE {"
                        + "?root <" + MetadataDataUnit.PREDICATE_SYMBOLIC_NAME + "> ?" + SYMBOLIC_NAME_BINDING + ". "
                        + "FILTER NOT EXISTS {"
                        + "?root <" + MapHelper.PREDICATE_HAS_MAP + "> ?map. "
                        + "?map <" + MapHelper.PREDICATE_MAP_TITLE + "> ?" + TITLE_BINDING + ". "
                        + "} }";

        protected static final String ADD_ENTRY =
                "INSERT {"
                        + "?map <" + MapHelper.PREDICATE_MAP_CONTAINS + "> _:entry. "
                        + "_:entry <" + MapHelper.PREDICATE_MAP_ENTRY_KEY + "> ?" + KEY_BINDING + ". "
                        + "_:entry <" + MapHelper.PREDICATE_MAP_ENTRY_VALUE + "> ?" + VALUE_BINDING + ". "
                        + "} WHERE { "
                        + "?s <" + MetadataDataUnit.PREDICATE_SYMBOLIC_NAME + "> ?" + SYMBOLIC_NAME_BINDING + ". "
                        + "?s <" + MapHelper.PREDICATE_HAS_MAP + "> ?map. "
                        + "?map <" + MapHelper.PREDICATE_MAP_TITLE + "> ?" + TITLE_BINDING + ". "
                        + "}";

        protected static final String DELETE_ENTRIES =
                "DELETE {"
                        + "?map <" + MapHelper.PREDICATE_MAP_CONTAINS + "> ?entry. "
                        + "?entry <" + MapHelper.PREDICATE_MAP_ENTRY_KEY + "> ?key. "
                        + "?entry <" + MapHelper.PREDICATE_MAP_ENTRY_VALUE + "> ?value. "
                        + "} WHERE { "
                        // get the right hash map
                        + "?s <" + MetadataDataUnit.PREDICATE_SYMBOLIC_NAME + "> ?" + SYMBOLIC_NAME_BINDING + ". "
                        + "?s <" + MapHelper.PREDICATE_HAS_MAP + "> ?map. "
                        + "?map <" + MapHelper.PREDICATE_MAP_TITLE + "> ?" + TITLE_BINDING + ". "
                        // map entries
                        + "?map <" + MapHelper.PREDICATE_MAP_CONTAINS + "> ?entry. "
                        + "?entry <" + MapHelper.PREDICATE_MAP_ENTRY_KEY + "> ?key. "
                        + "?entry <" + MapHelper.PREDICATE_MAP_ENTRY_VALUE + "> ?value. "
                        + "}";

        public WritableMapHelperImpl(WritableMetadataDataUnit dataUnit) {
            super(dataUnit);
            this.writableDataUnit = dataUnit;
        }

        @Override
        public Map<String, String> getMap(String symbolicName, String mapName) throws DataUnitException {
            // This initialization of connection may seem redundant with parent at first sight
            // But if the data unit returns read-only protected connection
            // then there is a difference between calling getConnection on writable data unit - we get read-write connection
            // and calling getConnection on data unit - we get read only connection
            if (connection == null) {
                connection = writableDataUnit.getConnection();
            }
            return super.getMap(symbolicName, mapName);
        }

        @Override
        public void putMap(String symbolicName, String mapName, Map<String, String> map) throws DataUnitException {
            if (map == null) {
                throw new IllegalArgumentException("Map can not be null");
            }
            if (connection == null) {
                connection = writableDataUnit.getConnection();
            }
            final ValueFactory valueFactory = connection.getValueFactory();
            final DatasetImpl dataset = new DatasetImpl();
            dataset.setDefaultInsertGraph(writableDataUnit.getMetadataWriteGraphname());
            dataset.addDefaultRemoveGraph(writableDataUnit.getMetadataWriteGraphname());

            try {
                //
                // create map
                //
                {
                    final Update update = connection.prepareUpdate(QueryLanguage.SPARQL, CREATE_MAP);

                    update.setBinding(SYMBOLIC_NAME_BINDING, valueFactory.createLiteral(symbolicName));
                    update.setBinding(TITLE_BINDING, valueFactory.createLiteral(mapName));

                    update.setDataset(dataset);
                    update.execute();
                }
                //
                // delete entries
                //
                {
                    final Update update = connection.prepareUpdate(QueryLanguage.SPARQL, DELETE_ENTRIES);

                    update.setBinding(SYMBOLIC_NAME_BINDING, valueFactory.createLiteral(symbolicName));
                    update.setBinding(TITLE_BINDING, valueFactory.createLiteral(mapName));

                    update.setDataset(dataset);
                    update.execute();
                }
                //
                // add entries
                //
                {
                    for (String key : map.keySet()) {
                        final Update update = connection.prepareUpdate(QueryLanguage.SPARQL, ADD_ENTRY);

                        update.setBinding(SYMBOLIC_NAME_BINDING, valueFactory.createLiteral(symbolicName));
                        update.setBinding(TITLE_BINDING, valueFactory.createLiteral(mapName));
                        update.setBinding(KEY_BINDING, valueFactory.createLiteral(key));
                        update.setBinding(VALUE_BINDING, valueFactory.createLiteral(map.get(key)));

                        update.setDataset(dataset);
                        update.execute();
                    }
                }
            } catch (MalformedQueryException | RepositoryException | UpdateExecutionException ex) {
                throw new DataUnitException("Failed to add map.", ex);
            }
        }
    }
}
