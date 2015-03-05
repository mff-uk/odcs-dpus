package cz.cuni.mff.xrg.uv.quality.sparqlask;

import java.util.Arrays;
import java.util.List;

import org.openrdf.repository.RepositoryConnection;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.helpers.dataunit.DataUnitUtils;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultToleranceUtils;
import eu.unifiedviews.helpers.dpu.rdf.sparql.SparqlUtils;

@DPU.AsQuality
public class SparqlAsk extends AbstractDpu<SparqlAskConfig_V1> {

	private static final Logger LOG = LoggerFactory.getLogger(SparqlAsk.class);
	
    private static final String ADD_QUERY = "INSERT {?s ?p ?o } WHERE {?s ?p ?o}";

    @DataUnit.AsInput(name = "rdf")
    public RDFDataUnit rdfInData;

    @DataUnit.AsOutput(name = "rdf")
    public WritableRDFDataUnit rdfOutData;

    @ExtensionInitializer.Init
    public FaultTolerance faultTolerance;

    protected boolean emptyFound = false;

	public SparqlAsk() {
		super(SparqlAskVaadinDialog.class, ConfigHistory.noHistory(SparqlAskConfig_V1.class));
	}

    @Override
    protected void innerExecute() throws DPUException {
        // Copy data to output.
        LOG.info("Coping input data to output ...");
        faultTolerance.execute(rdfInData, new FaultTolerance.ConnectionAction() {

            @Override
            public void action(RepositoryConnection connection) throws Exception {
                List<RDFDataUnit.Entry> inputs = DataUnitUtils.getMetadataEntries(rdfInData);
                RDFDataUnit.Entry output = DataUnitUtils.getWritableMetadataEntry(rdfOutData);
                // Prepare query.
                SparqlUtils.SparqlUpdateObject update = SparqlUtils.createInsert(ADD_QUERY, inputs, output);
                // Execute sparql.
                SparqlUtils.execute(connection, update);
            }
        });
        LOG.info("Coping input data to output ... done");
        // Get input graphs.
        LOG.info("Reading input graphs ...");
        final List<RDFDataUnit.Entry> graphs = FaultToleranceUtils.getEntries(faultTolerance, rdfInData, RDFDataUnit.Entry.class);
        // Execute query.
        if (config.isPerGraph()) {
            ContextUtils.sendShortInfo(ctx, "per-graph mode");

            for (RDFDataUnit.Entry sourceGraph : graphs) {
                checkGraph(Arrays.asList(sourceGraph));
                if (this.emptyFound) {
                    break;
                }
            }
        } else {
            checkGraph(graphs);
        }
    }

    /**
     * Report failure ie. send user defined message.
     */
    private void reportFailure() {
        String msg = config.getMessage();
        if (msg == null || msg.isEmpty()) {
            msg = SparqlAskConfig_V1.AUTO_MESSAGE;
        }
        ContextUtils.sendMessage(ctx, config.getMessageType(), msg, "");
        emptyFound = true;
    }

    protected void checkGraph(final List<RDFDataUnit.Entry> entries) throws DPUException {
        faultTolerance.execute(rdfInData, new FaultTolerance.ConnectionAction() {

            @Override
            public void action(RepositoryConnection connection) throws Exception {
                final SparqlUtils.SparqlAskObject ask = SparqlUtils.createAsk(config.getAskQuery(), entries);
                SparqlUtils.execute(connection, ask);
                if (!ask.result) {
                    reportFailure();
                }
            }
        });
    }

}
