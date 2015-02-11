package cz.cuni.mff.xrg.uv.boost.rdf.sparql;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.*;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.uv.boost.dpu.advanced.UserExecContext;
import cz.cuni.mff.xrg.uv.utils.dataunit.rdf.RdfDataUnitUtils;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dpu.DPUException;

import static cz.cuni.mff.xrg.uv.utils.dataunit.metadata.MetadataUtilsInstance.ENV_PROP_VIRTUOSO;

/**
 * Utilities for SPARQL execution.
 *
 * @author Škoda Petr
 */
public class SparqlUtils {

    public static final int MAX_GRAPH_COUNT = 1000;

    private static final Logger LOG = LoggerFactory.getLogger(SparqlUtils.class);

    /**
     * Holds SPARQL query.
     */
    public static class SparqlQueryObject {

        /**
         * Query to execute.
         */
        final String sparqlQuery;

        /**
         * Dataset used for Sesame during queries.
         */
        final Dataset dataset;

        private SparqlQueryObject(String sparqlQuery, Dataset dataset) {
            this.sparqlQuery = sparqlQuery;
            this.dataset = dataset;
        }

        @Override
        public String toString() {
            return sparqlQuery;
        }

    }

    /**
     * Represents SPARQL update query.
     */
    public static class SparqlUpdateObject extends SparqlQueryObject {

        public SparqlUpdateObject(String sparql, Dataset dataset) {
            super(sparql, dataset);
        }

    }

    /**
     * Represent SPARQL ask query with result.
     */
    public static class SparqlAskObject extends SparqlQueryObject {

        public Boolean result = null;

        public SparqlAskObject(String sparql, Dataset dataset) {
            super(sparql, dataset);
        }

        /**
         * @return Null if the ask has not been evaluated yet.
         */
        public Boolean isResult() {
            return result;
        }

    }

    /**
     * Represents SPARQL select query.
     */
    public static class SparqlSelectObject extends SparqlQueryObject {

        public SparqlSelectObject(String sparql, Dataset dataset) {
            super(sparql, dataset);
        }

    }

    /**
     * Represents SPARQL select query.
     */
    public static class SparqlConstructObject extends SparqlQueryObject {

        public SparqlConstructObject(String sparql, Dataset dataset) {
            super(sparql, dataset);
        }

    }

    /**
     * User callback for result iteration.
     */
    public static interface TupleIterator {

        /**
         * Called for every tuple in result.
         *
         * @param binding
         * @throws eu.unifiedviews.dpu.DPUException If thrown the the iteration stops and the the same
         *                                          exception is re-thrown.
         */
        public void next(BindingSet binding) throws DPUException;

    }

    /**
     * User callback for result iteration.
     */
    public static interface StatementIterator {

        /**
         * Called for every tuple in result.
         *
         * @param binding
         * @throws eu.unifiedviews.dpu.DPUException If thrown the the iteration stops and the the same
         *                                          exception is re-thrown.
         */
        public void next(Statement binding) throws DPUException;

    }

    /**
     * Fetch results of query. Before ever use in execute method the {@link #prepare()} method should be
     * called.
     */
    public static class QueryResultCollector implements TupleIterator {

        private final List<Map<String, Value>> results = new LinkedList<>();

        @Override
        public void next(BindingSet binding) throws DPUException {
            Map<String, Value> record = new HashMap<>();
            for (String key : binding.getBindingNames()) {
                record.put(key, binding.getBinding(key).getValue());
            }
            // Add to the result list.
            results.add(record);
        }

        /**
         * Reset current content. Call before every use!
         */
        public void prepare() {
            this.results.clear();
        }

        public List<Map<String, Value>> getResults() {
            return results;
        }

    }

    private SparqlUtils() {

    }

    /**
     *
     * @param clause
     * @param entry
     * @return Witch clause with given graph URI.
     * @throws eu.unifiedviews.dataunit.DataUnitException
     */
    public static String prepareClause(String clause, RDFDataUnit.Entry entry) throws DataUnitException {
        final StringBuilder clauseBuilder = new StringBuilder(clause.length() + 30);
        clauseBuilder.append(clause);
        clauseBuilder.append(" <");
        clauseBuilder.append(entry.getDataGraphURI().stringValue());
        clauseBuilder.append("> \n");
        return clauseBuilder.toString();
    }

    /**
     *
     * @param clause
     * @param entries
     * @return
     * @throws SparqlProblemException                     Is thrown in ma number of graph is exceeded.
     * @throws eu.unifiedviews.dataunit.DataUnitException
     */
    public static String prepareClause(String clause, List<RDFDataUnit.Entry> entries)
            throws SparqlProblemException, DataUnitException {
        if (entries.size() > MAX_GRAPH_COUNT) {
            throw new SparqlProblemException(String.format("Maximum graph limit exceeded. %d limit: %d",
                    entries.size(), MAX_GRAPH_COUNT));
        }

        final StringBuilder clauseBuilder = new StringBuilder((clause.length() + 30) * entries.size());
        for (RDFDataUnit.Entry entry : entries) {
            clauseBuilder.append(clause);
            clauseBuilder.append(" <");
            clauseBuilder.append(entry.getDataGraphURI().stringValue());
            clauseBuilder.append("> \n");
        }
        return clauseBuilder.toString();
    }

    /**
     *
     * @param query   Query must contains INSERT as a first command.
     * @param sources
     * @param target
     * @return Prepared SPARQL update query.
     * @throws cz.cuni.mff.xrg.uv.utils.dataunit.rdf.sparql.SparqlProblemException
     * @throws eu.unifiedviews.dataunit.DataUnitException
     */
    public static SparqlUpdateObject createInsert(String query, List<RDFDataUnit.Entry> sources,
            RDFDataUnit.Entry target) throws SparqlProblemException, DataUnitException {
        if (!useDataset()) {
            query = query.replaceFirst("(?i)INSERT", prepareClause("WITH", target) + "INSERT");
            query = query.replaceFirst("(?i)WHERE", prepareClause("USING", sources) + "WHERE");
        }
        // Return new object.
        return new SparqlUpdateObject(query, prepareDataset(sources, target));
    }

    /**
     *
     * @param query   Query must contains DELETE as a first command, it may optionally contains INSERT.
     * @param sources
     * @param target
     * @return Prepared SPARQL update query.
     * @throws cz.cuni.mff.xrg.uv.utils.dataunit.rdf.sparql.SparqlProblemException
     * @throws eu.unifiedviews.dataunit.DataUnitException
     */
    public static SparqlUpdateObject createDelete(String query, List<RDFDataUnit.Entry> sources,
            RDFDataUnit.Entry target) throws SparqlProblemException, DataUnitException {
        if (!useDataset()) {
            if (query.contains(query))

            query = query.replaceFirst("(?i)DELETE", prepareClause("WITH", target) + "DELETE");
            query = query.replaceFirst("(?i)WHERE", prepareClause("USING", sources) + "WHERE");
        }
        // Return new object.
        return new SparqlUpdateObject(query, prepareDataset(sources, target));
    }

    /**
     *
     * @param query
     * @param sources
     * @return Prepared SPARQL update query.
     * @throws cz.cuni.mff.xrg.uv.utils.dataunit.rdf.sparql.SparqlProblemException
     * @throws eu.unifiedviews.dataunit.DataUnitException
     */
    public static SparqlAskObject createAsk(String query, List<RDFDataUnit.Entry> sources)
            throws SparqlProblemException, DataUnitException {
        if (!useDataset()) {
            query = query.replaceFirst("(?i)ASK", "ASK " + prepareClause("FROM", sources) + "WHERE ");
        }
        // Return new object.
        return new SparqlAskObject(query, prepareDataset(sources, null));
    }

    /**
     *
     * @param query
     * @param entries
     * @return Prepared SPARQL select query object.
     */
    public static SparqlSelectObject createSelect(String query, List<RDFDataUnit.Entry> sources)
            throws SparqlProblemException, DataUnitException {
        if (!useDataset()) {
            query = query.replaceFirst("(?i)WHERE", prepareClause("FROM", sources) + "WHERE ");
        }
        return new SparqlSelectObject(query, prepareDataset(sources, null));
    }

    /**
     * Execute given query.
     *
     * @param connection
     * @param updateObject
     * @throws org.openrdf.repository.RepositoryException
     * @throws org.openrdf.query.MalformedQueryException
     * @throws org.openrdf.query.UpdateExecutionException
     */
    public static void execute(RepositoryConnection connection, SparqlUpdateObject updateObject)
            throws RepositoryException, MalformedQueryException, UpdateExecutionException {
        LOG.debug("Executing update: {}", updateObject.sparqlQuery);
        final Update query = connection.prepareUpdate(QueryLanguage.SPARQL, updateObject.sparqlQuery);
        // Set dataset if available.
        if (updateObject.dataset != null) {
            query.setDataset(updateObject.dataset);
        }
        query.execute();
    }

    /**
     * Execute given query and store the result into query object.
     *
     * @param connection
     * @param askObject
     * @throws RepositoryException
     * @throws MalformedQueryException
     * @throws UpdateExecutionException
     * @throws QueryEvaluationException
     */
    public static void execute(RepositoryConnection connection, SparqlAskObject askObject)
            throws RepositoryException, MalformedQueryException,
            UpdateExecutionException, QueryEvaluationException {
        LOG.debug("Executing ask: {}", askObject.sparqlQuery);
        final BooleanQuery query = connection.prepareBooleanQuery(QueryLanguage.SPARQL, askObject.sparqlQuery);
        // Set dataset if available.
        if (askObject.dataset != null) {
            query.setDataset(askObject.dataset);
        }
        askObject.result = query.evaluate();
    }

    /**
     * Execute given query and call user call-back for each tuple. This function does not provide any fault
     * tolerance, the iteration may fail at any time.
     *
     * Does not close given connection.
     *
     * @param connection
     * @param context
     * @param queryObject
     * @param callback    Must not be null.
     * @throws SparqlProblemException
     * @throws MalformedQueryException
     * @throws DPUException
     */
    public static void execute(RepositoryConnection connection, UserExecContext context,
            SparqlSelectObject queryObject, TupleIterator callback)
            throws SparqlProblemException, MalformedQueryException, DPUException {
        LOG.debug("Executing query: {}", queryObject.sparqlQuery);
        // Prepare query.
        final TupleQuery query;
        try {
            query = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryObject.sparqlQuery);
        } catch (RepositoryException ex) {
            throw new SparqlProblemException("Can't prepare query because of problem with repository.", ex);
        }
        // Set dataset if available.
        if (queryObject.dataset != null) {
            query.setDataset(queryObject.dataset);
        }
        // Execute and iterate over result.
        TupleQueryResult queryResult = null;
        try {
            queryResult = query.evaluate();
            while (queryResult.hasNext() && !context.canceled()) {
                callback.next(queryResult.next());
            }
        } catch (QueryEvaluationException ex) {
            throw new SparqlProblemException("Can't evaluate query.", ex);
        } finally {
            try {
                if (queryResult != null) {
                    queryResult.close();
                }
            } catch (QueryEvaluationException ex) {
                LOG.warn("Can't close query result.", ex);
            }
        }
    }

    /**
     * Execute user given construct.
     *
     * @param connection
     * @param context
     * @param constructObject
     * @param callback        Use null to not iterate over result.
     * @throws cz.cuni.mff.xrg.uv.utils.dataunit.rdf.sparql.SparqlProblemException
     * @throws MalformedQueryException
     */
    public static void execute(RepositoryConnection connection, UserExecContext context,
            SparqlConstructObject constructObject, StatementIterator callback)
            throws SparqlProblemException, MalformedQueryException, DPUException {
        LOG.debug("Executing construct: {}", constructObject.sparqlQuery);
        // Prepare query.
        final GraphQuery query;
        try {
            query = connection.prepareGraphQuery(QueryLanguage.SPARQL, constructObject.sparqlQuery);
        } catch (RepositoryException ex) {
            throw new SparqlProblemException("Can't prepare query because of problem with repository.", ex);
        }
        // Set dataset if available.
        if (constructObject.dataset != null) {
            query.setDataset(constructObject.dataset);
        }
        // Execute and iterate over result.
        GraphQueryResult queryResult = null;
        try {
            queryResult = query.evaluate();
            if (callback != null) {
                while (queryResult.hasNext() && !context.canceled()) {
                    callback.next(queryResult.next());
                }
            }
        } catch (QueryEvaluationException ex) {
            throw new SparqlProblemException("Can't evaluate query.", ex);
        } finally {
            try {
                if (queryResult != null) {
                    queryResult.close();
                }
            } catch (QueryEvaluationException ex) {
                LOG.warn("Can't close query result.", ex);
            }
        }
    }

    /**
     *
     * @return Null if no dataset should be used.
     */
    protected static Dataset prepareDataset(List<RDFDataUnit.Entry> source, RDFDataUnit.Entry target)
            throws DataUnitException {
        final DatasetImpl dataset = new DatasetImpl();
        // Add read graphs.
        for (URI uri : RdfDataUnitUtils.asGraphs(source)) {
            dataset.addDefaultGraph(uri);
        }
        // Add write graph.
        if (target != null) {
            dataset.setDefaultInsertGraph(target.getDataGraphURI());
            dataset.addDefaultRemoveGraph(target.getDataGraphURI());
        }
        return dataset;
    }

    protected static boolean useDataset() {
        return System.getProperty(ENV_PROP_VIRTUOSO) == null;
    }

}
