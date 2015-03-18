package cz.opendata.linked.mzcr.prices;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.css_parser.utils.Cache;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.helpers.dataunit.DataUnitUtils;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;
import eu.unifiedviews.helpers.dpu.extension.rdf.simple.WritableSimpleRdf;
import eu.unifiedviews.helpers.dpu.rdf.sparql.SparqlUtils;

@DPU.AsExtractor
public class Extractor extends AbstractDpu<ExtractorConfig> {

    private static final Logger LOG = LoggerFactory.getLogger(Extractor.class);

    @DataUnit.AsOutput(name = "output")
    public WritableRDFDataUnit outputDataUnit;

    @ExtensionInitializer.Init(param = "outputDataUnit")
    public WritableSimpleRdf outputWrap;

    @DataUnit.AsInput(name = "input")
    public RDFDataUnit inputDataUnit;

    @ExtensionInitializer.Init
    public FaultTolerance faultTolerance;

    public Extractor() {
        super(ExtractorDialog.class, ConfigHistory.noHistory(ExtractorConfig.class));
    }

    @Override
    protected void innerExecute() throws DPUException {
        // vytvorime si parser
        Cache.setInterval(config.getInterval());
        Cache.setTimeout(config.getTimeout());
        Cache.setBaseDir(ctx.getExecMasterContext().getDpuContext().getUserDirectory() + "/cache/");
        Cache.rewriteCache = config.isRewriteCache();
        Cache.logger = LOG;

        Parser s = new Parser();
        s.logger = LOG;
        s.context = ctx.getExecMasterContext().getDpuContext();
        s.output = outputWrap;

        LOG.info("Starting extraction.");

        int lines = 0;
        java.util.Date date = new java.util.Date();
        long start = date.getTime();

        // Get content of input.
        final SparqlUtils.QueryResultCollector collector = new SparqlUtils.QueryResultCollector();
        faultTolerance.execute(inputDataUnit, new FaultTolerance.ConnectionAction() {

            @Override
            public void action(RepositoryConnection connection) throws Exception {
                final SparqlUtils.SparqlSelectObject select
                        = SparqlUtils.createSelect("SELECT ?s ?p ?o WHERE { ?s ?p ?o }",
                                DataUnitUtils.getEntries(inputDataUnit, RDFDataUnit.Entry.class));
                SparqlUtils.execute(connection, ctx, select, collector);
            }
        });
        // Store as statements - minimal DPU change approach.
        final String notationPredicateStr = "http://www.w3.org/2004/02/skos/core#notation";
        int counter = 0;
        for (Map<String, Value> tuple : collector.getResults()) {
            if (ctx.canceled()) {
                throw ContextUtils.dpuExceptionCancelled(ctx);
            }
            if (tuple.get("p").stringValue().equals(notationPredicateStr)) {
                LOG.info("Processing {}/{}", ++counter, collector.getResults().size());

                try {
                    s.parse(Cache.getDocument(new URL("http://mzcr.cz/LekyNehrazene.aspx?naz="
                            + tuple.get("o").stringValue()), 10000), "tab");
                } catch (MalformedURLException ex) {
                    LOG.error("Unexpected malformed URL of ODCS textValue predicate", ex);
                } catch (IOException ex) {
                    LOG.error("IOException", ex);
                } catch (InterruptedException ex) {

                }
            }
        }

        outputWrap.flushBuffer();

        java.util.Date date2 = new java.util.Date();
        long end = date2.getTime();
        LOG.info("Processed " + lines + " in " + (end - start) + "ms");

    }

}
