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
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.jsoup.nodes.Document;
import org.openrdf.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.odcs.commons.dpu.DPU;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUContext;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUException;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.AsExtractor;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.InputDataUnit;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.OutputDataUnit;
import cz.cuni.mff.xrg.odcs.commons.message.MessageType;
import cz.cuni.mff.xrg.odcs.commons.module.dpu.ConfigurableBase;
import cz.cuni.mff.xrg.odcs.commons.web.AbstractConfigDialog;
import cz.cuni.mff.xrg.odcs.commons.web.ConfigDialogProvider;
import cz.cuni.mff.xrg.odcs.rdf.RDFDataUnit;
import cz.cuni.mff.xrg.odcs.rdf.simple.AddPolicy;
import cz.cuni.mff.xrg.odcs.rdf.simple.SimpleRDF;
import cz.cuni.mff.xrg.scraper.css_parser.utils.BannedException;
import cz.cuni.mff.xrg.scraper.css_parser.utils.Cache;
import org.openrdf.model.ValueFactory;

@AsExtractor
public class Extractor 
extends ConfigurableBase<ExtractorConfig> 
implements DPU, ConfigDialogProvider<ExtractorConfig> {

	private static final Logger LOG = LoggerFactory.getLogger(DPU.class);
	
	@InputDataUnit(name = "ICs", optional = true )
	public RDFDataUnit duICs;
	
	@OutputDataUnit(name = "Basic")
	public RDFDataUnit outBasic;

	@OutputDataUnit(name = "OR")
	public RDFDataUnit outOR;

	public Extractor(){
		super(ExtractorConfig.class);
	}

	private int countTodaysCacheFiles(DPUContext ctx) throws ParseException 
	{
		int count = 0;

		// Directory path here
		String path = ctx.getUserDirectory() + "/cache/wwwinfo.mfcr.cz/cgi-bin/ares/"; 
		File folder = new File(path);
		if (!folder.isDirectory()) return 0;

		File[] listOfFiles = folder.listFiles(); 
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

		for (File item : listOfFiles){
			if (item.isFile())  {
				Date now = new Date();
				Date modified = sdf.parse(sdf.format(item.lastModified()));
				long diff = (now.getTime() - modified.getTime()) / 1000;
				//System.out.println("Date modified: " + sdf.format(currentFile.lastModified()) + " which is " + diff + " seconds ago.");

				if (diff < (config.getHoursToCheck() * 60 * 60)) count++;
			}
		}

		ctx.sendMessage(MessageType.INFO, "Total of " + count + " files cached in last " + config.getHoursToCheck() + " hours. " + (config.getPerDay() - count) + " remaining.");
		return count;
	}


	@Override
	public AbstractConfigDialog<ExtractorConfig> getConfigurationDialog() {		
		return new ExtractorDialog();
	}

	@Override
	public void execute(DPUContext ctx) throws DPUException
	{	
		Cache.setInterval(config.getInterval());
		Cache.setTimeout(config.getTimeout());
		Cache.setBaseDir(ctx.getUserDirectory() + "/cache/");
		Cache.logger = LOG;
		Scraper_parser s = new Scraper_parser();
		s.logger = LOG;

		java.util.Date date = new java.util.Date();
		long start = date.getTime();

		Set<String> ICs = new TreeSet<>();

		//Load ICs from input DataUnit
		
		ctx.sendMessage(MessageType.INFO, "Interval: " + config.getInterval());
		ctx.sendMessage(MessageType.INFO, "Timeout: " + config.getTimeout());
		ctx.sendMessage(MessageType.INFO, "Hours to check: " + config.getHoursToCheck());
		ctx.sendMessage(MessageType.INFO, "Dowload per time frame: " + config.getPerDay());
		ctx.sendMessage(MessageType.INFO, "Cache base dir: " + Cache.basePath);
		ctx.sendMessage(MessageType.INFO, "Cache only: " + config.isUseCacheOnly());
		ctx.sendMessage(MessageType.INFO, "Generating output: " + config.isGenerateOutput());
		ctx.sendMessage(MessageType.INFO, "BAS Active only: " + config.isBas_active());
		ctx.sendMessage(MessageType.INFO, "Puvadr in BAS: " + config.isBas_puvadr());
		ctx.sendMessage(MessageType.INFO, "Stdadr in OR: " + config.isOr_stdadr());

		int lines = 0;
		
		SimpleRDF duICsWrap = new SimpleRDF(duICs, ctx);
		final List<Statement> statements = duICsWrap.getStatements();
		if (statements != null && !statements.isEmpty())
		{
			URL textPredicate;
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
				LOG.error("Unexpected malformed URL of ODCS textValue predicate");
			}
		}
		LOG.info("{} ICs loaded from input", lines);
		
		//Load ICs from file
		BufferedReader in;
		lines = 0;
		try {
			in = new BufferedReader(new FileReader(new File(ctx.getUserDirectory(),"ic.txt")));
			while (in.ready()) {
				ICs.add(in.readLine());
				lines++;
			}
			in.close();
		} catch (IOException e) {
			LOG.info("IO error when loading ICs from file - probably not present");
		}
		LOG.info(lines + " ICs loaded from config file");
		//End Load ICs from file

		int downloaded = 0;
		int cachedToday = 0;
		int cachedEarlier = 0;
		
		try {
			cachedToday = countTodaysCacheFiles(ctx);
		} catch (ParseException e) {
			LOG.info("countTodaysCacheFiles throws", e);
		}

		ctx.sendMessage(MessageType.INFO, "I see " + ICs.size() + " ICs.");

		/*//Remove duplicate ICs
		List<String> dedupICs = new LinkedList<String>();    
		for (String currentIC: ICs)
		{
		  if (!dedupICs.contains(currentIC)) 
		  {
			  dedupICs.add(currentIC);
		  }
		 }
		
		ICs = dedupICs;*/
		
		if (ctx.canceled()) {
			return;
		}
		

		//Download
		int toCache = (config.getPerDay() - cachedToday);
		Iterator<String> li = ICs.iterator();
		//ctx.sendMessage(MessageType.INFO, "I see " + ICs.size() + " ICs after deduplication.");

		final SimpleRDF outBasicWrap = new SimpleRDF(outBasic, ctx);
		outBasicWrap.setPolicy(AddPolicy.BUFFERED);

		final SimpleRDF outORWrap = new SimpleRDF(outOR, ctx);
		outORWrap.setPolicy(AddPolicy.BUFFERED);

		try {
			while (li.hasNext() && !ctx.canceled() && (config.isUseCacheOnly() || (downloaded < (toCache - 1)))) {
				String currentIC = li.next();
				URL current;

				if (config.isDownloadBasic()) {
					current = new URL("http://wwwinfo.mfcr.cz/cgi-bin/ares/darv_bas.cgi?ico=" + currentIC + (config.isBas_active() ? "" : "&aktivni=false") + (config.isBas_puvadr() ? "&adr_puv=true" : ""));

					final ValueFactory valueFactory = outBasicWrap.getValueFactory();						
					if (!Cache.isCached(current) && !config.isUseCacheOnly())
					{
						Document doc = Cache.getDocument(current, 10, "xml");
						if (doc != null)
						{
							//logger.trace(doc.outerHtml());
							if (config.isGenerateOutput()) {
								outBasicWrap.add(
										valueFactory.createURI("http://linked.opendata.cz/ontology/odcs/DataUnit"), 
										valueFactory.createURI("http://linked.opendata.cz/ontology/odcs/xmlValue"),
										valueFactory.createLiteral(doc.outerHtml()));
							}
							LOG.debug("Downloaded {}/{} in this run.", ++downloaded, toCache);
						}
						else
						{
							LOG.warn("Document null: {}", current);
						}
					}
					else if (Cache.isCached(current))
					{
						Document doc = Cache.getDocument(current, 10, "xml");
						cachedEarlier++;
						if (doc != null)
						{
							//logger.trace(doc.outerHtml());
							if (config.isGenerateOutput()) {
								outBasicWrap.add(
										valueFactory.createURI("http://linked.opendata.cz/ontology/odcs/DataUnit"), 
										valueFactory.createURI("http://linked.opendata.cz/ontology/odcs/xmlValue"),
										valueFactory.createLiteral(doc.outerHtml()));
							}
							LOG.debug("Got from cache {}:{}", cachedEarlier, current );
						}
						else
						{
							LOG.warn("Document null: {}", current);
						}
					}
				}

				if (ctx.canceled()) break;

				if (config.isDownloadOR()) {
					current = new URL("http://wwwinfo.mfcr.cz/cgi-bin/ares/darv_or.cgi?ico=" + currentIC + (config.isOr_stdadr()? "&stdadr=true" : ""));

					final ValueFactory valueFactory = outORWrap.getValueFactory();
					if (!Cache.isCached(current) && !config.isUseCacheOnly())
					{
						Document doc = Cache.getDocument(current, 10, "xml");
						cachedEarlier++;
						if (doc != null)
						{
							if (config.isGenerateOutput()) {
								outORWrap.add( 
										valueFactory.createURI("http://linked.opendata.cz/ontology/odcs/DataUnit"), 
										valueFactory.createURI("http://linked.opendata.cz/ontology/odcs/xmlValue"),
										valueFactory.createLiteral(doc.outerHtml()));
							}
							LOG.debug("Downloaded {}/{} in this run: {}", ++downloaded, toCache, current);
						}
						else
						{
							LOG.warn("Document null: {}", current);
						}
					}
					else if (Cache.isCached(current))
					{
						Document doc = Cache.getDocument(current, 10, "xml");
						cachedEarlier++;
						if (doc != null)
						{
							if (config.isGenerateOutput()) {
								outORWrap.add( 
										valueFactory.createURI("http://linked.opendata.cz/ontology/odcs/DataUnit"), 
										valueFactory.createURI("http://linked.opendata.cz/ontology/odcs/xmlValue"),
										valueFactory.createLiteral(doc.outerHtml()));
							}
							LOG.debug("Got from cache {}: {}", cachedEarlier, current);
						}
						else
						{
							LOG.warn("Document null: {}", current);
						}
					}
				}
			}
			if (ctx.canceled()) LOG.error("Interrupted");
		} catch (BannedException e) {
			LOG.warn("Seems like we are banned for today");
		} catch (IOException e) {
			LOG.info("IOException", e);
		} catch (InterruptedException e) {
			LOG.error("Interrupted");
		}

		// add the triples in repositories
		outBasicWrap.flushBuffer();
		outORWrap.flushBuffer();
		
		java.util.Date date2 = new java.util.Date();
		long end = date2.getTime();

		ctx.sendMessage(MessageType.INFO, "Processed in " + (end-start) + "ms, ICs on input: " + ICs.size() + (cachedEarlier > 0? ", files cached earlier: " + cachedEarlier : "") + ", files downloaded now: " + downloaded);
	}

}
