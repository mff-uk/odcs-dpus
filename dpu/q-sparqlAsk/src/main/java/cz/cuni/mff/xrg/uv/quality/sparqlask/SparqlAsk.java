package cz.cuni.mff.xrg.uv.quality.sparqlask;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.advanced.DpuAdvancedBase;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.uv.boost.dpu.config.MasterConfigObject;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.sparql.SparqlUtils;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.sparql.SparqlUtilsException;
import cz.cuni.mff.xrg.uv.utils.dataunit.DataUnitUtils;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;

@DPU.AsQuality
public class SparqlAsk extends DpuAdvancedBase<SparqlAskConfig_V1> {

	private static final Logger LOG = LoggerFactory.getLogger(SparqlAsk.class);
	
    private static final String ADD_QUERY = "ADD <%s> TO <%s>";

    @DataUnit.AsInput(name = "rdf")
    public RDFDataUnit rdfInData;

    @DataUnit.AsOutput(name = "rdf")
    public WritableRDFDataUnit rdfOutData;

    protected RepositoryConnection connection = null;

	public SparqlAsk() {
		super(SparqlAskConfig_V1.class, AddonInitializer.noAddons());
	}

    @Override
    protected void innerExecute() throws DPUException {
        // Get input graphs.
        LOG.info("Reading input graphs ...");
        final List<RDFDataUnit.Entry> graphs;
        try {
            graphs = DataUnitUtils.getEntries(rdfInData);
        } catch (DataUnitException ex) {
            throw new DPUException("Can't get graph list.");
        }
        LOG.info("Reading input graphs ... done");
        try {
            connection = rdfInData.getConnection();
        } catch (DataUnitException ex) {
            throw new DPUException("Can't get connection.", ex);
        }
        // Copy data to output.
        LOG.info("Coping input data to oudput ...");
        try {
            final URI metadataTarget = rdfOutData.getMetadataWriteGraphname();            
            final Set<URI> metadataSources = rdfInData.getMetadataGraphnames();
            for (URI source : metadataSources) {
                final String copyQuery = String.format(ADD_QUERY, source.stringValue(),
                        metadataTarget.stringValue());
                // Execute copy query.
                try {
                    connection.prepareUpdate(QueryLanguage.SPARQL, copyQuery).execute();
                } catch (MalformedQueryException | UpdateExecutionException | RepositoryException ex) {
                    throw new DPUException(ex);
                }
            }
        } catch (DataUnitException ex) {
            throw new DPUException("Can't copy data to output.", ex);
        }
        LOG.info("Coping input data to oudput ... done");
        // Execute query.
        if (config.isPerGraph()) {
            context.sendMessage(DPUContext.MessageType.INFO, "per-graph mode");
            for (RDFDataUnit.Entry sourceGraph : graphs) {
                // Prepare query.
                SparqlUtils.SparqlAskObject ask;
                try {
                    ask = SparqlUtils.createAsk(config.getAskQuery(), Arrays.asList(sourceGraph));
                } catch (DataUnitException | SparqlUtilsException ex) {
                    throw new DPUException("Can't prepare ASK query.", ex);
                }
                // Execute query.
                try {
                    SparqlUtils.execute(connection, ask);
                } catch (MalformedQueryException | UpdateExecutionException |
                         QueryEvaluationException | RepositoryException ex) {
                    throw new DPUException(ex);
                }
                // Check result.
                if (!ask.result) {
                    // Report problem and quit.
                    reportFailure();
                    break;
                }
            }
        } else {
            SparqlUtils.SparqlAskObject ask;
            try {
                ask = SparqlUtils.createAsk(config.getAskQuery(), graphs);
            } catch (DataUnitException | SparqlUtilsException ex) {
                throw new DPUException("Can't prepare ASK query.", ex);
            }
            // Execute query.
            try {
                SparqlUtils.execute(connection, ask);
            } catch (MalformedQueryException | UpdateExecutionException |
                    QueryEvaluationException | RepositoryException ex) {
                throw new DPUException(ex);
            }
            // Check result.
            if (!ask.result) {
                // Report problem and quit.
                reportFailure();
            }
        }
    }

    @Override
    public AbstractConfigDialog<MasterConfigObject> getConfigurationDialog() {
        return new SparqlAskVaadinDialog();
    }

    @Override
    protected void innerCleanUp() {
        super.innerCleanUp();
        if (connection != null) {
            try {
                connection.close();
            } catch (RepositoryException ex) {
                LOG.error("Can't close connection.", ex);
            }
        }
    }

    /**
     * Report failure.
     */
    private void reportFailure() {
        String msg = config.getMessage();
        if (msg == null || msg.isEmpty()) {
            msg = SparqlAskConfig_V1.AUTO_MESSAGE;
        }
        context.sendMessage(config.getMessageType(), msg);
    }

}
