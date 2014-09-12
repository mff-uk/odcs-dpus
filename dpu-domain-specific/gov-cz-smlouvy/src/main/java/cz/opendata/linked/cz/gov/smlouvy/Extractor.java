package cz.opendata.linked.cz.gov.smlouvy;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.uv.boost.dpu.advanced.DpuAdvancedBase;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import cz.cuni.mff.xrg.uv.boost.dpu.config.MasterConfigObject;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;
import cz.cuni.mff.xrg.odcs.dataunit.file.FileDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.AddPolicy;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.SimpleRdfWrite;
import cz.cuni.mff.xrg.scraper.css_parser.utils.BannedException;
import cz.cuni.mff.xrg.scraper.css_parser.utils.Cache;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.SimpleRdfFactory;

@DPU.AsExtractor
public class Extractor 
extends DpuAdvancedBase<ExtractorConfig> 
{

    private static final Logger LOG = LoggerFactory.getLogger(Extractor.class);

    @DataUnit.AsOutput(name = "XMLSmlouvy")
    public FileDataUnit outSmlouvy;    
    
    @DataUnit.AsOutput(name = "XMLObjednavky")
    public FileDataUnit outObjednavky;    

    @DataUnit.AsOutput(name = "XMLPlneni")
    public FileDataUnit outPlneni;    

    @DataUnit.AsOutput(name = "XMLSmlouvy-RocniSeznam")
    public FileDataUnit outSmlouvyRoky;    
    
    @DataUnit.AsOutput(name = "XMLObjednavky-RocniSeznam")
    public FileDataUnit outObjednavkyRoky;    

    @DataUnit.AsOutput(name = "XMLPlneni-RocniSeznam")
    public FileDataUnit outPlneniRoky;    

    @DataUnit.AsOutput(name = "Smlouvy-Metadata")
    public WritableRDFDataUnit outSmlouvyMeta;    
    
    @DataUnit.AsOutput(name = "Objednavky-Metadata")
    public WritableRDFDataUnit outObjednavkyMeta;    

    @DataUnit.AsOutput(name = "Plneni-Metadata")
    public WritableRDFDataUnit outPlneniMeta;    

    public Extractor(){
        super(ExtractorConfig.class,AddonInitializer.noAddons());
    }

    @Override
    public AbstractConfigDialog<MasterConfigObject> getConfigurationDialog() {        
        return new ExtractorDialog();
    }

    @Override
    protected void innerExecute() throws DPUException, DataUnitException
    {
        final SimpleRdfWrite outSmlouvyMetaWrap = SimpleRdfFactory.create(outSmlouvyMeta, context);
        outSmlouvyMetaWrap.setPolicy(AddPolicy.BUFFERED);
        
        final SimpleRdfWrite outObjednavkyMetaWrap = SimpleRdfFactory.create(outObjednavkyMeta, context);
        outObjednavkyMetaWrap.setPolicy(AddPolicy.BUFFERED);
        
        final SimpleRdfWrite outPlneniMetaWrap = SimpleRdfFactory.create(outPlneniMeta, context);
        outPlneniMetaWrap.setPolicy(AddPolicy.BUFFERED);
        
        Cache.setInterval(config.getInterval());
        Cache.setTimeout(config.getTimeout());
        Cache.setBaseDir(context.getUserDirectory() + "/cache/");
        Cache.logger = LOG;
        Cache.rewriteCache = config.isRewriteCache();
        Scraper_parser s = new Scraper_parser();
        s.logger = LOG;
        s.context = context;
        s.smlouvy = outSmlouvy;
        s.objednavky = outObjednavky;
        s.plneni = outPlneni;
        s.smlouvy_roky = outSmlouvyRoky;
        s.objednavky_roky = outObjednavkyRoky;
        s.plneni_roky = outPlneniRoky;
        s.smlouvy_meta = outSmlouvyMetaWrap;
        s.objednavky_meta = outObjednavkyMetaWrap;
        s.plneni_meta = outPlneniMetaWrap;

        outSmlouvyMetaWrap.flushBuffer();
        outObjednavkyMetaWrap.flushBuffer();
        outPlneniMetaWrap.flushBuffer();
        
        java.util.Date date = new java.util.Date();
        long start = date.getTime();

        //Download
        try {
            URL init_smlouvy = new URL("https://portal.gov.cz/portal/rejstriky/data/10013/index.xml");
            URL init_objednavky = new URL("https://portal.gov.cz/portal/rejstriky/data/10014/index.xml");
            URL init_plneni = new URL("https://portal.gov.cz/portal/rejstriky/data/10015/index.xml");
            
            if (config.isRewriteCache())
            {
                if (config.isSmlouvy()) {
                    Path path_smlouvy = Paths.get(context.getUserDirectory().getAbsolutePath() + "/cache/portal.gov.cz/portal/rejstriky/data/10013/index.xml");
                    LOG.info("Deleting " + path_smlouvy);
                    Files.deleteIfExists(path_smlouvy);
                }
                if (config.isObjednavky()) {
                    Path path_objednavky = Paths.get(context.getUserDirectory().getAbsolutePath() + "/cache/portal.gov.cz/portal/rejstriky/data/10014/index.xml");
                    LOG.info("Deleting " + path_objednavky);
                    Files.deleteIfExists(path_objednavky);
                }
                if (config.isPlneni()) {
                    Path path_plneni = Paths.get(context.getUserDirectory().getAbsolutePath() + "/cache/portal.gov.cz/portal/rejstriky/data/10015/index.xml");
                    LOG.info("Deleting " + path_plneni);
                    Files.deleteIfExists(path_plneni);
                }
            }
            
            try {
                if (config.isSmlouvy()) s.parse(init_smlouvy, "init-s");
                if (config.isObjednavky()) s.parse(init_objednavky, "init-o");
                if (config.isPlneni()) s.parse(init_plneni, "init-p");
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

        context.sendMessage(DPUContext.MessageType.INFO, "Processed " + s.numSmlouvy + " smlouvy from " + s.numSmlouvyRoks + " years.");
        context.sendMessage(DPUContext.MessageType.INFO, "Processed " + s.numObjednavky + " objednávky from " + s.numObjednavkyRoks + " years.");
        context.sendMessage(DPUContext.MessageType.INFO, "Processed " + s.numPlneni + " plnění from " + s.numPlneniRoks + " years.");
        context.sendMessage(DPUContext.MessageType.INFO, "Processed in " + (end-start) + "ms");

    }

}
