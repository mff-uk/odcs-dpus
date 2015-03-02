package cz.opendata.linked.cz.gov.organy;

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
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;
import eu.unifiedviews.helpers.dpu.extension.files.simple.WritableSimpleFiles;

@DPU.AsExtractor
public class Extractor 
extends AbstractDpu<ExtractorConfig> 
{

    private static final Logger LOG = LoggerFactory.getLogger(Extractor.class);

    @DataUnit.AsOutput(name = "XMLList")
    public WritableFilesDataUnit outList;
    
    @DataUnit.AsOutput(name = "XMLDetails")
    public WritableFilesDataUnit outDetails;
    
    @ExtensionInitializer.Init
    public FaultTolerance faultTolerance;
    
    @ExtensionInitializer.Init(param = "outList")
    public WritableSimpleFiles outListSimple;
    
    @ExtensionInitializer.Init(param = "outDetails")
    public WritableSimpleFiles outDetailsSimple;
    
    public Extractor(){
        super(ExtractorDialog.class,ConfigHistory.noHistory(ExtractorConfig.class));
    }

    @Override
    protected void innerExecute() throws DPUException
    {
    	Cache.setInterval(config.getInterval());
        Cache.setTimeout(config.getTimeout());
        Cache.setBaseDir(ctx.getExecMasterContext().getDpuContext().getUserDirectory() + "/cache/");
        Cache.logger = LOG;
        Cache.rewriteCache = config.isRewriteCache();
        Scraper_parser s = new Scraper_parser();
        s.logger = LOG;
        s.context = ctx.getExecMasterContext().getDpuContext();
        s.list = outListSimple;
        s.details = outDetailsSimple;
        s.faultTolerance = faultTolerance;

        java.util.Date date = new java.util.Date();
        long start = date.getTime();

        //Download
        
        try {
            URL init = new URL("http://seznam.gov.cz/ovm/datafile.do?format=xml&service=seznamovm");
            
            if (config.isRewriteCache())
            {
                Path path = Paths.get(ctx.getExecMasterContext().getDpuContext().getUserDirectory().getAbsolutePath() + "/cache/seznam.gov.cz/ovm/datafile.do@format=xml&service=seznamovm");
                LOG.info("Deleting " + path);
                Files.deleteIfExists(path);
            }
            
            try {
                s.parse(init, "init");
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

        ContextUtils.sendShortInfo(ctx, "Processed in {0} ms", (end-start));
    }

}
