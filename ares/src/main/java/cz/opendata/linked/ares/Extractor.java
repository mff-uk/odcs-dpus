package cz.opendata.linked.ares;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
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

import cz.cuni.mff.xrg.odcs.commons.dpu.DPU;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUContext;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUException;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.AsExtractor;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.InputDataUnit;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.OutputDataUnit;
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

	@InputDataUnit(name = "ICs", optional = true )
	public RDFDataUnit duICs;
	
	@OutputDataUnit(name = "Basic")
	public RDFDataUnit outBasic;

	@OutputDataUnit(name = "OR")
	public RDFDataUnit outOR;

	private Logger logger = LoggerFactory.getLogger(DPU.class);

	public Extractor(){
		super(ExtractorConfig.class);
	}

	private int countTodaysCacheFiles(DPUContext ctx) throws ParseException 
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

	public void execute(DPUContext ctx) throws DPUException
	{
		Cache.setInterval(config.interval);
		Cache.setTimeout(config.timeout);
		Cache.setBaseDir(ctx.getUserDirectory() + "/cache/");
		Cache.logger = logger;
		Scraper_parser s = new Scraper_parser();
		s.logger = logger;

		java.util.Date date = new java.util.Date();
		long start = date.getTime();

		List<String> ICs = new LinkedList<String>();

		//Load ICs from input DataUnit
		
		int lines = 0;
		if (duICs.getTripleCount() > 0)
		{
			List<Statement> statements = duICs.getTriples();
			
			URL textPredicate = null;
			try {
				textPredicate = new URL("http://linked.opendata.cz/ontology/odcs/textValue");
				for (Statement stmt : statements)
				{
					if (stmt.getPredicate().toString().equals(textPredicate.toString()))
					{
						ICs.add(stmt.getObject().stringValue());
						lines++;
					}
				}
			} catch (MalformedURLException e) {
				logger.error("Unexpected malformed URL of ODCS textValue predicate");
			}
		}
		logger.info(lines + " ICs loaded from input");
		
		//Load ICs from file
		BufferedReader in = null;
		lines = 0;
		try {
			in = new BufferedReader(new FileReader(new File(ctx.getUserDirectory(),"ic.txt")));
			while (in.ready()) {
				ICs.add(in.readLine());
				lines++;
			}
			in.close();
		} catch (IOException e) {
			logger.info("IO error when loading ICs from file - probably not present");
		}
		logger.info(lines + " ICs loaded from config file");
		//End Load ICs from file

		int downloaded = 0;
		int cachedToday = 0;
		int cachedEarlier = 0;
		
		try {
			cachedToday = countTodaysCacheFiles(ctx);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		logger.info("I see " + ICs.size() + " ICs before deduplication.");

		//Remove duplicate ICs
		List<String> dedupICs = new LinkedList<String>();    
		for (String currentIC: ICs)
		{
		  if (!dedupICs.contains(currentIC)) 
		  {
			  dedupICs.add(currentIC);
		  }
		 }
		
		ICs = dedupICs;
		
		if (!ctx.canceled())
		{
			//Download
			int toCache = (config.PerDay - cachedToday);
			Iterator<String> li = ICs.iterator();
			logger.info("I see " + ICs.size() + " ICs after deduplication.");
	
			try {
				while (li.hasNext() && !ctx.canceled() && (config.sendCache || (downloaded < (toCache - 1)))) {
					String currentIC = li.next();
					URL current = new URL("http://wwwinfo.mfcr.cz/cgi-bin/ares/darv_bas.cgi?ico=" + currentIC);
					if (!Cache.isCached(current) && !config.sendCache)
					{
						Document doc = Cache.getDocument(current, 10, "xml");
						if (doc != null)
						{
							//logger.trace(doc.outerHtml());
							outBasic.addTriple(outBasic.createURI("http://linked.opendata.cz/ontology/odcs/DataUnit"), outBasic.createURI("http://linked.opendata.cz/ontology/odcs/xmlValue"),outBasic.createLiteral(doc.outerHtml()));
							logger.debug("Downloaded " + ++downloaded + "/" + toCache + " in this run.");
						}
						else
						{
							logger.warn("Document null: " + current);
						}
					}
					else if (Cache.isCached(current))
					{
						Document doc = Cache.getDocument(current, 10, "xml");
						cachedEarlier++;
						if (doc != null)
						{
							//logger.trace(doc.outerHtml());
							outBasic.addTriple(outBasic.createURI("http://linked.opendata.cz/ontology/odcs/DataUnit"), outBasic.createURI("http://linked.opendata.cz/ontology/odcs/xmlValue"),outBasic.createLiteral(doc.outerHtml()));
							logger.debug("Got from cache "+ cachedEarlier + ": " + current );
						}
						else
						{
							logger.warn("Document null: " + current);
						}
					}
						
					if (ctx.canceled()) break;
					
					current = new URL("http://wwwinfo.mfcr.cz/cgi-bin/ares/darv_or.cgi?ico=" + currentIC);
					if (!Cache.isCached(current) && !config.sendCache)
					{
						Document doc = Cache.getDocument(current, 10, "xml");
						cachedEarlier++;
						if (doc != null)
						{
							outOR.addTriple(outOR.createURI("http://linked.opendata.cz/ontology/odcs/DataUnit"), outOR.createURI("http://linked.opendata.cz/ontology/odcs/xmlValue"),outOR.createLiteral(doc.outerHtml()));
							logger.debug("Downloaded " + ++downloaded + "/" + toCache + " in this run: " + current);
						}
						else
						{
							logger.warn("Document null: " + current);
						}
					}
					else if (Cache.isCached(current))
					{
						Document doc = Cache.getDocument(current, 10, "xml");
						cachedEarlier++;
						if (doc != null)
						{
							outOR.addTriple(outOR.createURI("http://linked.opendata.cz/ontology/odcs/DataUnit"), outOR.createURI("http://linked.opendata.cz/ontology/odcs/xmlValue"),outOR.createLiteral(doc.outerHtml()));
							logger.debug("Got from cache "+ cachedEarlier + ": " + current );
						}
						else
						{
							logger.warn("Document null: " + current);
						}
					}
				}
				if (ctx.canceled()) logger.error("Interrupted");
			} catch (BannedException e) {
				logger.warn("Seems like we are banned for today");
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				logger.error("Interrupted");
			}
		}

		java.util.Date date2 = new java.util.Date();
		long end = date2.getTime();

		logger.info("Processed in " + (end-start) + "ms, ICs on input: " + ICs.size() + (cachedEarlier > 0? ", files cached earlier: " + cachedEarlier : "") + ", files downloaded now: " + downloaded);

	}

	@Override
	public void cleanUp() {	}

}
