package cz.cuni.mff.xrg.uv.transformer.sparql.update;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.openrdf.model.URI;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.uv.boost.dpu.advanced.AbstractDpu;
import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigHistory;
import cz.cuni.mff.xrg.uv.boost.dpu.context.ContextUtils;
import cz.cuni.mff.xrg.uv.boost.dpu.initialization.AutoInitializer;
import cz.cuni.mff.xrg.uv.boost.extensions.FaultTolerance;
import cz.cuni.mff.xrg.uv.utils.dataunit.DataUnitUtils;
import cz.cuni.mff.xrg.uv.utils.dataunit.metadata.MetadataUtilsInstance;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPUContext;

/**
 * 
 * @author Škoda Petr
 */
@DPU.AsTransformer
public class SparqlUpdate 
        extends AbstractDpu<SparqlUpdateConfig_V1> {

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

    @AutoInitializer.Init
    public SPARQLConfig_V1_Convertor _sparqlConfigConvertor;

    @AutoInitializer.Init
    public FaultTolerance faultTolerance;

    public SparqlUpdate() {
        super(SparqlUpdateVaadinDialog.class, ConfigHistory.noHistory(SparqlUpdateConfig_V1.class),
                SparqlUpdateOntology.class);
    }

    @Override
    protected void innerExecute() throws DPUException {
        if (useDataset()) {
            ContextUtils.sendShortInfo(ctx, "OpenRdf mode.");
        } else {
            ContextUtils.sendShortInfo(ctx, "Virtuoso mode.");
        }
        // Get update query.
        final String query = config.getQuery();
        if (query == null || query.isEmpty()) {
            throw new DPUException("Query string is null or empty");
        }
        // Get graphs.
        final List<RDFDataUnit.Entry> sourceEntries = getInputEntries(rdfInput);
        // Copy data into a new graph.
        if (config.isPerGraph()) {
            ContextUtils.sendMessage(ctx, DPUContext.MessageType.INFO, "Per-graph query execution",
                    "Number of graphs: %d", sourceEntries.size());
            // Execute on per-graph basis.
            int counter = 1;
            for (final RDFDataUnit.Entry sourceEntry : sourceEntries) {
                LOG.info("Executing {}/{}", counter++, sourceEntries.size());
                // For each input graph prepare output graph.
                final URI targetGraph = faultTolerance.execute(new FaultTolerance.ActionReturn<URI>() {

                    @Override
                    public URI action() throws Exception {
                        final URI outputUri = createOutputGraph(sourceEntry);
                        LOG.info("   {} -> {}", sourceEntry.getDataGraphURI(), outputUri);
                        return outputUri;
                    }

                });
                // Execute query 1 -> 1.
                updateEntries(query, Arrays.asList(sourceEntry), targetGraph);
                if (ctx.canceled()) {
                    ContextUtils.sendMessage(ctx, DPUContext.MessageType.INFO, "DPU cancelled ..", "");
                    // Cancel.
                    break;
                }
            }
        } else {
            // All graph at once, just check size.
            if (sourceEntries.size() > MAX_GRAPH_COUNT) {
                throw new DPUException("Too many graphs .. (limit: " + MAX_GRAPH_COUNT + ", given: "
                        + sourceEntries.size() + ")");
            }
            // Prepare single output graph.
            final URI targetGraph = faultTolerance.execute(new FaultTolerance.ActionReturn<URI>() {

                @Override
                public URI action() throws Exception {
                    return createOutputGraph();
                }
            });
            // Execute over all intpu graph ie. m -> 1
            ContextUtils.sendMessage(ctx, DPUContext.MessageType.INFO,
                    "Executing user query with single output.", "");
            updateEntries(query, sourceEntries, targetGraph);
        }
    }

    /**
     * Get connection and use it to execute given query. Based on user option the query is executed over one
     * or over updateQuery graphs.
     *
     * @param updateQuery
     * @param sourceEntries
     * @param targetgraph
     * @throws DPUException
     */
    protected void updateEntries(final String updateQuery, final List<RDFDataUnit.Entry> sourceEntries,
            final URI targetgraph) throws DPUException {
        faultTolerance.execute(rdfInput, new FaultTolerance.ConnectionAction() {

            @Override
            public void action(RepositoryConnection connection) throws Exception {
                // Copy data.
                executeUpdateQuery(QUERY_COPY, toUriList(sourceEntries), targetgraph, connection);
            }

        });
        faultTolerance.execute(rdfInput, new FaultTolerance.ConnectionAction() {

            @Override
            public void action(RepositoryConnection connection) throws Exception {
                // Execute user query over new graph.
                executeUpdateQuery(updateQuery, Arrays.asList(targetgraph), targetgraph, connection);
            }

        });
    }

    /**
     * Execute given query.
     *
     * @param query
     * @param sourceGraphs USING graphs.
     * @param targetGraph  WITH graphs.
     * @param connection
     * @throws eu.unifiedviews.dpu.DPUException
     */
    protected void executeUpdateQuery(String query, List<URI> sourceGraphs, URI targetGraph,
            RepositoryConnection connection) throws DPUException {
        // Prepare query.
        if (!useDataset()) {
            if (Pattern.compile(Pattern.quote("DELETE"), Pattern.CASE_INSENSITIVE).matcher(query).find()) {
                query = query.replaceFirst("(?i)DELETE", prepareWithClause(targetGraph) + " DELETE");
            } else {
                query = query.replaceFirst("(?i)INSERT", prepareWithClause(targetGraph) + " INSERT");
            }
            query = query.replaceFirst("(?i)WHERE", prepareUsingClause(sourceGraphs) + "WHERE");
        }
        LOG.debug("Query to execute: {}", query);
        try {
            // Execute query.
            final Update update = connection.prepareUpdate(QueryLanguage.SPARQL, query);
            if (useDataset()) {
                final DatasetImpl dataset = new DatasetImpl();
                for (URI graph : sourceGraphs) {
                    dataset.addDefaultGraph(graph);
                }
                dataset.addDefaultRemoveGraph(targetGraph);
                dataset.setDefaultInsertGraph(targetGraph);
                update.setDataset(dataset);
            }
            update.execute();
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
    protected URI createOutputGraph(RDFDataUnit.Entry entry) throws DPUException {
        try {
            return rdfOutput.addNewDataGraph(entry.getSymbolicName());
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
     * Get graph URIs from entry list.
     *
     * @param entries
     * @return
     * @throws DPUException
     */
    protected List<URI> toUriList(final List<RDFDataUnit.Entry> entries) throws DPUException {
        final List<URI> result = new ArrayList(entries.size());
        faultTolerance.execute(new FaultTolerance.Action() {

            @Override
            public void action() throws Exception {
                for (RDFDataUnit.Entry entry : entries) {
                    try {
                        result.add(entry.getDataGraphURI());
                    } catch (DataUnitException ex) {
                        throw new DPUException("Problem with DataUnit.", ex);
                    }
                }
            }

        });
        return result;
    }

    /**
     *
     * @param uris
     * @return Using clause for SPARQL insert, based on input graphs.
     * @throws DPUException
     */
    protected String prepareUsingClause(List<URI> uris) throws DPUException {
        final StringBuilder usingClause = new StringBuilder();
        for (URI uri : uris) {
            usingClause.append("USING <");
            usingClause.append(uri.stringValue());
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
    protected List<RDFDataUnit.Entry> getInputEntries(final RDFDataUnit dataUnit) throws DPUException {
        return faultTolerance.execute(new FaultTolerance.ActionReturn<List<RDFDataUnit.Entry>>() {

            @Override
            public List<RDFDataUnit.Entry> action() throws Exception {
                return DataUnitUtils.getEntries(dataUnit, RDFDataUnit.Entry.class);
            }
        });
    }

    protected final boolean useDataset() {
        // Should be removed once bug in Sesame or Virtuoso is fixex.
        return System.getProperty(MetadataUtilsInstance.ENV_PROP_VIRTUOSO) == null;
    }

}
