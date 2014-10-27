package cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple;

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
 * Wrap for {@link RDFDataUnit} aims to provide more user friendly way how to handler RDF functionality and
 * also reduce code duplicity.
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
        // Set read contexts.
        setCurrentReadSetToAll();
    }

    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
     */
    @Override
    public List<Statement> getStatements() throws OperationFailedException {
        final List<Statement> statemens = new ArrayList<>();
        try (ClosableConnection conn = new ClosableConnection(dataUnit)) {
            final RepositoryResult<Statement> repoResult = conn.c().getStatements(null, null, null, true,
                    readSetCurrent.toArray(new URI[0]));
            // Add all data into list.
            while (repoResult.hasNext()) {
                Statement next = repoResult.next();
                statemens.add(next);
            }
            return statemens;
        } catch (RepositoryException ex) {
            throw new OperationFailedException("Failed to get statements from repository.", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConnectionPair<TupleQueryResult> executeSelectQuery(String query)
            throws OperationFailedException {
        // The connction needs to stay open during the whole query.
        final ClosableConnection conn = new ClosableConnection(dataUnit);
        try {
            // Prepare query
            TupleQuery tupleQuery = conn.c().prepareTupleQuery(QueryLanguage.SPARQL, query);
            // Prepare dataset.
            tupleQuery.setDataset(prepareReadDataSet(readSetCurrent));
            // Wrap result and return.
            return new ConnectionPair<>(conn.c(), tupleQuery.evaluate());
        } catch (RepositoryException | MalformedQueryException | QueryEvaluationException ex) {
            conn.close();
            throw new OperationFailedException("Failed to execute select query.", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConnectionPair<Graph> executeConstructQuery(String query) throws OperationFailedException {
        // The connction needs to stay open during the whole query.
        final ClosableConnection conn = new ClosableConnection(dataUnit);
        try {
            // Prepare query.
            final GraphQuery graphQuery = conn.c().prepareGraphQuery(QueryLanguage.SPARQL, query);
            graphQuery.setDataset(prepareReadDataSet(readSetCurrent));
            // Evaluate query.
            final GraphQueryResult result = graphQuery.evaluate();
            // Convert into model - this load the result into memory.
            // TODO It can be better to use custom list as it may provide better performance in some cases.
            final Model resultGraph = QueryResults.asModel(result);
            // Wrap result and return.
            return new ConnectionPair<>(conn.c(), (Graph) resultGraph);
        } catch (RepositoryException | MalformedQueryException | QueryEvaluationException ex) {
            conn.close();
            throw new OperationFailedException("Failed to execute construct query.", ex);
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
            throw new OperationFailedException("Failed to get list of data graph names.", ex);
        }
    }

    /**
     * Prepare dataset with all given URIs as default graphs.
     *
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
