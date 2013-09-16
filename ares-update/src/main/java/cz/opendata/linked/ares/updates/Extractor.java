package cz.opendata.linked.ares.updates;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.css_parser.utils.Cache;
import cz.cuni.xrg.intlib.commons.dpu.DPU;
import cz.cuni.xrg.intlib.commons.dpu.DPUContext;
import cz.cuni.xrg.intlib.commons.dpu.DPUException;
import cz.cuni.xrg.intlib.commons.dpu.annotation.AsExtractor;
import cz.cuni.xrg.intlib.commons.dpu.annotation.InputDataUnit;
import cz.cuni.xrg.intlib.commons.dpu.annotation.OutputDataUnit;
import cz.cuni.xrg.intlib.commons.module.dpu.ConfigurableBase;
import cz.cuni.xrg.intlib.commons.web.AbstractConfigDialog;
import cz.cuni.xrg.intlib.commons.web.ConfigDialogProvider;
import cz.cuni.xrg.intlib.rdf.exceptions.RDFException;
import cz.cuni.xrg.intlib.rdf.interfaces.RDFDataUnit;

@AsExtractor
public class Extractor 
extends ConfigurableBase<ExtractorConfig> 
implements DPU, ConfigDialogProvider<ExtractorConfig> {

	/**
	 * DPU's configuration.
	 */

	@OutputDataUnit(name = "BEs")
	public RDFDataUnit BEs;

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
		Cache.setInterval(config.interval);
		Cache.setTimeout(config.timeout);
		Cache.setBaseDir(ctx.getUserDirectory() + "/cache/");
		Cache.logger = logger;
		Scraper_parser s = new Scraper_parser();
		s.logger = logger;
		s.ctx = ctx;

        String ICfilename = ctx.getWorkingDir() + "/ic.ttl";
        try {
			s.ps = new PrintStream(ICfilename, "UTF-8");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			logger.error("Unexpected error opening filestreams for temp files");
			e.printStackTrace();
		}

		String prefixes =
        	    "@prefix skos:       <http://www.w3.org/2004/02/skos/core#> .\n" +
        	    "@prefix gr:         <http://purl.org/goodrelations/v1#> .\n" +
        	    "@prefix adms:       <http://www.w3.org/ns/adms#> .\n" +
                    "\n" +
                "@prefix czbe:     <http://linked.opendata.cz/resource/business-entity/> .\n";        

		s.ps.println(prefixes);

		// a spustim na vychozi stranku

		java.util.Date date = new java.util.Date();
		long start = date.getTime();

		//Download

		try {
			URL init = new URL("http://wwwinfo.mfcr.cz/cgi-bin/ares/darv_zm.cgi?cislo_zdroje=2");
			
			Path path = Paths.get(ctx.getUserDirectory().getAbsolutePath() + "/cache/wwwinfo.mfcr.cz/cgi-bin/ares/darv_zm.cgi@cislo_zdroje=2");
			logger.info("Deleting " + path);
			Files.deleteIfExists(path);
			
			s.parse(init, "init");
			
			s.ps.close();

        	logger.info("Parsing done. Passing RDF to ODCS");
	        try {
	        	BEs.addFromTurtleFile(new File(ICfilename));
	        }
	        catch (RDFException e)
	        {
	        	logger.error("Cannot put TTL to repository.");
	        	throw new DPUException("Cannot put TTL to repository.", e);
	        }
		
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			logger.error("Interrupted");
			s.ps.close();
		}

		
		java.util.Date date2 = new java.util.Date();
		long end = date2.getTime();

		logger.info("Processed in " + (end-start) + "ms");

	}

	@Override
	public void cleanUp() {	}

}
