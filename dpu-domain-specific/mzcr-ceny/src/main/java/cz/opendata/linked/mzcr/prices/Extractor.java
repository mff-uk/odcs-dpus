package cz.opendata.linked.mzcr.prices;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.openrdf.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.css_parser.utils.Cache;
import cz.cuni.mff.xrg.odcs.commons.data.DataUnitException;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPU;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUContext;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUException;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.AsExtractor;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.InputDataUnit;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.OutputDataUnit;
import cz.cuni.mff.xrg.odcs.commons.module.dpu.ConfigurableBase;
import cz.cuni.mff.xrg.odcs.commons.web.AbstractConfigDialog;
import cz.cuni.mff.xrg.odcs.commons.web.ConfigDialogProvider;
import cz.cuni.mff.xrg.odcs.rdf.RDFDataUnit;
import cz.cuni.mff.xrg.odcs.rdf.WritableRDFDataUnit;
import cz.cuni.mff.xrg.uv.rdf.simple.AddPolicy;
import cz.cuni.mff.xrg.uv.rdf.simple.SimpleRdfFactory;
import cz.cuni.mff.xrg.uv.rdf.simple.SimpleRdfRead;
import cz.cuni.mff.xrg.uv.rdf.simple.SimpleRdfWrite;

@AsExtractor
public class Extractor 
extends ConfigurableBase<ExtractorConfig> 
implements DPU, ConfigDialogProvider<ExtractorConfig> {

	@OutputDataUnit(name = "output")
	public WritableRDFDataUnit outputDataUnit;
	
	@InputDataUnit(name = "input")
	public RDFDataUnit inputDataUnit;

	private static final Logger LOG = LoggerFactory.getLogger(Extractor.class);

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
		final SimpleRdfRead inputWrap = SimpleRdfFactory.create(inputDataUnit, ctx);
		final SimpleRdfWrite outputWrap = SimpleRdfFactory.create(outputDataUnit, ctx);
		outputWrap.setPolicy(AddPolicy.BUFFERED);
		
		// vytvorime si parser
		Cache.setInterval(config.getInterval());
		Cache.setTimeout(config.getTimeout());
		Cache.setBaseDir(ctx.getUserDirectory() + "/cache/");
		Cache.rewriteCache = config.isRewriteCache();
		Cache.logger = LOG;
		
		Parser s = new Parser();
		s.logger = LOG;
		s.ctx = ctx;
		s.output = outputWrap;
		
		LOG.info("Starting extraction.");
		
		int lines = 0;
		java.util.Date date = new java.util.Date();
		long start = date.getTime();
		
		List<Statement> statements = inputWrap.getStatements();
		if (!statements.isEmpty())
		{
			int total = statements.size();
			
			URL notationPredicate;
			try {
				notationPredicate = new URL("http://www.w3.org/2004/02/skos/core#notation");
				for (Statement stmt : statements)
				{
					if (ctx.canceled())
					{
						LOG.error("Interrupted");
						break;
					}
					if (stmt.getPredicate().toString().equals(notationPredicate.toString()))
					{
						lines++;
						LOG.debug("Parsing " + lines + "/" + total);
						s.parse(Cache.getDocument(new URL("http://mzcr.cz/LekyNehrazene.aspx?naz=" + stmt.getObject().stringValue()), 10000), "tab");
					}
				}
			} catch (MalformedURLException e) {
				LOG.error("Unexpected malformed URL of ODCS textValue predicate");
			} catch (IOException e) {
				LOG.error("IOException", e);
			} catch (InterruptedException e) {
				
			}
		}
		
		outputWrap.flushBuffer();
		
		java.util.Date date2 = new java.util.Date();
		long end = date2.getTime();
		LOG.info("Processed " + lines + " in " + (end-start) + "ms");

	}

}
