package cz.opendata.linked.cz.gov.smlouvy;

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

    @DataUnit.AsOutput(name = "XMLSmlouvy")
    public WritableFilesDataUnit outSmlouvyFiles;

    @DataUnit.AsOutput(name = "XMLObjednavky")
    public WritableFilesDataUnit outObjednavkyFiles;

    @DataUnit.AsOutput(name = "XMLPlneni")
    public WritableFilesDataUnit outPlneniFiles;

    @DataUnit.AsOutput(name = "XMLSmlouvy-RocniSeznam")
    public WritableFilesDataUnit outSmlouvyRokyFiles;

    @DataUnit.AsOutput(name = "XMLObjednavky-RocniSeznam")
    public WritableFilesDataUnit outObjednavkyRokyFiles;

    @DataUnit.AsOutput(name = "XMLPlneni-RocniSeznam")
    public WritableFilesDataUnit outPlneniRokyFiles;

    @DataUnit.AsOutput(name = "Metadata")
    public WritableRDFDataUnit outRdfMetadata;

    @ExtensionInitializer.Init(param = "outRdfMetadata")
    public WritableSimpleRdf outMetadata;

    @ExtensionInitializer.Init(param = "outSmlouvyFiles")
    public WritableSimpleFiles outSmlouvy;

    @ExtensionInitializer.Init(param = "outObjednavkyFiles")
    public WritableSimpleFiles outObjednavky;

    @ExtensionInitializer.Init(param = "outPlneniFiles")
    public WritableSimpleFiles outPlneni;

    @ExtensionInitializer.Init(param = "outSmlouvyRokyFiles")
    public WritableSimpleFiles outSmlouvyRoky;

    @ExtensionInitializer.Init(param = "outObjednavkyRokyFiles")
    public WritableSimpleFiles outObjednavkyRoky;

    @ExtensionInitializer.Init(param = "outPlneniRokyFiles")
    public WritableSimpleFiles outPlneniRoky;

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
        s.smlouvy = outSmlouvy;
        s.objednavky = outObjednavky;
        s.plneni = outPlneni;
        s.smlouvy_roky = outSmlouvyRoky;
        s.objednavky_roky = outObjednavkyRoky;
        s.plneni_roky = outPlneniRoky;
        s.metadata = outMetadata;

        java.util.Date date = new java.util.Date();
        long start = date.getTime();

        DPUContext context = ctx.getExecMasterContext().getDpuContext();
        //Download
        try {
            URL init_smlouvy = new URL("https://portal.gov.cz/portal/rejstriky/data/10013/index.xml");
            URL init_objednavky = new URL("https://portal.gov.cz/portal/rejstriky/data/10014/index.xml");
            URL init_plneni = new URL("https://portal.gov.cz/portal/rejstriky/data/10015/index.xml");

            if (config.isRewriteCache()) {
                if (config.isSmlouvy()) {
                    Path path_smlouvy = Paths.get(
                            context.getUserDirectory().getAbsolutePath() + "/cache/portal.gov.cz/portal/rejstriky/data/10013/index.xml");
                    LOG.info("Deleting " + path_smlouvy);
                    Files.deleteIfExists(path_smlouvy);
                }
                if (config.isObjednavky()) {
                    Path path_objednavky = Paths.get(
                            context.getUserDirectory().getAbsolutePath() + "/cache/portal.gov.cz/portal/rejstriky/data/10014/index.xml");
                    LOG.info("Deleting " + path_objednavky);
                    Files.deleteIfExists(path_objednavky);
                }
                if (config.isPlneni()) {
                    Path path_plneni = Paths.get(
                            context.getUserDirectory().getAbsolutePath() + "/cache/portal.gov.cz/portal/rejstriky/data/10015/index.xml");
                    LOG.info("Deleting " + path_plneni);
                    Files.deleteIfExists(path_plneni);
                }
            }

            try {
                if (config.isSmlouvy()) {
                    s.parse(init_smlouvy, "init-s");
                }
                if (config.isObjednavky()) {
                    s.parse(init_objednavky, "init-o");
                }
                if (config.isPlneni()) {
                    s.parse(init_plneni, "init-p");
                }
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
                "Processed " + s.numSmlouvy + " smlouvy from " + s.numSmlouvyRoks + " years.");
        context.sendMessage(DPUContext.MessageType.INFO,
                "Processed " + s.numObjednavky + " objednávky from " + s.numObjednavkyRoks + " years.");
        context.sendMessage(DPUContext.MessageType.INFO,
                "Processed " + s.numPlneni + " plnění from " + s.numPlneniRoks + " years.");
        context.sendMessage(DPUContext.MessageType.INFO, "Processed in " + (end - start) + "ms");

    }

}
