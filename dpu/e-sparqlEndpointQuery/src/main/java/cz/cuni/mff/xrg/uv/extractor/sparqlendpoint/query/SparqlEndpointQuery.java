package cz.cuni.mff.xrg.uv.extractor.sparqlendpoint.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
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
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultToleranceUtils;
import eu.unifiedviews.helpers.dpu.extension.rdf.simple.WritableSimpleRdf;
import eu.unifiedviews.helpers.dpu.rdf.sparql.SparqlUtils;

/**
 * Main data processing unit class.
 *
 * @author Petr Škoda
 */
@DPU.AsExtractor
public class SparqlEndpointQuery extends AbstractDpu<SparqlEndpointQueryConfig_V1> {

    private static final Logger LOG = LoggerFactory.getLogger(SparqlEndpointQuery.class);

    @DataUnit.AsInput(name = "input")
    public RDFDataUnit rdfInput;

    @DataUnit.AsOutput(name = "output")
    public WritableRDFDataUnit rdfOutput;

    @ExtensionInitializer.Init(param = "rdfOutput")
    public WritableSimpleRdf output;

    @ExtensionInitializer.Init
    public FaultTolerance faultTolerance;

	public SparqlEndpointQuery() {
		super(SparqlEndpointQueryVaadinDialog.class, ConfigHistory.noHistory(SparqlEndpointQueryConfig_V1.class));
	}
		
    @Override
    protected void innerExecute() throws DPUException {
        // Execute select query and colect the results.
        final List<RDFDataUnit.Entry> inputGraphs = FaultToleranceUtils.getEntries(faultTolerance, rdfInput, RDFDataUnit.Entry.class);
        final SparqlUtils.QueryResultCollector results = new SparqlUtils.QueryResultCollector();
        faultTolerance.execute(rdfInput, new FaultTolerance.ConnectionAction() {

            @Override
            public void action(RepositoryConnection connection) throws Exception {
                SparqlUtils.SparqlSelectObject select = SparqlUtils.createSelect(config.getSelectQuery(), inputGraphs);
                SparqlUtils.execute(connection, ctx, select, results);
            }
        });
        // Prepare output.
        final RDFDataUnit.Entry outputEntry = faultTolerance.execute(new FaultTolerance.ActionReturn<RDFDataUnit.Entry>() {

            @Override
            public RDFDataUnit.Entry action() throws Exception {
                return RdfDataUnitUtils.addGraph(rdfOutput, DataUnitUtils.generateSymbolicName(this.getClass()));
            }
        });
        output.setOutput(outputEntry);
        // For each row prepare template and execute it.
        // TODO: This can be a parameteer in a configuration.
        final int bufferSize = 100;
        final List<String> queries = new ArrayList<>(bufferSize + 1);
        for (Map<String, Value> resultRow : results.getResults()) {
            String template = config.getQueryTemplate();
            for (String key : resultRow.keySet()){
                template = template.replaceAll(Pattern.quote("${" + key + "}"), resultRow.get(key).stringValue());
            }
            // Add to buffer.
            queries.add(template);
            if (queries.size() > bufferSize) {
                executeRemote(queries);
                queries.clear();
            }
        }
        // Execute the rest of stored queries.
        executeRemote(queries);
        // Get and print size.
        faultTolerance.execute(rdfOutput, new FaultTolerance.ConnectionAction() {

            @Override
            public void action(RepositoryConnection connection) throws Exception {
                long size = connection.size(outputEntry.getDataGraphURI());
                ContextUtils.sendShortInfo(ctx, "{0} triples extracted", size);
            }
        });
    }

    /**
     * Execute given SPARQL construct query on remote endpoint and save results into the DPUs output.
     *
     * @param queries
     * @throws DPUException
     */
    protected void executeRemote(final List<String> queries) throws DPUException {
        // Connect to remote repository.
        final RDFDataUnit remote;
        try {
            remote = ExternalServicesFactory.remoteRdf(ctx, config.getEndpoint(), new URI[0]);
        } catch (ExternalError ex) {
            throw ContextUtils.dpuException(ctx, ex, "Can't connect to remote endpoint.");
        }
        // Execute query.
        final List<Statement> outputStatements = new ArrayList<>(queries.size() * 2);

        faultTolerance.execute(remote, new FaultTolerance.ConnectionAction() {

            @Override
            public void action(RepositoryConnection connection) throws Exception {
                outputStatements.clear();
                for (String queryAsString : queries) {
                    final GraphQuery query = connection.prepareGraphQuery(QueryLanguage.SPARQL, queryAsString);
                    final GraphQueryResult result = query.evaluate();
                    long counter = 0;
                    while (result.hasNext()) {
                        outputStatements.add(result.next());
                        // Print info.
                        ++counter;
                        if (counter % 100000 == 0) {
                            LOG.info("{} triples extracted", counter);
                        }
                    }
                }
            }
        });
        output.add(outputStatements);
        // Flush buffre.
        output.flushBuffer();
    }

}
