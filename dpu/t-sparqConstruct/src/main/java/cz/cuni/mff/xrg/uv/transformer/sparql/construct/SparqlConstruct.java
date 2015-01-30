package cz.cuni.mff.xrg.uv.transformer.sparql.construct;

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
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.advanced.DpuAdvancedBase;
import cz.cuni.mff.xrg.uv.boost.dpu.config.MasterConfigObject;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.uv.boost.dpu.addon.impl.FaultToleranceWrap;
import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigHistory;
import cz.cuni.mff.xrg.uv.boost.dpu.context.ContextUtils;
import cz.cuni.mff.xrg.uv.utils.dataunit.DataUnitUtils;
import cz.cuni.mff.xrg.uv.utils.dataunit.metadata.ManipulatorInstance;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPUContext.MessageType;

/*
 *
 * @author Škoda Petr
 */
@DPU.AsTransformer
public class SparqlConstruct extends DpuAdvancedBase<SparqlConstructConfig_V1> {

    private static final Logger LOG = LoggerFactory.getLogger(SparqlConstruct.class);

    private static final int MAX_GRAPH_COUNT = 1000;

    @DataUnit.AsInput(name = "input")
    public RDFDataUnit rdfInput;

    @DataUnit.AsOutput(name = "output")
    public WritableRDFDataUnit rdfOutput;

    public SparqlConstruct() {
        super(ConfigHistory.createNoHistory(SparqlConstructConfig_V1.class),
                AddonInitializer.create(new FaultToleranceWrap(), new SPARQLConfig_V1_Convertor()));
    }

    @Override
    protected void innerExecute() throws DPUException {
        // Update query ie. substitute constract with insert.
        String query = config.getQuery();
        if (query == null || query.isEmpty()) {
            throw new DPUException("Query string is null or empty");
        }
        // Modify query - we always do inserts.
        query = query.replaceFirst("(?i)CONSTRUCT", "INSERT");
        // Get graphs.
        final List<RDFDataUnit.Entry> sourceEntries = getInputEntries(rdfInput);
        final URI targetGraph = createOutputGraph();
        // Execute.
        executeUpdateQuery(query, sourceEntries, targetGraph);
    }

    @Override
    public AbstractConfigDialog<MasterConfigObject> getConfigurationDialog() {
        return new SparqlConstructVaadinDialog();
    }

    /**
     * Get connection and use it to execute given query. Based on user option the query is executed
     * over one or over multiple graphs.
     *
     * @param query
     * @param sourceEntries
     * @param targetgraph
     * @throws DPUException
     */
    protected void executeUpdateQuery(final String query, final List<RDFDataUnit.Entry> sourceEntries,
            final URI targetgraph) throws DPUException {
        final FaultToleranceWrap wrap = getAddon(FaultToleranceWrap.class);
        // Execute based on configuration.
        if (config.isPerGraph()) {
            // Execute one graph at time.
            ContextUtils.sendMessage(context, MessageType.INFO, "Per-graph query execution",
                    "Number of graphs: %d", sourceEntries.size());
            // Execute one query per graph, the target graph is always the same.
            int counter = 1;
            for (final RDFDataUnit.Entry sourceEntry : sourceEntries) {
                LOG.info("Executing {}/{}", counter++, sourceEntries.size());
                wrap.execute(rdfInput, new FaultToleranceWrap.ConnectionAction() {

                    @Override
                    public void action(RepositoryConnection connection) throws Exception {
                        executeUpdateQuery(query, Arrays.asList(sourceEntry), targetgraph, connection);
                    }

                });
            }            
        } else {
            // All graph at once, just check size.
            if (sourceEntries.size() > MAX_GRAPH_COUNT) {
                throw new DPUException("Too many graphs .. (limit: " + MAX_GRAPH_COUNT + ", given: " +
                        sourceEntries.size() + ")");
            }
            ContextUtils.sendMessage(context, MessageType.INFO, "Executing over all graphs",
                    "Executing query over all graphs (%d)", sourceEntries.size());
            // Execute single query.
            wrap.execute(rdfInput, new FaultToleranceWrap.ConnectionAction() {

                @Override
                public void action(RepositoryConnection connection) throws Exception {
                    executeUpdateQuery(query, sourceEntries, targetgraph, connection);
                }

            });
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
     * @throws eu.unifiedviews.dataunit.DataUnitException
     */
    protected void executeUpdateQuery(String query, final List<RDFDataUnit.Entry> sourceEntries, URI targetGraph,
            RepositoryConnection connection) throws DPUException, DataUnitException {
        // Prepare query.
        if (!useDataset()) {
            if (Pattern.compile(Pattern.quote("DELETE"), Pattern.CASE_INSENSITIVE).matcher(query).find()) {
                query = query.replaceFirst("(?i)DELETE", prepareWithClause(targetGraph) + " DELETE");
            } else {
                query = query.replaceFirst("(?i)INSERT", prepareWithClause(targetGraph) + " INSERT");
            }
            query = query.replaceFirst("(?i)WHERE", prepareUsingClause(sourceEntries) + "WHERE");
        }
        LOG.debug("Query to execute: {}", query);
        try {
            // Execute query.
            final Update update = connection.prepareUpdate(QueryLanguage.SPARQL, query);
            if (useDataset()) {
                final DatasetImpl dataset = new DatasetImpl();
                for (RDFDataUnit.Entry entry : sourceEntries) {
                    dataset.addDefaultGraph(entry.getDataGraphURI());
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
    protected String prepareUsingClause(final List<RDFDataUnit.Entry> entries) throws DPUException {
        final FaultToleranceWrap wrap = getAddon(FaultToleranceWrap.class);
        return wrap.execute(new FaultToleranceWrap.ActionReturn<String>() {

            @Override
            public String action() throws Exception {
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

        });
    }

    /**
     *
     * @param dataUnit
     * @return Data graphs in given DataUnit.
     * @throws DPUException
     */
    protected List<RDFDataUnit.Entry> getInputEntries(final RDFDataUnit dataUnit) throws DPUException {
        final FaultToleranceWrap wrap = getAddon(FaultToleranceWrap.class);
        return wrap.execute(new FaultToleranceWrap.ActionReturn<List<RDFDataUnit.Entry>>() {

            @Override
            public List<RDFDataUnit.Entry> action() throws Exception {
                return  DataUnitUtils.getEntries(dataUnit);
            }
        });
    }

    protected final boolean useDataset() {
        // Should be removed once bug in Sesame or Virtuoso is fixex.
        return System.getProperty(ManipulatorInstance.ENV_PROP_VIRTUOSO) == null;
    }

}
