package cz.cuni.mff.xrg.uv.extractor.sparqlendpoint;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.RepositoryConnection;

import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.uv.service.external.ExternalError;
import cz.cuni.mff.xrg.uv.service.external.ExternalServicesFactory;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.helpers.dataunit.DataUnitUtils;
import eu.unifiedviews.helpers.dataunit.rdf.RdfDataUnitUtils;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.config.migration.ConfigurationUpdate;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;
import eu.unifiedviews.helpers.dpu.extension.rdf.simple.WritableSimpleRdf;
import eu.unifiedviews.plugins.extractor.rdffromsparql.RdfFromSparqlEndpointConfig_V1;

/**
 * Main data processing unit class.
 *
 * @author Petr Škoda
 */
@DPU.AsExtractor
public class SparqlEndpoint extends AbstractDpu<SparqlEndpointConfig_V1> {

    private static final Logger LOG = LoggerFactory.getLogger(SparqlEndpoint.class);

    @DataUnit.AsOutput(name = "output")
    public WritableRDFDataUnit rdfOutput;

    @ExtensionInitializer.Init(param = "rdfOutput")
    public WritableSimpleRdf output;

    @ExtensionInitializer.Init
    public FaultTolerance faultTolerance;

    @ExtensionInitializer.Init(param = "eu.unifiedviews.plugins.transformer.zipper.ZipperConfig__V1")
    public ConfigurationUpdate _ConfigurationUpdate;

	public SparqlEndpoint() {
		super(SparqlEndpointVaadinDialog.class, 
                ConfigHistory.history(RdfFromSparqlEndpointConfig_V1.class).addCurrent(SparqlEndpointConfig_V1.class));
	}
		
    @Override
    protected void innerExecute() throws DPUException {
        // Prepare output.
        final RDFDataUnit.Entry outputEntry = faultTolerance.execute(new FaultTolerance.ActionReturn<RDFDataUnit.Entry>() {

            @Override
            public RDFDataUnit.Entry action() throws Exception {
                return RdfDataUnitUtils.addGraph(rdfOutput, DataUnitUtils.generateSymbolicName(this.getClass()));
            }
        });
        output.setOutput(outputEntry);
        // Connect to remote repository.
        final RDFDataUnit remote;
        try {
            remote = ExternalServicesFactory.remoteRdf(ctx, config.getEndpoint(), new URI[0]);
        } catch (ExternalError ex) {
            throw ContextUtils.dpuException(ctx, ex, "Can't connect to remote endpoint.");
        }
        // Execute query.
        faultTolerance.execute(remote, new FaultTolerance.ConnectionAction() {

            @Override
            public void action(RepositoryConnection connection) throws Exception {
                GraphQuery query = connection.prepareGraphQuery(QueryLanguage.SPARQL, config.getQuery());
                LOG.info("Executing query.");
                GraphQueryResult result = query.evaluate();
                LOG.info("Storing result.");
                long counter = 0;
                while (result.hasNext()) {
                    final Statement st = result.next();
                    // Add to out output.
                    output.add(st.getSubject(), st.getPredicate(), st.getObject());
                    // Print info.
                    ++counter;
                    if (counter % 100000 == 0) {
                        LOG.info("{} triples extracted", counter);
                    }
                }
            }
        });
        // Flush buffre.
        output.flushBuffer();
        // Get and print size.
        faultTolerance.execute(rdfOutput, new FaultTolerance.ConnectionAction() {

            @Override
            public void action(RepositoryConnection connection) throws Exception {
                long size = connection.size(outputEntry.getDataGraphURI());
                ContextUtils.sendShortInfo(ctx, "{0} triples extracted", size);
            }
        });

    }
}
