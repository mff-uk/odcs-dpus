package cz.cuni.mff.xrg.uv.rdf.simple;

import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dpu.DPUContext;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openrdf.model.*;
import org.openrdf.query.*;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

/**
 * Wrap for {@link RDFDataUnit} aims to provide more user friendly way how to
 * handler RDF functionality and also reduce code duplicity.
 *
 * @author Škoda Petr
 */
class SimpleRdfReadImpl implements SimpleRdfRead {

    /**
     * Wrapped data unit.
     */
    protected RDFDataUnit dataUnit;

    /**
     * Execution context.
     */
    protected DPUContext context;

    /**
     * Value factory.
     */
    protected ValueFactory valueFactory = null;

    /**
     * Set of currently used graphs.
     */
    protected final Set<URI> readSetCurrent;

    /**
     *
     * @param dataUnit
     * @param context
     * @throws cz.cuni.mff.xrg.uv.rdf.simple.OperationFailedException
     */
    SimpleRdfReadImpl(RDFDataUnit dataUnit, DPUContext context) throws OperationFailedException {
        this.dataUnit = dataUnit;
        this.context = context;
        this.readSetCurrent = new HashSet<>();
        // set read contexts
        setCurrentReadSetToAll();
    }

    /**
     * In case of multiple calls the dame {@link ValueFactory} will be returned.
     *
     * @return Value factory for wrapped {@link RDFDataUnit}
     * @throws OperationFailedException
     */
    @Override
    public ValueFactory getValueFactory() throws OperationFailedException {
        if (valueFactory == null) {
            try (ClosableConnection conn = new ClosableConnection(dataUnit)) {
                valueFactory = conn.c().getValueFactory();
            }
        }
        return valueFactory;
    }

    /**
     * Eagerly load all triples and store them into list.
     *
     * @return List of all triples in the repository.
     * @throws OperationFailedException
     */
    @Override
    public List<Statement> getStatements() throws OperationFailedException {
        List<Statement> statemens = new ArrayList<>();
        try (ClosableConnection conn = new ClosableConnection(dataUnit)) {
            RepositoryResult<Statement> repoResult
                    = conn.c().getStatements(null, null, null, true,
                            readSetCurrent.toArray(new URI[0]));
            // add all data into list
            while (repoResult.hasNext()) {
                Statement next = repoResult.next();
                statemens.add(next);
            }
            return statemens;
        } catch (RepositoryException ex) {
            throw new OperationFailedException(
                    "Failed to get statements from repository.", ex);
        }
    }

    /**
     * Execute given select query and return result. See {@link ConnectionPair}
     * for more information about usage.
     *
     * @param query SPARQL select query
     * @return
     * @throws OperationFailedException
     */
    @Override
    public ConnectionPair<TupleQueryResult> executeSelectQuery(String query)
            throws OperationFailedException {
        // the connction needs to stay open during the whole query
        ClosableConnection conn = new ClosableConnection(dataUnit);
        try {
            // prepare query
            TupleQuery tupleQuery = conn.c().prepareTupleQuery(
                    QueryLanguage.SPARQL, query);
            // prepare dataset
            tupleQuery.setDataset(prepareReadDataSet(readSetCurrent));
            // wrap result and return
            return new ConnectionPair<>(conn.c(), tupleQuery.evaluate());
        } catch (RepositoryException | MalformedQueryException | QueryEvaluationException e) {
            conn.close();
            throw new OperationFailedException("Failed to execute select query.",
                    e);
        }
    }

    /**
     * Execute given construct query and return result. See
     * {@link ConnectionPair} for more information about usage.
     *
     * @param query SPARQL construct query
     * @return
     * @throws OperationFailedException
     */
    @Override
    public ConnectionPair<Graph> executeConstructQuery(String query) throws OperationFailedException {
        // the connction needs to stay open during the whole query
        ClosableConnection conn = new ClosableConnection(dataUnit);
        try {
            // prepare query
            GraphQuery graphQuery = conn.c().prepareGraphQuery(
                    QueryLanguage.SPARQL,
                    query);
            graphQuery.setDataset(prepareReadDataSet(readSetCurrent));
            // evaluate
            GraphQueryResult result = graphQuery.evaluate();
            // convert into graph
            Model resultGraph = QueryResults.asModel(result);
            // wrap result and return
            return new ConnectionPair<>(conn.c(), (Graph) resultGraph);
        } catch (RepositoryException | MalformedQueryException | QueryEvaluationException e) {
            conn.close();
            throw new OperationFailedException(
                    "Failed to execute construct query.", e);
        }
    }

    /**
     * Set {@link #readSetCurrent} to all graphs from {@link #dataUnit}.
     *
     * @throws OperationFailedException
     */
    private void setCurrentReadSetToAll() throws OperationFailedException {
        readSetCurrent.clear();

        try {
            final RDFDataUnit.Iteration iter = dataUnit.getIteration();
            while (iter.hasNext()) {
                readSetCurrent.add(iter.next().getDataGraphURI());
            }
        } catch (DataUnitException ex) {
            throw new OperationFailedException(
                    "Failed to get list of data graph names.", ex);
        }
    }

    /**
     * Prepare dataset with all given URIs as default graphs.
     * @param graphs
     * @return
     */
    private Dataset prepareReadDataSet(Set<URI> graphs) {
        final DatasetImpl dataset = new DatasetImpl();
        for (URI uri : graphs) {
            dataset.addDefaultGraph(uri);
        }
        return dataset;
    }

}
