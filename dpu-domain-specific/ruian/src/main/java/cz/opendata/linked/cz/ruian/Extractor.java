package cz.opendata.linked.cz.ruian;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;
import eu.unifiedviews.helpers.dpu.extension.files.simple.WritableSimpleFiles;
import cz.cuni.mff.xrg.scraper.css_parser.utils.BannedException;
import cz.cuni.mff.xrg.scraper.css_parser.utils.Cache;

@DPU.AsExtractor
public class Extractor 
extends AbstractDpu<ExtractorConfig> 
{

    private static final Logger LOG = LoggerFactory.getLogger(Extractor.class);

    @DataUnit.AsOutput(name = "XMLObce")
    public WritableFilesDataUnit outObce;    
    
    @DataUnit.AsOutput(name = "XMLZsj")
    public WritableFilesDataUnit outZsj;    

    @ExtensionInitializer.Init
    public FaultTolerance faultTolerance;
    
    @ExtensionInitializer.Init(param = "outObce")
    public WritableSimpleFiles outObceSimple;
    
    @ExtensionInitializer.Init(param = "outZsj")
    public WritableSimpleFiles outZsjSimple;

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
        //Cache.rewriteCache = config.rewriteCache;
        Scraper_parser s = new Scraper_parser();
        s.logger = LOG;
        s.context = ctx.getExecMasterContext().getDpuContext();
        s.obce = outObceSimple;
        s.zsj = outZsjSimple;
        s.outputFiles = config.isPassToOutput();

        java.util.Date date = new java.util.Date();
        long start = date.getTime();

        //Download

        try {
            URL init, initStat;
            if (config.isInclGeoData())
            {
                init = new URL("http://vdp.cuzk.cz/vdp/ruian/vymennyformat/seznamlinku?vf.pu=S&_vf.pu=on&_vf.pu=on&vf.cr=U&vf.up=OB&vf.ds=K&vf.vu=Z&_vf.vu=on&_vf.vu=on&_vf.vu=on&_vf.vu=on&vf.uo=A&search=Vyhledat");
                initStat = new URL("http://vdp.cuzk.cz/vdp/ruian/vymennyformat/seznamlinku?vf.pu=S&_vf.pu=on&_vf.pu=on&vf.cr=U&vf.up=ST&vf.ds=K&vf.vu=Z&_vf.vu=on&vf.vu=G&_vf.vu=on&vf.vu=H&_vf.vu=on&_vf.vu=on&search=Vyhledat");
            }
            else
            {
                init = new URL("http://vdp.cuzk.cz/vdp/ruian/vymennyformat/seznamlinku?vf.pu=S&_vf.pu=on&_vf.pu=on&vf.cr=U&vf.up=OB&vf.ds=Z&vf.vu=Z&_vf.vu=on&_vf.vu=on&_vf.vu=on&_vf.vu=on&vf.uo=A&search=Vyhledat");
                initStat = new URL("http://vdp.cuzk.cz/vdp/ruian/vymennyformat/seznamlinku?vf.pu=S&_vf.pu=on&_vf.pu=on&vf.cr=U&vf.up=ST&vf.ds=Z&vf.vu=Z&_vf.vu=on&_vf.vu=on&_vf.vu=on&_vf.vu=on&search=Vyhledat");
            }
            if (config.isRewriteCache())
            {
                Path path, pathStat;
                if (config.isInclGeoData())
                {
                    path = Paths.get(ctx.getExecMasterContext().getDpuContext().getUserDirectory().getAbsolutePath() + "/cache/vdp.cuzk.cz/vdp/ruian/vymennyformat/seznamlinku?vf.pu=S&_vf.pu=on&_vf.pu=on&vf.cr=U&vf.up=OB&vf.ds=K&vf.vu=Z&_vf.vu=on&_vf.vu=on&_vf.vu=on&_vf.vu=on&vf.uo=A&search=Vyhledat");
                    pathStat = Paths.get(ctx.getExecMasterContext().getDpuContext().getUserDirectory().getAbsolutePath() + "/cache/vdp.cuzk.cz/vdp/ruian/vymennyformat/seznamlinku?vf.pu=S&_vf.pu=on&_vf.pu=on&vf.cr=U&vf.up=ST&vf.ds=K&vf.vu=Z&_vf.vu=on&vf.vu=G&_vf.vu=on&vf.vu=H&_vf.vu=on&_vf.vu=on&search=Vyhledat");
                }
                else
                {
                    path = Paths.get(ctx.getExecMasterContext().getDpuContext().getUserDirectory().getAbsolutePath() + "/cache/vdp.cuzk.cz/vdp/ruian/vymennyformat/seznamlinku@vf.pu=S&_vf.pu=on&_vf.pu=on&vf.cr=U&vf.up=OB&vf.ds=Z&vf.vu=Z&_vf.vu=on&_vf.vu=on&_vf.vu=on&_vf.vu=on&vf.uo=A&search=Vyhledat");
                    pathStat = Paths.get(ctx.getExecMasterContext().getDpuContext().getUserDirectory().getAbsolutePath() + "/cache/vdp.cuzk.cz/vdp/ruian/vymennyformat/seznamlinku@vf.pu=S&_vf.pu=on&_vf.pu=on&vf.cr=U&vf.up=ST&vf.ds=Z&vf.vu=Z&_vf.vu=on&_vf.vu=on&_vf.vu=on&_vf.vu=on&search=Vyhledat");
                }
                LOG.info("Deleting " + path);
                Files.deleteIfExists(path);
                LOG.info("Deleting " + pathStat);
                Files.deleteIfExists(pathStat);                
            }
            
            try {
                s.parse(init, "init");
                s.parse(initStat, "initStat");
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
