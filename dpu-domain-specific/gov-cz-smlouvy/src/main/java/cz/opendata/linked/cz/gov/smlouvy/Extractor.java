package cz.opendata.linked.cz.gov.smlouvy;

import cz.cuni.mff.xrg.odcs.commons.data.DataUnitException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.odcs.commons.dpu.DPU;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUContext;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUException;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.AsExtractor;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.OutputDataUnit;
import cz.cuni.mff.xrg.odcs.commons.message.MessageType;
import cz.cuni.mff.xrg.odcs.commons.module.dpu.ConfigurableBase;
import cz.cuni.mff.xrg.odcs.commons.web.AbstractConfigDialog;
import cz.cuni.mff.xrg.odcs.commons.web.ConfigDialogProvider;
import cz.cuni.mff.xrg.odcs.dataunit.file.FileDataUnit;
import cz.cuni.mff.xrg.odcs.rdf.WritableRDFDataUnit;
import cz.cuni.mff.xrg.uv.rdf.simple.AddPolicy;
import cz.cuni.mff.xrg.uv.rdf.simple.SimpleRdfWrite;
import cz.cuni.mff.xrg.scraper.css_parser.utils.BannedException;
import cz.cuni.mff.xrg.scraper.css_parser.utils.Cache;
import cz.cuni.mff.xrg.uv.rdf.simple.SimpleRdfFactory;

@AsExtractor
public class Extractor 
extends ConfigurableBase<ExtractorConfig> 
implements DPU, ConfigDialogProvider<ExtractorConfig> {

	private static final Logger LOG = LoggerFactory.getLogger(Extractor.class);

	@OutputDataUnit(name = "XMLSmlouvy")
	public FileDataUnit outSmlouvy;	
	
	@OutputDataUnit(name = "XMLObjednavky")
	public FileDataUnit outObjednavky;	

	@OutputDataUnit(name = "XMLPlneni")
	public FileDataUnit outPlneni;	

	@OutputDataUnit(name = "XMLSmlouvy-RocniSeznam")
	public FileDataUnit outSmlouvyRoky;	
	
	@OutputDataUnit(name = "XMLObjednavky-RocniSeznam")
	public FileDataUnit outObjednavkyRoky;	

	@OutputDataUnit(name = "XMLPlneni-RocniSeznam")
	public FileDataUnit outPlneniRoky;	

	@OutputDataUnit(name = "Smlouvy-Metadata")
	public WritableRDFDataUnit outSmlouvyMeta;	
	
	@OutputDataUnit(name = "Objednavky-Metadata")
	public WritableRDFDataUnit outObjednavkyMeta;	

	@OutputDataUnit(name = "Plneni-Metadata")
	public WritableRDFDataUnit outPlneniMeta;	

	public Extractor(){
		super(ExtractorConfig.class);
	}

	@Override
	public AbstractConfigDialog<ExtractorConfig> getConfigurationDialog() {		
		return new ExtractorDialog();
	}

	@Override
	public void execute(DPUContext ctx) throws DPUException, DataUnitException
	{
		final SimpleRdfWrite outSmlouvyMetaWrap = SimpleRdfFactory.create(outSmlouvyMeta, ctx);
		outSmlouvyMetaWrap.setPolicy(AddPolicy.BUFFERED);
		
		final SimpleRdfWrite outObjednavkyMetaWrap = SimpleRdfFactory.create(outObjednavkyMeta, ctx);
		outObjednavkyMetaWrap.setPolicy(AddPolicy.BUFFERED);
		
		final SimpleRdfWrite outPlneniMetaWrap = SimpleRdfFactory.create(outPlneniMeta, ctx);
		outPlneniMetaWrap.setPolicy(AddPolicy.BUFFERED);
		
		Cache.setInterval(config.getInterval());
		Cache.setTimeout(config.getTimeout());
		Cache.setBaseDir(ctx.getUserDirectory() + "/cache/");
		Cache.logger = LOG;
		Cache.rewriteCache = config.isRewriteCache();
		Scraper_parser s = new Scraper_parser();
		s.logger = LOG;
		s.ctx = ctx;
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
					Path path_smlouvy = Paths.get(ctx.getUserDirectory().getAbsolutePath() + "/cache/portal.gov.cz/portal/rejstriky/data/10013/index.xml");
					LOG.info("Deleting " + path_smlouvy);
					Files.deleteIfExists(path_smlouvy);
				}
				if (config.isObjednavky()) {
					Path path_objednavky = Paths.get(ctx.getUserDirectory().getAbsolutePath() + "/cache/portal.gov.cz/portal/rejstriky/data/10014/index.xml");
					LOG.info("Deleting " + path_objednavky);
					Files.deleteIfExists(path_objednavky);
				}
				if (config.isPlneni()) {
					Path path_plneni = Paths.get(ctx.getUserDirectory().getAbsolutePath() + "/cache/portal.gov.cz/portal/rejstriky/data/10015/index.xml");
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

		ctx.sendMessage(MessageType.INFO, "Processed " + s.numSmlouvy + " smlouvy from " + s.numSmlouvyRoks + " years.");
		ctx.sendMessage(MessageType.INFO, "Processed " + s.numObjednavky + " objednávky from " + s.numObjednavkyRoks + " years.");
		ctx.sendMessage(MessageType.INFO, "Processed " + s.numPlneni + " plnění from " + s.numPlneniRoks + " years.");
		ctx.sendMessage(MessageType.INFO, "Processed in " + (end-start) + "ms");

	}

}
