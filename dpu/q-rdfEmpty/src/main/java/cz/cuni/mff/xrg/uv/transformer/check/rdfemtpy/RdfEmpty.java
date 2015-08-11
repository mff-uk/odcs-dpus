package cz.cuni.mff.xrg.uv.transformer.check.rdfemtpy;

import java.util.List;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;

import org.openrdf.repository.RepositoryConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.helpers.dataunit.DataUnitUtils;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultToleranceUtils;
import eu.unifiedviews.helpers.dpu.rdf.sparql.SparqlUtils;

@DPU.AsQuality
public class RdfEmpty extends AbstractDpu<RdfEmptyConfig_V1> {

    private static final Logger LOG = LoggerFactory.getLogger(RdfEmpty.class);

    private static final String QUERY_COPY = "INSERT { ?s ?p ?o } WHERE { ?s ?p ?o }";

    @DataUnit.AsInput(name = "rdf")
    public RDFDataUnit rdfInData;

    @DataUnit.AsOutput(name = "rdf")
    public WritableRDFDataUnit rdfOutData;

    @ExtensionInitializer.Init
    public FaultTolerance faultTolerance;

    /**
     * True if all graphs are empty. Is located here so we can call it from lambda method.
     */
    private boolean isEmpty = true;

    public RdfEmpty() {
        super(RdfEmptyVaadinDialog.class, ConfigHistory.noHistory(RdfEmptyConfig_V1.class));
    }

    @Override
    protected void innerExecute() throws DPUException {
        final List<RDFDataUnit.Entry> graphs = FaultToleranceUtils.getEntries(faultTolerance, rdfInData,
                RDFDataUnit.Entry.class);

        faultTolerance.execute(rdfInData, new FaultTolerance.ConnectionAction() {

            @Override
            public void action(RepositoryConnection connection) throws Exception {
                for (RDFDataUnit.Entry entry : graphs) {
                    long size = connection.size(entry.getDataGraphURI());
                    if (size > 0) {
                        isEmpty = false;
                    }
                    LOG.debug("size( {} ) = {}", entry.getDataGraphURI(), size);
                }
            }
        });
        if (!isEmpty) {
            String msg = config.getMessage();
            if (msg == null || msg.isEmpty()) {
                msg = RdfEmptyConfig_V1.AUTO_MESSAGE;
            }
            ctx.getExecMasterContext().getDpuContext().sendMessage(config.getMessageType(), msg);
        } else {
            // Copy metadata (data) from input to output.
            faultTolerance.execute(rdfInData, new FaultTolerance.ConnectionAction() {

                @Override
                public void action(RepositoryConnection connection) throws Exception {
                    SparqlUtils.SparqlUpdateObject update = SparqlUtils.createInsert(QUERY_COPY,
                            DataUnitUtils.getMetadataEntries(rdfInData),
                            DataUnitUtils.getWritableMetadataEntry(rdfOutData));
                    SparqlUtils.execute(connection, update);
                }
            }
            );
        }
    }

}