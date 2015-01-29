package cz.cuni.mff.xrg.uv.transformer.graphmerge;

import java.util.Date;

import org.openrdf.model.URI;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.repository.RepositoryConnection;
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
import cz.cuni.mff.xrg.uv.utils.dataunit.rdf.RdfDataUnitUtils;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPUContext;

/**
 * 
 * @author Škoda Petr
 */
@DPU.AsTransformer
public class GraphMerge extends AbstractDpu<GraphMergeConfig_V1, GraphMergeOntology> {

    private static final Logger LOG = LoggerFactory.getLogger(GraphMerge.class);

    @DataUnit.AsInput(name = "input")
    public RDFDataUnit rdfInput;

    @DataUnit.AsOutput(name = "output")
    public WritableRDFDataUnit rdfOutput;

    @AutoInitializer.Init
    public FaultTolerance faultTolerance;

    public GraphMerge() {
        super(GraphMergeVaadinDialog.class, ConfigHistory.noHistory(GraphMergeConfig_V1.class),
                new GraphMergeOntology());
    }

    @Override
    protected void innerExecute() throws DPUException {
        if (useDataset()) {
            ContextUtils.sendMessage(context, DPUContext.MessageType.INFO, "OpenRdf mode.", "");
        } else {
            ContextUtils.sendMessage(context, DPUContext.MessageType.INFO, "Virtuoso mode.", "");
        }
        // Get list of input graphs.
        final URI[] inputGraphs = faultTolerance.execute(new FaultTolerance.ActionReturn<URI[]>() {

            @Override
            public URI[] action() throws Exception {
                return RdfDataUnitUtils.asGraphs(DataUnitUtils.getEntries(rdfInput, RDFDataUnit.Entry.class));
            }

        });
        // Prepare output graph.
        final URI targetGraph = faultTolerance.execute(new FaultTolerance.ActionReturn<URI>() {

            @Override
            public URI action() throws Exception {
                return rdfOutput.addNewDataGraph(generateOutputSymbolicName());
            }

        });
        // Copy data from input graphs to output.
        int counter = 1;
        for (final URI sourceGraph : inputGraphs) {
            LOG.info("Merging {}/{}", counter++, inputGraphs.length);
            LOG.debug("{} -> {}", sourceGraph.stringValue(), targetGraph.stringValue());
            faultTolerance.execute(rdfOutput, new FaultTolerance.ConnectionAction() {

                @Override
                public void action(RepositoryConnection connection) throws Exception {
                    final Update updateQuery;
                    if (!useDataset()) {
                        final String query = String.format("ADD <%s> TO <%s>", sourceGraph.stringValue(),
                                targetGraph.stringValue());
                        updateQuery = connection.prepareUpdate(QueryLanguage.SPARQL, query);
                    } else {
                        updateQuery = connection.prepareUpdate(QueryLanguage.SPARQL,
                                "INSERT {?s ?p ?o} WHERE {?s ?p ?o}");
                        final DatasetImpl dataset = new DatasetImpl();
                        dataset.addDefaultGraph(sourceGraph);
                        dataset.setDefaultInsertGraph(targetGraph);
                        updateQuery.setDataset(dataset);
                    }
                    updateQuery.execute();
                }
            });
        }

        // TODO Petr Add metadata here?
    }

    /**
     *
     * @return New and unique output graph name.
     */
    private String generateOutputSymbolicName() {
        return "GraphMerge/output/generated-" + Long.toString((new Date()).getTime());
    }

    protected final boolean useDataset() {
        // Should be removed once bug in Sesame or Virtuoso is fixex.
        return System.getProperty(MetadataUtilsInstance.ENV_PROP_VIRTUOSO) == null;
    }

}
