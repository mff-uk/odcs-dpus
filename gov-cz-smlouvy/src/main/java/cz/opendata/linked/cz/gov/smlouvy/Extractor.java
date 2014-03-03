package cz.opendata.linked.cz.gov.smlouvy;

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
import cz.cuni.mff.xrg.odcs.rdf.interfaces.RDFDataUnit;
import cz.cuni.mff.xrg.scraper.css_parser.utils.BannedException;
import cz.cuni.mff.xrg.scraper.css_parser.utils.Cache;

@AsExtractor
public class Extractor 
extends ConfigurableBase<ExtractorConfig> 
implements DPU, ConfigDialogProvider<ExtractorConfig> {

	/**
	 * DPU's configuration.
	 */

	private Logger logger = LoggerFactory.getLogger(DPU.class);

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
	public RDFDataUnit outSmlouvyMeta;	
	
	@OutputDataUnit(name = "Objednavky-Metadata")
	public RDFDataUnit outObjednavkyMeta;	

	@OutputDataUnit(name = "Plneni-Metadata")
	public RDFDataUnit outPlneniMeta;	

	public Extractor(){
		super(ExtractorConfig.class);
	}

	@Override
	public AbstractConfigDialog<ExtractorConfig> getConfigurationDialog() {		
		return new ExtractorDialog();
	}

	public void execute(DPUContext ctx) throws DPUException
	{
		Cache.setInterval(config.interval);
		Cache.setTimeout(config.timeout);
		Cache.setBaseDir(ctx.getUserDirectory() + "/cache/");
		Cache.logger = logger;
		Cache.rewriteCache = config.rewriteCache;
		Scraper_parser s = new Scraper_parser();
		s.logger = logger;
		s.ctx = ctx;
		s.smlouvy = outSmlouvy;
		s.objednavky = outObjednavky;
		s.plneni = outPlneni;
		s.smlouvy_roky = outSmlouvyRoky;
		s.objednavky_roky = outObjednavkyRoky;
		s.plneni_roky = outPlneniRoky;
		s.smlouvy_meta = outSmlouvyMeta;
		s.objednavky_meta = outObjednavkyMeta;
		s.plneni_meta = outPlneniMeta;

		java.util.Date date = new java.util.Date();
		long start = date.getTime();

		//Download

		try {
			URL init_smlouvy = new URL("https://portal.gov.cz/portal/rejstriky/data/10013/index.xml");
			URL init_objednavky = new URL("https://portal.gov.cz/portal/rejstriky/data/10014/index.xml");
			URL init_plneni = new URL("https://portal.gov.cz/portal/rejstriky/data/10015/index.xml");
			
			if (config.rewriteCache)
			{
				Path path_smlouvy = Paths.get(ctx.getUserDirectory().getAbsolutePath() + "/cache/portal.gov.cz/portal/rejstriky/data/10013/index.xml");
				Path path_objednavky = Paths.get(ctx.getUserDirectory().getAbsolutePath() + "/cache/portal.gov.cz/portal/rejstriky/data/10014/index.xml");
				Path path_plneni = Paths.get(ctx.getUserDirectory().getAbsolutePath() + "/cache/portal.gov.cz/portal/rejstriky/data/10015/index.xml");
				logger.info("Deleting " + path_smlouvy);
				Files.deleteIfExists(path_smlouvy);
				logger.info("Deleting " + path_objednavky);
				Files.deleteIfExists(path_objednavky);
				logger.info("Deleting " + path_plneni);
				Files.deleteIfExists(path_plneni);
			}
			
			try {
				s.parse(init_smlouvy, "init-s");
				s.parse(init_objednavky, "init-o");
				s.parse(init_plneni, "init-p");
			} catch (BannedException b) {
				logger.warn("Seems like we are banned for today");
			}
			
        	logger.info("Download done.");
		
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage());
			e.printStackTrace();
		} catch (InterruptedException e) {
			logger.error("Interrupted");
		}
		
		java.util.Date date2 = new java.util.Date();
		long end = date2.getTime();

		ctx.sendMessage(MessageType.INFO, "Processed " + s.numSmlouvy + " smlouvy from " + s.numSmlouvyRoks + " years.");
		ctx.sendMessage(MessageType.INFO, "Processed " + s.numObjednavky + " objednávky from " + s.numObjednavkyRoks + " years.");
		ctx.sendMessage(MessageType.INFO, "Processed " + s.numPlneni + " plnění from " + s.numPlneniRoks + " years.");
		ctx.sendMessage(MessageType.INFO, "Processed in " + (end-start) + "ms");

	}

	@Override
	public void cleanUp() {	}

}
