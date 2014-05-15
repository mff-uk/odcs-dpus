package cz.opendata.linked.psp_cz.metadata;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.css_parser.utils.Cache;
import cz.cuni.mff.xrg.odcs.commons.data.DataUnitException;
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
import cz.cuni.mff.xrg.odcs.rdf.simple.SimpleRDF;
import org.openrdf.rio.RDFFormat;

@AsExtractor
public class Extractor 
extends ConfigurableBase<ExtractorConfig> 
implements DPU, ConfigDialogProvider<ExtractorConfig> {

	@OutputDataUnit(name = "output")
	public RDFDataUnit outputDataUnit;

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
		// vytvorime si parser
		Cache.setInterval(config.getInterval());
		Cache.setTimeout(config.getTimeout());
		Cache.setBaseDir(ctx.getUserDirectory() + "/cache/");
		Cache.rewriteCache = config.isRewriteCache();
		Cache.logger = LOG;
		String tempfilename = ctx.getWorkingDir() + "/" + config.getOutputFileName();
		Parser s = new Parser();
		s.logger = LOG;
		s.ctx = ctx;
		try {
			s.ps = new PrintStream(tempfilename, "UTF-8");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			LOG.error("Failed to write stream into file", e);
		}

		s.ps.println(
				"@prefix dcterms:  <http://purl.org/dc/terms/> .\n" +
						//"@prefix rdf:      <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
						//"@prefix rdfs:     <http://www.w3.org/2000/01/rdf-schema#> .\n" +
						"@prefix xsd:      <http://www.w3.org/2001/XMLSchema#> .\n" +
						"\n" +
						"@prefix frbr:     <http://purl.org/vocab/frbr/core#> .\n" +
						"@prefix odcs:     <http://linked.opendata.cz/ontology/odcs/temp/psp.cz-metadata/> .\n" +
						"@prefix lex:      <http://purl.org/lex#> .\n"

				);
		

		// a spustim na vychozi stranku

		ctx.sendMessage(MessageType.INFO, "Starting extraction. From year: " + config.getStart_year() + " To: " + config.getEnd_year() + " Output: " + tempfilename);
		
		try {
			for (int i = config.getStart_year(); i <= config.getEnd_year(); i++)
			{   
				if (ctx.canceled())
				{
					LOG.error("Interrupted");
					break;
				}
				try {
					java.util.Date date = new java.util.Date();
					long start = date.getTime();
					if (!config.isCachedLists())
					{
						Path path = Paths.get(ctx.getUserDirectory().getAbsolutePath() + "/cache/www.psp.cz/sqw/sbirka.sqw@r=" + i);
						LOG.info("Deleting " + path);
						Files.deleteIfExists(path);
					}
					s.parse(new URL("http://www.psp.cz/sqw/sbirka.sqw?r=" + i), "list");
					java.util.Date date2 = new java.util.Date();
					long end = date2.getTime();
					
					ctx.sendMessage(MessageType.INFO, "Processed " + i + " in " + (end-start) + " ms");
				}
				catch (IOException e) {
					LOG.error(e.getLocalizedMessage());
				}
			}
        	
			LOG.info("Parsing done. Passing RDF to ODCS");
			SimpleRDF outputWrap = new SimpleRDF(outputDataUnit, ctx);
			outputWrap.extract(new File(tempfilename), RDFFormat.TURTLE, null);
		} catch (InterruptedException e) {
			LOG.error("Interrupted");
		}

		s.ps.close();
	}

}
