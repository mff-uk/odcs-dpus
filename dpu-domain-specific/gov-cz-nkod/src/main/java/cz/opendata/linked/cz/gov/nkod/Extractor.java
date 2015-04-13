package cz.opendata.linked.cz.gov.nkod;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.scraper.css_parser.utils.BannedException;
import cz.cuni.mff.xrg.scraper.css_parser.utils.Cache;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;
import eu.unifiedviews.helpers.dpu.extension.files.simple.WritableSimpleFiles;
import eu.unifiedviews.helpers.dpu.extension.rdf.simple.WritableSimpleRdf;

@DPU.AsExtractor
public class Extractor extends AbstractDpu<ExtractorConfig> {

    private static final Logger LOG = LoggerFactory.getLogger(Extractor.class);

    @DataUnit.AsOutput(name = "XMLNkod")
    public WritableFilesDataUnit outNkodFiles;

    @DataUnit.AsOutput(name = "XMLNkod-RocniSeznam")
    public WritableFilesDataUnit outNkodRokyFiles;

    @DataUnit.AsOutput(name = "Metadata")
    public WritableRDFDataUnit outRdfMetadata;

    @ExtensionInitializer.Init(param = "outRdfMetadata")
    public WritableSimpleRdf outMetadata;

    @ExtensionInitializer.Init(param = "outNkodFiles")
    public WritableSimpleFiles outNkod;

    @ExtensionInitializer.Init(param = "outNkodRokyFiles")
    public WritableSimpleFiles outNkodRoky;

    @ExtensionInitializer.Init
    public FaultTolerance faultTolerance;

    public Extractor() {
        super(ExtractorDialog.class, ConfigHistory.noHistory(ExtractorConfig.class));
    }

    @Override
    protected void innerExecute() throws DPUException {
        Cache.setInterval(config.getInterval());
        Cache.setTimeout(config.getTimeout());
        Cache.setBaseDir(ctx.getExecMasterContext().getDpuContext().getUserDirectory() + "/cache/");
        Cache.logger = LOG;
        Cache.rewriteCache = config.isRewriteCache();
        Scraper_parser s = new Scraper_parser();
        s.logger = LOG;
        s.context = ctx.getExecMasterContext().getDpuContext();
        s.nkod = outNkod;
        s.nkod_roky = outNkodRoky;
        s.metadata = outMetadata;

        java.util.Date date = new java.util.Date();
        long start = date.getTime();

        DPUContext context = ctx.getExecMasterContext().getDpuContext();
        //Download
        try {
            URL init_nkod = new URL("http://portal.gov.cz/portal/rejstriky/data/97898/index.xml");

            if (config.isRewriteCache()) {
                Path path_nkod = Paths.get(
                        context.getUserDirectory().getAbsolutePath() + "/cache/portal.gov.cz/portal/rejstriky/data/97898/index.xml");
                LOG.info("Deleting " + path_nkod);
                Files.deleteIfExists(path_nkod);
            }

            try {
                s.parse(init_nkod, "init-s");
            } catch (BannedException b) {
                LOG.warn("Seems like we are banned for today");
            }

            LOG.info("Download done.");

        } catch (IOException e) {
            LOG.error("IOException", e);
        } catch (InterruptedException e) {
            LOG.error("Interrupted");
        }

        java.util.Date date2 = new java.util.Date();
        long end = date2.getTime();

        context.sendMessage(DPUContext.MessageType.INFO,
                "Processed " + s.numNkod + " nkod from " + s.numNkodRoks + " years.");
        context.sendMessage(DPUContext.MessageType.INFO, "Processed in " + (end - start) + "ms");

    }

}
