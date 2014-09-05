package cz.opendata.linked.cz.gov.organy;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.uv.boost.dpu.advanced.DpuAdvancedBase;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.impl.SimpleRdfConfigurator;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import cz.cuni.mff.xrg.uv.boost.dpu.config.MasterConfigObject;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.SimpleRdfWrite;
import cz.cuni.mff.xrg.scraper.css_parser.utils.BannedException;
import cz.cuni.mff.xrg.scraper.css_parser.utils.Cache;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.SimpleRdfFactory;

@DPU.AsExtractor
public class Extractor 
extends DpuAdvancedBase<ExtractorConfig> 
{

    private static final Logger LOG = LoggerFactory.getLogger(Extractor.class);

//    @DataUnit.AsOutput(name = "XMLList")
//    public WritableRDFDataUnit outList;

//    @DataUnit.AsOutput(name = "XMLDetails")
//    public WritableRDFDataUnit outDetails; 
    
    @DataUnit.AsOutput(name = "XMLList")
    public WritableFilesDataUnit outList;
    
    @DataUnit.AsOutput(name = "XMLDetails")
    public WritableFilesDataUnit outDetails;
    
//    @SimpleRdfConfigurator.Configure(dataUnitFieldName="outList")
//    public SimpleRdfWrite outListWrap;
    
//    @SimpleRdfConfigurator.Configure(dataUnitFieldName="outDetails")
//    public SimpleRdfWrite outDetailsWrap;
    
    public Extractor(){
        super(ExtractorConfig.class,AddonInitializer.create(new SimpleRdfConfigurator(Extractor.class)));
    }

    @Override
    public AbstractConfigDialog<MasterConfigObject> getConfigurationDialog() {        
        return new ExtractorDialog();
    }

    @Override
    protected void innerExecute() throws DPUException
    {
        Cache.setInterval(config.getInterval());
        Cache.setTimeout(config.getTimeout());
        Cache.setBaseDir(context.getUserDirectory() + "/cache/");
        Cache.logger = LOG;
        Cache.rewriteCache = config.isRewriteCache();
        Scraper_parser s = new Scraper_parser();
        s.logger = LOG;
        s.context = context;
        s.list = outList;
        s.details = outDetails;

        java.util.Date date = new java.util.Date();
        long start = date.getTime();

        //Download
        
        try {
            URL init = new URL("http://seznam.gov.cz/ovm/datafile.do?format=xml&service=seznamovm");
            
            if (config.isRewriteCache())
            {
                Path path = Paths.get(context.getUserDirectory().getAbsolutePath() + "/cache/seznam.gov.cz/ovm/datafile.do@format=xml&service=seznamovm");
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

        context.sendMessage(DPUContext.MessageType.INFO, "Processed in " + (end-start) + "ms");
    }

}
