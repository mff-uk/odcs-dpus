package cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.sparql;

import java.util.List;

import org.openrdf.query.BooleanQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;

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

        private SparqlQueryObject(String sparqlQuery) {
            this.sparqlQuery = sparqlQuery;
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

        public SparqlUpdateObject(String sparql) {
            super(sparql);
        }

    }

    /**
     * Represent SPARQL ask query with result.
     */
    public static class SparqlAskObject extends SparqlQueryObject {

        public Boolean result = null;

        public SparqlAskObject(String sparql) {
            super(sparql);
        }

        /**
         * @return Null if the ask has not been evaluated yet.
         */
        public Boolean isResult() {
            return result;
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
     * @throws SparqlUtilsException Is thrown in ma number of graph is exceeded.
     * @throws eu.unifiedviews.dataunit.DataUnitException
     */
    public static String prepareClause(String clause, List<RDFDataUnit.Entry> entries)
            throws SparqlUtilsException, DataUnitException {
        if (entries.size() > MAX_GRAPH_COUNT) {
            throw new SparqlUtilsException(String.format("Maximum graph limit exceeded. %d limit: %d",
                    entries.size(), MAX_GRAPH_COUNT));
        }

        final StringBuilder clauseBuilder = new StringBuilder((clause.length() + 30) * entries.size());
        for(RDFDataUnit.Entry entry : entries) {
            clauseBuilder.append(clause);
            clauseBuilder.append(" <");
            clauseBuilder.append(entry.getDataGraphURI().stringValue());
            clauseBuilder.append("> \n");
        }
        return clauseBuilder.toString();
    }

    /**
     *
     * @param query
     * @param sources
     * @param target
     * @return Prepared SPARQL update query.
     * @throws cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.sparql.SparqlUtilsException
     * @throws eu.unifiedviews.dataunit.DataUnitException
     */
    public static SparqlUpdateObject createInsert(String query, List<RDFDataUnit.Entry> sources, RDFDataUnit.Entry target)
            throws SparqlUtilsException, DataUnitException {
        query = query.replaceFirst("(?i)INSERT", prepareClause("WITH", target) + "INSERT");
        query = query.replaceFirst("(?i)WHERE", prepareClause("USING", sources) + "WHERE");
        // Return new object.
        return new SparqlUpdateObject(query);
    }

    /**
     *
     * @param query
     * @param entries
     * @return Prepared SPARQL update query.
     * @throws cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.sparql.SparqlUtilsException
     * @throws eu.unifiedviews.dataunit.DataUnitException
     */
    public static SparqlAskObject createAsk(String query, List<RDFDataUnit.Entry> entries)
            throws SparqlUtilsException, DataUnitException {
        query = query.replaceFirst("(?i)ASK", "ASK " + prepareClause("FROM", entries) + "WHERE ");
        // Return new object.
        return new SparqlAskObject(query);
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
        connection.prepareUpdate(QueryLanguage.SPARQL, updateObject.sparqlQuery).execute();        
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
        askObject.result = query.evaluate();
    }

}
