package cz.cuni.mff.xrg.uv.utils.dataunit.metadata;

import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.MetadataDataUnit;
import eu.unifiedviews.helpers.dataunit.maphelper.MapHelper;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.*;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author Škoda Petr
 */
public class MetadataMap {

    // - - - - -

    private static final String SYMBOLIC_NAME_BINDING = "symbolicName";

    private static final String TITLE_BINDING = "title";

    private static final String KEY_BINDING = "key";

    private static final String VALUE_BINDING = "value";

    private static final String SELECT =
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

    private static final String CREATE_MAP =
            "INSERT {"
            + "?root <" + MapHelper.PREDICATE_HAS_MAP + "> _:map. "
            + "_:map <" + MapHelper.PREDICATE_MAP_TITLE + "> ?" + TITLE_BINDING + ". "
            + "} WHERE {"
            + "?root <" + MetadataDataUnit.PREDICATE_SYMBOLIC_NAME + "> ?" + SYMBOLIC_NAME_BINDING + ". "
            + "FILTER NOT EXISTS {"
            + "?map <" + MapHelper.PREDICATE_MAP_TITLE + "> ?" + TITLE_BINDING + ". "
            + "} }";

    private static final String ADD_ENTRY =
            "INSERT {"
            + "?map <" + MapHelper.PREDICATE_MAP_CONTAINS + "> _:entry. "
            + "_:entry <" + MapHelper.PREDICATE_MAP_ENTRY_KEY + "> ?" + KEY_BINDING + ". "
            + "_:entry <" + MapHelper.PREDICATE_MAP_ENTRY_VALUE + "> ?" + VALUE_BINDING + ". "
            + "} WHERE { "
            + "?s <" + MetadataDataUnit.PREDICATE_SYMBOLIC_NAME + "> ?" + SYMBOLIC_NAME_BINDING + ". "
            + "?s <" + MapHelper.PREDICATE_HAS_MAP + "> ?map. "
            + "?map <" + MapHelper.PREDICATE_MAP_TITLE + "> ?" + TITLE_BINDING + ". "
            + "}";

    private static final String DELETE_ENTRIES =
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

    static Map<String, String> get(RepositoryConnection connection,
            Set<URI> graph, String symbolicName, String name) throws Exception {
        final Map<String, String> resultMap = new HashMap<>();
        final ValueFactory valueFactory = connection.getValueFactory();
        final DatasetImpl dataset = new DatasetImpl();

        try {
            final TupleQuery query = connection.prepareTupleQuery(
                    QueryLanguage.SPARQL, SELECT);

            query.setBinding(SYMBOLIC_NAME_BINDING,
                    valueFactory.createLiteral(symbolicName));
            query.setBinding(TITLE_BINDING,
                    valueFactory.createLiteral(name));

            query.setDataset(dataset);

            final TupleQueryResult resultQuery = query.evaluate();
            while(resultQuery.hasNext()) {
                final BindingSet binding = resultQuery.next();

                resultMap.put(
                        binding.getBinding(KEY_BINDING).getValue().stringValue(),
                        binding.getBinding(VALUE_BINDING).getValue().stringValue());
            }

        } catch (MalformedQueryException | QueryEvaluationException
                | RepositoryException ex) {
            throw new Exception(ex);
        }

        return resultMap;
    }

    static void set(RepositoryConnection connection, URI graph,
            String symbolicName, String name, Map<String, String> data)
            throws DataUnitException {

        final ValueFactory valueFactory = connection.getValueFactory();
        final DatasetImpl dataset = new DatasetImpl();
        dataset.setDefaultInsertGraph(graph);
        dataset.addDefaultRemoveGraph(graph);

        //
        // create map
        //
        try {
            final Update update =
                    connection.prepareUpdate(QueryLanguage.SPARQL, CREATE_MAP);

            update.setBinding(SYMBOLIC_NAME_BINDING,
                    valueFactory.createLiteral(symbolicName));
            update.setBinding(TITLE_BINDING,
                    valueFactory.createLiteral(name));

            update.setDataset(dataset);
            update.execute();

        } catch (MalformedQueryException | RepositoryException |
                UpdateExecutionException ex) {
            throw new DataUnitException("Map creation failed.", ex);
        }
        //
        // delete entries
        //
        try {
            final Update update =
                    connection.prepareUpdate(QueryLanguage.SPARQL, DELETE_ENTRIES);

            update.setBinding(SYMBOLIC_NAME_BINDING,
                    valueFactory.createLiteral(symbolicName));
            update.setBinding(TITLE_BINDING,
                    valueFactory.createLiteral(name));

            update.setDataset(dataset);
            update.execute();

        } catch (MalformedQueryException | RepositoryException |
                UpdateExecutionException ex) {
            throw new DataUnitException("Failed to remove old entries.", ex);
        }

        //
        // add entries
        //
        try {
            for (String key : data.keySet()) {

                final Update update =
                    connection.prepareUpdate(QueryLanguage.SPARQL, ADD_ENTRY);

            update.setBinding(SYMBOLIC_NAME_BINDING,
                    valueFactory.createLiteral(symbolicName));
            update.setBinding(TITLE_BINDING,
                    valueFactory.createLiteral(name));
            update.setBinding(KEY_BINDING,
                    valueFactory.createLiteral(key));
            update.setBinding(VALUE_BINDING,
                    valueFactory.createLiteral(data.get(key)));

            update.setDataset(dataset);
            update.execute();
            }
        } catch (MalformedQueryException | RepositoryException |
                UpdateExecutionException ex) {
            throw new DataUnitException("Failed to add entries.", ex);
        }
    }


}