package cz.opendata.linked.ares.updates;

import cz.cuni.mff.xrg.odcs.commons.data.DataUnitException;
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
import cz.cuni.mff.xrg.uv.rdf.simple.SimpleRdfWrite;
import cz.cuni.mff.xrg.scraper.css_parser.utils.BannedException;
import cz.cuni.mff.xrg.scraper.css_parser.utils.Cache;
import cz.cuni.mff.xrg.uv.rdf.simple.SimpleRdfFactory;
import org.openrdf.rio.RDFFormat;

@AsExtractor
public class Extractor 
extends ConfigurableBase<ExtractorConfig> 
implements DPU, ConfigDialogProvider<ExtractorConfig> {

	@OutputDataUnit(name = "BEs")
	public WritableRDFDataUnit BEs;

	private final static Logger LOG = LoggerFactory.getLogger(DPU.class);

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
		Cache.setInterval(config.getInterval());
		Cache.setTimeout(config.getTimeout());
		Cache.setBaseDir(ctx.getUserDirectory() + "/cache/");
		Cache.logger = LOG;
		Scraper_parser s = new Scraper_parser();
		s.logger = LOG;
		s.ctx = ctx;

        String ICfilename = ctx.getWorkingDir() + "/ic.ttl";
        try {
			s.ps = new PrintStream(ICfilename, "UTF-8");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			LOG.error("Unexpected error opening filestreams for temp files", e);
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
			LOG.info("Deleting {}", path);
			Files.deleteIfExists(path);
			
			try {
				s.parse(init, "init");
			} catch (BannedException b) {
				LOG.warn("Seems like we are banned for today");
			}
			
			s.ps.close();

        	LOG.info("Parsing done. Passing RDF to ODCS");
			
			SimpleRdfWrite BEsWrap = SimpleRdfFactory.create(BEs, ctx);
			BEsWrap.extract(new File(ICfilename), RDFFormat.TURTLE, null);
		
		} catch (IOException e) {
			LOG.error("IOException", e);
		} catch (InterruptedException e) {
			LOG.error("Interrupted");
			s.ps.close();
		}
		
		java.util.Date date2 = new java.util.Date();
		long end = date2.getTime();

		ctx.sendMessage(MessageType.INFO, "Processed in " + (end-start) + "ms");
	}

}
