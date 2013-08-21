package cz.opendata.linked.ares;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.css_parser.utils.Cache;
import cz.cuni.xrg.intlib.commons.extractor.Extract;
import cz.cuni.xrg.intlib.commons.extractor.ExtractContext;
import cz.cuni.xrg.intlib.commons.extractor.ExtractException;
import cz.cuni.xrg.intlib.commons.module.dpu.ConfigurableBase;
import cz.cuni.xrg.intlib.commons.web.AbstractConfigDialog;
import cz.cuni.xrg.intlib.commons.web.ConfigDialogProvider;

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

	private int countTodaysCacheFiles(ExtractContext ctx) throws ParseException 
	{
		int count = 0;

		// Directory path here
		String path = ctx.getUserDirectory() + "/cache/wwwinfo.mfcr.cz/cgi-bin/ares/"; 
		File currentFile;
		File folder = new File(path);
		if (!folder.isDirectory()) return 0;

		File[] listOfFiles = folder.listFiles(); 
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

		for (int i = 0; i < listOfFiles.length; i++) 
		{

			if (listOfFiles[i].isFile()) 
			{
				currentFile = listOfFiles[i];

				Date now = new Date();
				//    	    	  System.currentTimeMillis()
				Date modified = sdf.parse(sdf.format(currentFile.lastModified()));
				long diff = (now.getTime() - modified.getTime()) / 1000;
				//System.out.println("Date modified: " + sdf.format(currentFile.lastModified()) + " which is " + diff + " seconds ago.");

				if (diff < (config.hoursToCheck * 60 * 60)) count++;
			}
		}
		logger.info("Total of " + count + " files cached in last " + config.hoursToCheck + " hours. " + (config.PerDay - count) + " remaining.");
		return count;
	}


	@Override
	public AbstractConfigDialog<ExtractorConfig> getConfigurationDialog() {		
		return new ExtractorDialog();
	}

	public void extract(ExtractContext ctx) throws ExtractException
	{
		Cache.setInterval(config.interval);
		Cache.setTimeout(config.timeout);
		Cache.setBaseDir(ctx.getUserDirectory() + "/cache/");
		Cache.logger = logger;
		Scraper_parser s = new Scraper_parser();
		s.logger = logger;

		/*String prefixes =
                "@prefix dcterms:    <http://purl.org/dc/terms/> .\n" +
        	    "@prefix rdf:        <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
                //"@prefix rdfs:       <http://www.w3.org/2000/01/rdf-schema#> .\n" +
                "@prefix xsd:        <http://www.w3.org/2001/XMLSchema#> .\n" +
        	    "@prefix gr:         <http://purl.org/goodrelations/v1#> .\n" +
        	    "@prefix adms:       <http://www.w3.org/ns/adms#> .\n" +
        	    "@prefix v:          <http://www.w3.org/2006/vcard/ns#> .\n" +
        	    "@prefix skos:       <http://www.w3.org/2004/02/skos/core#> .\n" +
                "@prefix pc:         <http://purl.org/procurement/public-contracts#> .\n" +
        	    "@prefix pccz:       <http://purl.org/procurement/public-contracts-czech#> .\n" +
                "@prefix activities: <http://purl.org/procurement/public-contracts-activities#> .\n" +
                "@prefix kinds:      <http://purl.org/procurement/public-contracts-kinds#> .\n" +
                "@prefix authkinds:  <http://purl.org/procurement/public-contracts-authority-kinds#> .\n" +
                "@prefix proctypes:  <http://purl.org/procurement/public-contracts-procedure-types#> .\n" +
                "@prefix countries:  <http://linked.opendata.cz/resource/domain/buyer-profiles/country#> .\n" +
                "@prefix czstatus:   <http://purl.org/procurement/public-contracts-czech-statuses#> .\n" +
                    "\n" +
                "@prefix czbe:     <http://linked.opendata.cz/resource/business-entity/> .\n";*/        

		//s.ps.println(prefixes);
		//s.zak_ps.println(prefixes);

		// a spustim na vychozi stranku

		java.util.Date date = new java.util.Date();
		long start = date.getTime();

		BufferedReader in = null;
		int lines = 0;
		try {
			in = new BufferedReader(new FileReader(new File(ctx.getUserDirectory(),"ic.txt")));
			while (in.ready()) {
				String line = in.readLine();
				lines++;
			}
			in.close();
			logger.info("I see " + lines + " ICs.");
			in = new BufferedReader(new FileReader(new File(ctx.getUserDirectory(),"ic.txt")));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int downloaded = 0;
		int cachedToday = 0;
		int cachedEarlier = 0;
		
		try {
			cachedToday = countTodaysCacheFiles(ctx);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int toCache = (config.PerDay - cachedToday);
		try {
			while (in.ready() && downloaded < toCache) {
				String line = in.readLine();
				URL current = new URL("http://wwwinfo.mfcr.cz/cgi-bin/ares/darv_bas.cgi?ico=" + line);
				if (!Cache.isCached(current))
				{
					Document doc = Cache.getDocument(current, 10, "xml");
					logger.debug("Downloaded " + ++downloaded + "/" + toCache + " in this run.");
				}
				else cachedEarlier++;
			}
			in.close();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		java.util.Date date2 = new java.util.Date();
		long end = date2.getTime();

		logger.info("Processed in " + (end-start) + "ms, ICs on input: " + lines + (cachedEarlier > 0? ", cached earlier: " + cachedEarlier : "") + ", downloaded now: " + downloaded);

	}

}
