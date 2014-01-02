package cz.opendata.linked.cz.gov.organy;

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

	@OutputDataUnit(name = "XMLList")
	public RDFDataUnit outList;

	@OutputDataUnit(name = "XMLDetails")
	public RDFDataUnit outDetails;	
	
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
		s.list = outList;
		s.details = outDetails;

		java.util.Date date = new java.util.Date();
		long start = date.getTime();

		//Download

		try {
			URL init = new URL("http://seznam.gov.cz/ovm/datafile.do?format=xml&service=seznamovm");
			
			if (config.rewriteCache)
			{
				Path path = Paths.get(ctx.getUserDirectory().getAbsolutePath() + "/cache/seznam.gov.cz/ovm/datafile.do@format=xml&service=seznamovm");
				logger.info("Deleting " + path);
				Files.deleteIfExists(path);
			}
			
			try {
				s.parse(init, "init");
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

		ctx.sendMessage(MessageType.INFO, "Processed in " + (end-start) + "ms");

	}

	@Override
	public void cleanUp() {	}

}
