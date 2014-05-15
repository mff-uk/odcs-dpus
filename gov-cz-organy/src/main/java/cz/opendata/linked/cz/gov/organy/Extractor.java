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
import cz.cuni.mff.xrg.odcs.rdf.RDFDataUnit;
import cz.cuni.mff.xrg.odcs.rdf.WritableRDFDataUnit;
import cz.cuni.mff.xrg.odcs.rdf.simple.SimpleRdfRead;
import cz.cuni.mff.xrg.odcs.rdf.simple.SimpleRdfWrite;
import cz.cuni.mff.xrg.scraper.css_parser.utils.BannedException;
import cz.cuni.mff.xrg.scraper.css_parser.utils.Cache;

@AsExtractor
public class Extractor 
extends ConfigurableBase<ExtractorConfig> 
implements DPU, ConfigDialogProvider<ExtractorConfig> {

	private static final Logger LOG = LoggerFactory.getLogger(Extractor.class);

	@OutputDataUnit(name = "XMLList")
	public WritableRDFDataUnit outList;

	@OutputDataUnit(name = "XMLDetails")
	public WritableRDFDataUnit outDetails;	
	
	public Extractor(){
		super(ExtractorConfig.class);
	}

	@Override
	public AbstractConfigDialog<ExtractorConfig> getConfigurationDialog() {		
		return new ExtractorDialog();
	}

	@Override
	public void execute(DPUContext ctx) throws DPUException
	{
		final SimpleRdfWrite outListWrap = new SimpleRdfWrite(outList, ctx);
		final SimpleRdfWrite outDetailsWrap = new SimpleRdfWrite(outDetails, ctx);
		
		Cache.setInterval(config.getInterval());
		Cache.setTimeout(config.getTimeout());
		Cache.setBaseDir(ctx.getUserDirectory() + "/cache/");
		Cache.logger = LOG;
		Cache.rewriteCache = config.isRewriteCache();
		Scraper_parser s = new Scraper_parser();
		s.logger = LOG;
		s.ctx = ctx;
		s.list = outListWrap;
		s.details = outDetailsWrap;

		java.util.Date date = new java.util.Date();
		long start = date.getTime();

		//Download

		try {
			URL init = new URL("http://seznam.gov.cz/ovm/datafile.do?format=xml&service=seznamovm");
			
			if (config.isRewriteCache())
			{
				Path path = Paths.get(ctx.getUserDirectory().getAbsolutePath() + "/cache/seznam.gov.cz/ovm/datafile.do@format=xml&service=seznamovm");
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

		ctx.sendMessage(MessageType.INFO, "Processed in " + (end-start) + "ms");
	}

}
