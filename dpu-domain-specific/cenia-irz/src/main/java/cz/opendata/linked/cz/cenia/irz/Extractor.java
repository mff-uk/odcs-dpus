package cz.opendata.linked.cz.cenia.irz;

import cz.cuni.mff.xrg.odcs.commons.data.DataUnitException;
import java.io.IOException;
import java.net.URL;

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
import cz.cuni.mff.xrg.odcs.rdf.WritableRDFDataUnit;
import cz.cuni.mff.xrg.uv.rdf.simple.AddPolicy;
import cz.cuni.mff.xrg.uv.rdf.simple.SimpleRdfWrite;
import cz.cuni.mff.xrg.scraper.css_parser.utils.Cache;
import cz.cuni.mff.xrg.uv.rdf.simple.SimpleRdfFactory;

@AsExtractor
public class Extractor 
extends ConfigurableBase<ExtractorConfig> 
implements DPU, ConfigDialogProvider<ExtractorConfig> {

	@OutputDataUnit(name = "output")
	public WritableRDFDataUnit output;

	private Logger LOG = LoggerFactory.getLogger(DPU.class);

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
		SimpleRdfWrite outputWrap = SimpleRdfFactory.create(output, ctx);
		outputWrap.setPolicy(AddPolicy.BUFFERED);
		
		// vytvorime si parser
		Cache.setInterval(config.getInterval());
		Cache.setTimeout(config.getTimeout());
		Cache.setBaseDir(ctx.getUserDirectory() + "/cache/");
		Cache.rewriteCache = config.isRewriteCache();
		Cache.logger = LOG;

		try {
			Cache.setTrustAllCerts();
		} catch (Exception e) {
			LOG.error("Unexpected error when setting trust to all certificates.",e );
		}
		
		Parser s = new Parser();
		s.logger = LOG;
		s.ctx = ctx;
		s.outputDataUnit = outputWrap;
		s.valueFactory = outputWrap.getValueFactory();

		LOG.info("Starting extraction.");
		
		try {
			try {
				java.util.Date date = new java.util.Date();
				long start = date.getTime();
				
				for (int i = config.getStartYear(); i <= config.getEndYear(); i++)
				{
					if (ctx.canceled()) break;
					s.parse(new URL("http://portal.cenia.cz/irz/unikyPrenosy.jsp?rok=" + i + "&unikyPrenosyVyhledatVse=1"), "list");
				}
				
				ctx.sendMessage(MessageType.INFO, "Bad PSCs: " + s.badPostals);
				ctx.sendMessage(MessageType.INFO, "Switched GPS coordinats: " + s.switchedGPS);
				java.util.Date date2 = new java.util.Date();
				long end = date2.getTime();
				
				ctx.sendMessage(MessageType.INFO, "Processed in " + (end-start) + "ms");
			}
			catch (IOException e) {
				LOG.error("IOException", e);
			}
        	
			outputWrap.flushBuffer();
			
			LOG.info("Parsing done.");
		} catch (InterruptedException intex) {
			LOG.error("Interrupted");
		}

	}

}
