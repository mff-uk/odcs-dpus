package cz.cuni.mff.xrg.uv.transformer.sparql.update;

import java.util.Date;
import java.util.regex.Pattern;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
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

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;

@DPU.AsTransformer
public class SparqlUpdate extends DpuAdvancedBase<SparqlUpdateConfig_V1> {

	private static final Logger LOG = LoggerFactory.getLogger(SparqlUpdate.class);

    /**
     * Query used to copy all data from input to output graph.
     */
    private static final String QUERY_COPY = "%s INSERT {?s ?p ?o} %s WHERE {?s ?p ?o}";

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
        // Copy data into a new graph.
        final String outputgraphUri = prepareOutputGraph();
        final String copyQuery = String.format(QUERY_COPY, prepareWithClause(outputgraphUri),
                prepareUsingClause());
        LOG.debug("used copy query: {}", copyQuery);
        executeQuery(copyQuery);
        // If contains DELETE, then DELETE is before INSERT. Add WITH clause before first of them.
        if (Pattern.compile(Pattern.quote("DELETE"), Pattern.CASE_INSENSITIVE).matcher(query).find()) {
            query = query.replaceFirst("(?i)DELETE", prepareWithClause(outputgraphUri) + " DELETE");
        } else {
            query = query.replaceFirst("(?i)INSERT", prepareWithClause(outputgraphUri) + " INSERT");
        }
        LOG.debug("used update query: {}", query);
        executeQuery(query);
    }

    @Override
    public AbstractConfigDialog<MasterConfigObject> getConfigurationDialog() {
        return new SparqlUpdateVaadinDialog();
    }

    /**
     * Execute given query.
     *
     * @param query
     * @throws DPUException
     */
    protected void executeQuery(String query) throws DPUException {
        RepositoryConnection connection = null;
        try {
            connection = rdfInput.getConnection();
            final Update update = connection.prepareUpdate(QueryLanguage.SPARQL, query);
            update.execute();
        } catch (DataUnitException ex) {
            throw new DPUException("Problem with data unit.", ex);
        } catch (MalformedQueryException | UpdateExecutionException ex) {
            throw new DPUException("Problem with query", ex);
        } catch (RepositoryException ex) {
            throw new DPUException("Problem with repository.", ex);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (RepositoryException ex) {
                    LOG.error("Can't close connection.", ex);
                }
            }
        }        
    }

    /**
     * Register new output graph.
     * 
     * @return URI of output graph.
     * @throws DPUException 
     */
    protected String prepareOutputGraph() throws DPUException {
        // Register new output graph
        final String symbolicName = "http://unifiedviews.eu/resource/sparql-update/"
                + Long.toString((new Date()).getTime());
        try {
            return rdfOutput.addNewDataGraph(symbolicName).stringValue();
        } catch (DataUnitException ex) {
            throw new DPUException("DPU failed to add a new graph.", ex);
        }
    }

    /**
     * Register new output graph and return WITH clause for SPARQL insert.
     *
     * @param graphUri Name of output graph.
     * @return
     * @throws DPUException
     */
    protected String prepareWithClause(String graphUri) throws DPUException {
        final StringBuilder withClause = new StringBuilder();
        withClause.append("WITH <");
        withClause.append(graphUri);
        withClause.append("> \n");
        return withClause.toString();
    }

    /**
     *
     * @return Using clause for SPARQL insert, based on input graphs.
     * @throws DPUException
     */
    protected String prepareUsingClause() throws DPUException {
        final StringBuilder usingClause = new StringBuilder();
        try {
            final RDFDataUnit.Iteration iter = rdfInput.getIteration();
            while (iter.hasNext()) {
                final String graphUri = iter.next().getDataGraphURI().stringValue();
                usingClause.append("USING <");
                usingClause.append(graphUri);
                usingClause.append("> \n");
            }
        } catch (DataUnitException ex) {
            throw new DPUException("Problem with data unit dueing using clause generation.", ex);
        }
        return usingClause.toString();
    }

}
