package cz.opendata.linked.mzcr.prices;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.openrdf.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.css_parser.utils.Cache;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPU;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUContext;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUException;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.AsExtractor;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.InputDataUnit;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.OutputDataUnit;
import cz.cuni.mff.xrg.odcs.commons.module.dpu.ConfigurableBase;
import cz.cuni.mff.xrg.odcs.commons.web.AbstractConfigDialog;
import cz.cuni.mff.xrg.odcs.commons.web.ConfigDialogProvider;
import cz.cuni.mff.xrg.odcs.rdf.exceptions.RDFException;
import cz.cuni.mff.xrg.odcs.rdf.interfaces.RDFDataUnit;

@AsExtractor
public class Extractor 
extends ConfigurableBase<ExtractorConfig> 
implements DPU, ConfigDialogProvider<ExtractorConfig> {

	/**
	 * DPU's configuration.
	 */

	@OutputDataUnit
	public RDFDataUnit outputDataUnit;
	
	@InputDataUnit
	public RDFDataUnit inputDataUnit;

	private Logger logger = LoggerFactory.getLogger(DPU.class);

	public Extractor(){
		super(ExtractorConfig.class);
	}

	@Override
	public AbstractConfigDialog<ExtractorConfig> getConfigurationDialog() {		
		return new ExtractorDialog();
	}

	public void execute(DPUContext ctx) throws DPUException
	{
		// vytvorime si parser
		Cache.setInterval(config.interval);
		Cache.setTimeout(config.timeout);
		Cache.setBaseDir(ctx.getUserDirectory() + "/cache/");
		Cache.rewriteCache = config.rewriteCache;
		Cache.logger = logger;
		
		Parser s = new Parser();
		s.logger = logger;
		s.ctx = ctx;
		s.output = outputDataUnit;
		
		logger.info("Starting extraction.");
		
		int lines = 0;
		java.util.Date date = new java.util.Date();
		long start = date.getTime();
		
		if (inputDataUnit.getTripleCount() > 0)
		{
			List<Statement> statements = inputDataUnit.getTriples();
			
			int total = statements.size();
			
			URL notationPredicate = null;
			try {
				notationPredicate = new URL("http://www.w3.org/2004/02/skos/core#notation");
				for (Statement stmt : statements)
				{
					if (ctx.canceled())
					{
						logger.error("Interrupted");
						break;
					}
					if (stmt.getPredicate().toString().equals(notationPredicate.toString()))
					{
						lines++;
						logger.debug("Parsing " + lines + "/" + total);
						s.parse(Cache.getDocument(new URL("http://mzcr.cz/LekyNehrazene.aspx?naz=" + stmt.getObject().stringValue()), 10000), "tab");
					}
				}
			} catch (MalformedURLException e) {
				logger.error("Unexpected malformed URL of ODCS textValue predicate");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		java.util.Date date2 = new java.util.Date();
		long end = date2.getTime();
		logger.info("Processed " + lines + " in " + (end-start) + "ms");

	}

	@Override
	public void cleanUp() {	}

}
