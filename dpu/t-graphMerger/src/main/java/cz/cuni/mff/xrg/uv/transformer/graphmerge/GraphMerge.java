package cz.cuni.mff.xrg.uv.transformer.graphmerge;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.openrdf.repository.RepositoryConnection;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.helpers.dataunit.rdf.RdfDataUnitUtils;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultToleranceUtils;
import eu.unifiedviews.helpers.dpu.rdf.sparql.SparqlUtils;

@DPU.AsTransformer
public class GraphMerge extends AbstractDpu<GraphMergeConfig_V1> {

	private static final Logger LOG = LoggerFactory.getLogger(GraphMerge.class);

    private static final String COPY_QUERY = "INSERT { ?s ?p ?o } WHERE { ?s ?p ?o }";

    @DataUnit.AsInput(name = "input")
    public RDFDataUnit rdfInput;
    
    @DataUnit.AsOutput(name = "output")
    public WritableRDFDataUnit rdfOutput;

    @ExtensionInitializer.Init
    public FaultTolerance faultTolerance;

	public GraphMerge() {
		super(GraphMergeVaadinDialog.class, ConfigHistory.noHistory(GraphMergeConfig_V1.class));
	}
		
    @Override
    protected void innerExecute() throws DPUException {
        // Get list of input graphs.
        final List<RDFDataUnit.Entry> inputEntries = FaultToleranceUtils.getEntries(faultTolerance, rdfInput,
                RDFDataUnit.Entry.class);
        // Prepare output graph.
        final RDFDataUnit.Entry outputEntry = faultTolerance.execute(new FaultTolerance.ActionReturn<RDFDataUnit.Entry>() {

            @Override
            public RDFDataUnit.Entry action() throws Exception {
                return RdfDataUnitUtils.addGraph(rdfOutput, generateOutputSymbolicName());
            }
        });
        // Per-graph execution.
        int counter = 0;
        for (final RDFDataUnit.Entry entry : inputEntries) {
            LOG.info("Processing {}/{}", ++counter, inputEntries.size());
            faultTolerance.execute(rdfInput, new FaultTolerance.ConnectionAction() {

                @Override
                public void action(RepositoryConnection connection) throws Exception {
                    final SparqlUtils.SparqlUpdateObject update =
                            SparqlUtils.createInsert(COPY_QUERY, Arrays.asList(entry), outputEntry);
                    // Copy statementes.
                    SparqlUtils.execute(connection, update);
                }
            });
        }
    }

    /**
     *
     * @return New and unique output graph name.
     */
    private String generateOutputSymbolicName() {
        return "GraphMerge/output/generated-" + Long.toString((new Date()).getTime());
    }

}
