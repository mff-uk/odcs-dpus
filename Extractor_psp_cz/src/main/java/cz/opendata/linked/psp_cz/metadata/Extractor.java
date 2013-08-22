package cz.opendata.linked.psp_cz.metadata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.util.Calendar;

import cz.cuni.mff.css_parser.utils.Cache;
import cz.cuni.xrg.intlib.commons.configuration.ConfigException;
import cz.cuni.xrg.intlib.commons.configuration.Configurable;
import cz.cuni.xrg.intlib.commons.data.DataUnitCreateException;
import cz.cuni.xrg.intlib.commons.data.DataUnitType;
import cz.cuni.xrg.intlib.commons.extractor.Extract;
import cz.cuni.xrg.intlib.commons.extractor.ExtractContext;
import cz.cuni.xrg.intlib.commons.extractor.ExtractException;
import cz.cuni.xrg.intlib.commons.module.dpu.ConfigurableBase;
import cz.cuni.xrg.intlib.commons.module.file.FileManager;
import cz.cuni.xrg.intlib.commons.web.AbstractConfigDialog;
import cz.cuni.xrg.intlib.commons.web.ConfigDialogProvider;
import cz.cuni.xrg.intlib.rdf.interfaces.RDFDataRepository;
import cz.cuni.xrg.intlib.rdf.exceptions.RDFException;

public class Extractor 
extends ConfigurableBase<ExtractorConfig> 
implements Extract, ConfigDialogProvider<ExtractorConfig> {

	/**
	 * DPU's configuration.
	 */

	private Logger logger = LoggerFactory.getLogger(Extract.class);

	public Extractor(){
		super(new ExtractorConfig());
	}

	@Override
	public AbstractConfigDialog<ExtractorConfig> getConfigurationDialog() {		
		return new ExtractorDialog();
	}

	public void extract(ExtractContext ctx) throws ExtractException
	{
		// vytvorime si parser
		Cache.setInterval(config.interval);
		Cache.setTimeout(config.timeout);
		Cache.setBaseDir(ctx.getUserDirectory() + "/cache/");
		Cache.rewriteCache = config.rewriteCache;
		Cache.logger = logger;
		String tempfilename = ctx.getWorkingDir() + "/" + config.outputFileName;
		Parser s = new Parser();
		try {
			s.ps = new PrintStream(tempfilename, "UTF-8");
			s.logger = logger;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		s.ps.println(
				"@prefix dcterms:  <http://purl.org/dc/terms/> .\n" +
						//"@prefix rdf:      <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
						//"@prefix rdfs:     <http://www.w3.org/2000/01/rdf-schema#> .\n" +
						"@prefix xsd:      <http://www.w3.org/2001/XMLSchema#> .\n" +
						"\n" +
						"@prefix frbr:     <http://purl.org/vocab/frbr/core#> .\n" +
						"@prefix lex:      <http://purl.org/lex#> .\n"

				);

		// a spustim na vychozi stranku

		logger.info("Starting extraction. From year: " + config.Start_year + " To: " + config.End_year + " Output: " + tempfilename);
		for (int i = config.Start_year; i <= config.End_year; i++)
		{   
			java.util.Date date = new java.util.Date();
			long start = date.getTime();
			try {
				if (!config.cachedLists)
				{
					Path path = Paths.get(ctx.getUserDirectory().getAbsolutePath() + "/cache/www.psp.cz/sqw/sbirka.sqw@r=" + i);
					logger.info("Deleting " + path);
					Files.deleteIfExists(path);
				}
				s.parse(new URL("http://www.psp.cz/sqw/sbirka.sqw?r=" + i), "list");
			} catch (MalformedURLException e) {
				logger.error(e.getLocalizedMessage());
			} catch (InterruptedException e) {
				logger.error(e.getLocalizedMessage());
			} catch (IOException e) {
				logger.error(e.getLocalizedMessage());
			}
			java.util.Date date2 = new java.util.Date();
			long end = date2.getTime();
			logger.info("Processed " + i + " in " + (end-start) + "ms");
		}

		s.ps.close();

		//give ttl to odcs
		RDFDataRepository outputRepository;
		try {
			outputRepository = (RDFDataRepository) ctx.addOutputDataUnit(DataUnitType.RDF, "output");
		} catch (DataUnitCreateException e) {
			throw new ExtractException("Can't create DataUnit", e);
		}

		try {
			outputRepository.extractFromLocalTurtleFile(tempfilename);
		}
		catch (RDFException e)
		{
			logger.error("Cannot put TTL to repository.");
			throw new ExtractException("Cannot put TTL to repository.");
		}

	}

}
