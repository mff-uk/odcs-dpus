package cz.cuni.mff.xrg.uv.transformer.sparql.update;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.openrdf.model.URI;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.advanced.DpuAdvancedBase;
import cz.cuni.mff.xrg.uv.boost.dpu.config.MasterConfigObject;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.uv.boost.dpu.utils.SendMessage;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;

@DPU.AsTransformer
public class SparqlUpdate extends DpuAdvancedBase<SparqlUpdateConfig_V1> {

	private static final Logger LOG = LoggerFactory.getLogger(SparqlUpdate.class);

    private static final int MAX_GRAPH_COUNT = 1000;

    /**
     * Query used to copy all data from input to output graph.
     *
     * We could use ADD here, but in this way copy query is executed by the same procedure as a user query.
     */
    private static final String QUERY_COPY = "INSERT {?s ?p ?o} WHERE {?s ?p ?o}";

    @DataUnit.AsInput(name = "input")
    public RDFDataUnit rdfInput;

    @DataUnit.AsOutput(name = "output")
    public WritableRDFDataUnit rdfOutput;

	public SparqlUpdate() {
		super(SparqlUpdateConfig_V1.class, AddonInitializer.noAddons());
	}
		
    @Override
    protected void innerExecute() throws DPUException {
        // Get update query.
        String query = config.getQuery();
        if (query == null || query.isEmpty()) {
            throw new DPUException("Query string is null or empty");
        }
        // Get graphs.
        final List<RDFDataUnit.Entry> sourceEntries = getInputEntries(rdfInput);
        // Copy data into a new graph.
        if (config.isPerGraph()) {
            SendMessage.sendInfo(context, "Per-graph query execution", "Number of graphs: %d",
                    sourceEntries.size());
            // Execute on per-graph basis.
            int counter = 1;
            for (RDFDataUnit.Entry sourceEntry : sourceEntries) {
                LOG.info("Executing query for graph ({}/{}): {}", counter++, sourceEntries.size(),
                        sourceEntry);
                // Get input symbolic name.
                final String outputSymbolicName;
                try {
                    outputSymbolicName = sourceEntry.getSymbolicName();
                } catch (DataUnitException ex) {
                    throw new DPUException("Can't get input resource's symbolic mame.", ex);
                }
                // prepare output graph.
                final URI targetGraph = createOutputGraph(outputSymbolicName);
                // Execute query - ie. open connection.
                executeUpdateQuery(query, Arrays.asList(sourceEntry), targetGraph);
            }

        } else {
            // All graph at once, just check size.
            if (sourceEntries.size() > MAX_GRAPH_COUNT) {
                SendMessage.sendError(context, "Too many graphs...",
                        "Maximum graph limit exceeded. Number of graphs %d max. %d",
                        sourceEntries.size(), MAX_GRAPH_COUNT);
                return;
            }
            // Get single output.
            final URI targetGraph = createOutputGraph();
            // Execute over all intpu graph ie. m -> 1
            SendMessage.sendInfo(context, "Executing user query with single output.", "");
            executeUpdateQuery(query, sourceEntries, targetGraph);
        }
    }

    @Override
    public AbstractConfigDialog<MasterConfigObject> getConfigurationDialog() {
        return new SparqlUpdateVaadinDialog();
    }

    /**
     * Get connection and use it to execute given query. Based on user option the query is executed
     * over one or over updateQuery graphs.
     *
     * @param updateQuery
     * @param sourceEntries
     * @param targetgraph
     * @throws DPUException
     */
    protected void executeUpdateQuery(String updateQuery, List<RDFDataUnit.Entry> sourceEntries,
            URI targetgraph) throws DPUException {
        // Get connection.
        RepositoryConnection connection = null;
        try {
            connection = rdfInput.getConnection();
            // Copy data.
            executeUpdateQuery(QUERY_COPY, sourceEntries, targetgraph, connection);
            // Execute user query.
            executeUpdateQuery(updateQuery, sourceEntries, targetgraph, connection);
        } catch (DataUnitException ex) {
            throw new DPUException("Can't get connection.", ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (RepositoryException closeEx) {
                LOG.error("Can't close connection.", closeEx);
            }
        }
    }

    /**
     * Execute given query.
     *
     * @param query
     * @param sourceEntries USING graphs.
     * @param targetGraph WITH graphs.
     * @param connection
     * @throws eu.unifiedviews.dpu.DPUException
     */
    protected void executeUpdateQuery(String query, List<RDFDataUnit.Entry> sourceEntries, URI targetGraph,
            RepositoryConnection connection) throws DPUException {
        // Prepare query.
        if (Pattern.compile(Pattern.quote("DELETE"), Pattern.CASE_INSENSITIVE).matcher(query).find()) {
            query = query.replaceFirst("(?i)DELETE", prepareWithClause(targetGraph) + " DELETE");
        } else {
            query = query.replaceFirst("(?i)INSERT", prepareWithClause(targetGraph) + " INSERT");
        }
        query = query.replaceFirst("(?i)WHERE", prepareUsingClause(sourceEntries) + "WHERE");
        LOG.debug("Query to execute: {}", query);
        try {
            // Execute query.
            connection.prepareUpdate(QueryLanguage.SPARQL, query).execute();
        } catch (MalformedQueryException | UpdateExecutionException ex) {
            throw new DPUException("Problem with query", ex);
        } catch (RepositoryException ex) {
            throw new DPUException("Problem with repository.", ex);
        }
    }

    /**
     *
     * @return New output graph.
     * @throws DPUException
     */
    protected URI createOutputGraph() throws DPUException {
        // Register new output graph
        final String symbolicName = "http://unifiedviews.eu/resource/sparql-construct/"
                + Long.toString((new Date()).getTime());
        try {
            return rdfOutput.addNewDataGraph(symbolicName);
        } catch (DataUnitException ex) {
            throw new DPUException("DPU failed to add a new graph.", ex);
        }
    }

    /**
     * 
     * @param symbolicName
     * @return New output graph.
     * @throws DPUException
     */
    protected URI createOutputGraph(String symbolicName) throws DPUException {
        try {
            return rdfOutput.addNewDataGraph(symbolicName);
        } catch (DataUnitException ex) {
            throw new DPUException("DPU failed to add a new graph.", ex);
        }
    }

    /**
     * Register new output graph and return WITH clause for SPARQL insert.
     *
     * @param graph
     * @return
     */
    protected String prepareWithClause(URI graph) {
        final StringBuilder withClause = new StringBuilder();
        withClause.append("WITH <");
        withClause.append(graph.stringValue());
        withClause.append("> \n");
        return withClause.toString();
    }

    /**
     *
     * @param entries List of entries to use.
     * @return Using clause for SPARQL insert, based on input graphs.
     * @throws DPUException
     */
    protected String prepareUsingClause(List<RDFDataUnit.Entry> entries) throws DPUException {
        final StringBuilder usingClause = new StringBuilder();
        for(RDFDataUnit.Entry entry : entries) {
            usingClause.append("USING <");
            try {
                usingClause.append(entry.getDataGraphURI().stringValue());
            } catch (DataUnitException ex) {
                throw new DPUException("Problem with DataUnit.", ex);
            }
            usingClause.append("> \n");
        }
        return usingClause.toString();
    }

    /**
     *
     * @param dataUnit
     * @return Data graphs in given DataUnit.
     * @throws DPUException
     */
    protected List<RDFDataUnit.Entry> getInputEntries(RDFDataUnit dataUnit) throws DPUException {
        final List<RDFDataUnit.Entry> graphList = new LinkedList<>();
        try {
            final RDFDataUnit.Iteration iter = rdfInput.getIteration();
            while (iter.hasNext()) {
                graphList.add(iter.next());
            }
        } catch (DataUnitException ex) {
            throw new DPUException("Problem with data unit dueing using clause generation.", ex);
        }
        return graphList;
    }

}
