package cz.cuni.mff.xrg.uv.transformer.sparql.construct;

import java.util.Date;
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

import cz.cuni.mff.xrg.uv.boost.dpu.utils.SendMessage;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;

/**
 *
 * @author Škoda Petr
 */
@DPU.AsTransformer
public class SparqlConstruct extends DpuAdvancedBase<SparqlConstructConfig_V1> {

    private static final Logger LOG = LoggerFactory.getLogger(SparqlConstruct.class);

    @DataUnit.AsInput(name = "input")
    public RDFDataUnit rdfInput;

    @DataUnit.AsOutput(name = "output")
    public WritableRDFDataUnit rdfOutput;

    public SparqlConstruct() {
        super(SparqlConstructConfig_V1.class, AddonInitializer.noAddons());
    }

    @Override
    protected void innerExecute() throws DPUException {
        // Update query ie. substitute constract with insert.
        String query = config.getQuery();
        if (query == null || query.isEmpty()) {
            throw new DPUException("Query string is null or empty");
        }
        // Prepare query with graphs.
        query = query.replaceFirst("(?i)CONSTRUCT", prepareWithClause() + "INSERT");
        query = query.replaceFirst("(?i)WHERE", prepareUsingClause()+ "WHERE");
        SendMessage.sendInfo(context, "Query to execute", "Query to execute: %s", query);
        // Execute query.
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

    @Override
    public AbstractConfigDialog<MasterConfigObject> getConfigurationDialog() {
        return new SparqlConstructVaadinDialog();
    }

    /**
     * Register new output graph and return WITH clause for SPARQL insert.
     *
     * @return
     * @throws DPUException
     */
    protected String prepareWithClause() throws DPUException {
        // Register new output graph
        final String symbolicName = "http://unifiedviews.eu/resource/sparql-construct/" 
                + Long.toString((new Date()).getTime());
        final String graphUri;
        try {
            graphUri = rdfOutput.addNewDataGraph(symbolicName).stringValue();
        } catch (DataUnitException ex) {
            throw new DPUException("DPU failed to add a new graph.", ex);
        }
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
