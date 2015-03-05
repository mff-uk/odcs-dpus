package cz.opendata.linked.ares.updates;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import cz.cuni.mff.xrg.scraper.css_parser.utils.BannedException;
import cz.cuni.mff.xrg.scraper.css_parser.utils.Cache;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;
import eu.unifiedviews.helpers.dpu.extension.files.simple.WritableSimpleFiles;

@DPU.AsExtractor
public class Extractor extends AbstractDpu<ExtractorConfig> {

    private final static Logger LOG = LoggerFactory.getLogger(DPU.class);

    @DataUnit.AsOutput(name = "BEs")
    public WritableFilesDataUnit filesBEs;

    @ExtensionInitializer.Init(param = "filesBEs")
    public WritableSimpleFiles BEs;

    @ExtensionInitializer.Init
    public FaultTolerance faultTolerance;

    public Extractor(){
        super(ExtractorDialog.class, ConfigHistory.noHistory(ExtractorConfig.class));
    }

    @Override
    protected void innerExecute() throws DPUException {
        DPUContext context = ctx.getExecMasterContext().getDpuContext();

        Cache.setInterval(config.getInterval());
        Cache.setTimeout(config.getTimeout());
        Cache.setBaseDir(context.getUserDirectory() + "/cache/");
        Cache.logger = LOG;
        Scraper_parser s = new Scraper_parser();
        s.logger = LOG;
        s.context = context;

        String ICfilename = context.getWorkingDir() + "/ic.ttl";
        try {
            s.ps = new PrintStream(ICfilename, "UTF-8");
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            LOG.error("Unexpected error opening filestreams for temp files", e);
        }

        String prefixes =
                "@prefix skos:       <http://www.w3.org/2004/02/skos/core#> .\n" +
                "@prefix gr:         <http://purl.org/goodrelations/v1#> .\n" +
                "@prefix adms:       <http://www.w3.org/ns/adms#> .\n" +
                    "\n" +
                "@prefix czbe:     <http://linked.opendata.cz/resource/business-entity/> .\n";        

        s.ps.println(prefixes);

        // a spustim na vychozi stranku

        java.util.Date date = new java.util.Date();
        long start = date.getTime();

        //Download

        try {
            URL init = new URL("http://wwwinfo.mfcr.cz/cgi-bin/ares/darv_zm.cgi?cislo_zdroje=2");
            
            Path path = Paths.get(context.getUserDirectory().getAbsolutePath() + "/cache/wwwinfo.mfcr.cz/cgi-bin/ares/darv_zm.cgi@cislo_zdroje=2");
            LOG.info("Deleting {}", path);
            Files.deleteIfExists(path);
            
            try {
                s.parse(init, "init");
            } catch (BannedException b) {
                LOG.warn("Seems like we are banned for today");
            }
            
            s.ps.close();

            LOG.info("Parsing done. Passing RDF to ODCS");
            
            //BEs.addExistingFile(ICfilename, new File(ICfilename).toURI().toString());
            BEs.add(new File(ICfilename), "ic.ttl"); // USE ic.ttl as fileName.


        } catch (IOException e) {
            LOG.error("IOException", e);
        } catch (InterruptedException e) {
            LOG.error("Interrupted");
            s.ps.close();
        }
        
        java.util.Date date2 = new java.util.Date();
        long end = date2.getTime();

        context.sendMessage(DPUContext.MessageType.INFO, "Processed in " + (end-start) + "ms");
    }

}
