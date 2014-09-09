package cz.opendata.linked.ares;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.jsoup.nodes.Document;
import org.openrdf.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.uv.boost.dpu.advanced.DpuAdvancedBase;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import cz.cuni.mff.xrg.uv.boost.dpu.config.MasterConfigObject;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.AddPolicy;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.OperationFailedException;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.SimpleRdfRead;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.SimpleRdfWrite;
import cz.cuni.mff.xrg.scraper.css_parser.utils.BannedException;
import cz.cuni.mff.xrg.scraper.css_parser.utils.Cache;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.*;

import org.openrdf.model.ValueFactory;

@DPU.AsExtractor
public class Extractor 
extends DpuAdvancedBase<ExtractorConfig> 
{

    private static final Logger LOG = LoggerFactory.getLogger(DPU.class);
    
    @DataUnit.AsInput(name = "ICs", optional = true )
    public RDFDataUnit duICs;
    
    @DataUnit.AsOutput(name = "Basic")
    public WritableFilesDataUnit outBasic;

    @DataUnit.AsOutput(name = "OR")
    public WritableFilesDataUnit outOR;

    @DataUnit.AsOutput(name = "RZP")
    public WritableFilesDataUnit outRZP;

    public Extractor(){
        super(ExtractorConfig.class,AddonInitializer.noAddons());
    }

    private int countTodaysCacheFiles(DPUContext context) throws ParseException 
    {
        int count = 0;

        // Directory path here
        String path = context.getUserDirectory() + "/cache/wwwinfo.mfcr.cz/cgi-bin/ares/"; 
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

        context.sendMessage(DPUContext.MessageType.INFO, "Total of " + count + " files cached in last " + config.getHoursToCheck() + " hours. " + (config.getPerDay() - count) + " remaining.");
        return count;
    }


    @Override
    public AbstractConfigDialog<MasterConfigObject> getConfigurationDialog() {        
        return new ExtractorDialog();
    }

    @Override
    protected void innerExecute() throws DPUException, OperationFailedException
    {    
        Cache.setInterval(config.getInterval());
        Cache.setTimeout(config.getTimeout());
        Cache.setBaseDir(context.getUserDirectory() + "/cache/");
        Cache.logger = LOG;
        Scraper_parser s = new Scraper_parser();
        s.logger = LOG;

        java.util.Date date = new java.util.Date();
        long start = date.getTime();

        Set<String> ICs = new TreeSet<>();

        //Load ICs from input DataUnit
        
        context.sendMessage(DPUContext.MessageType.INFO, "Interval: " + config.getInterval());
        context.sendMessage(DPUContext.MessageType.INFO, "Timeout: " + config.getTimeout());
        context.sendMessage(DPUContext.MessageType.INFO, "Hours to check: " + config.getHoursToCheck());
        context.sendMessage(DPUContext.MessageType.INFO, "Dowload per time frame: " + config.getPerDay());
        context.sendMessage(DPUContext.MessageType.INFO, "Cache base dir: " + Cache.basePath);
        context.sendMessage(DPUContext.MessageType.INFO, "Cache only: " + config.isUseCacheOnly());
        context.sendMessage(DPUContext.MessageType.INFO, "Generating output: " + config.isGenerateOutput());
        context.sendMessage(DPUContext.MessageType.INFO, "BAS Active only: " + config.isBas_active());
        context.sendMessage(DPUContext.MessageType.INFO, "Puvadr in BAS: " + config.isBas_puvadr());
        context.sendMessage(DPUContext.MessageType.INFO, "Stdadr in OR: " + config.isOr_stdadr());

        int lines = 0;
        
        SimpleRdfRead duICsWrap = SimpleRdfFactory.create(duICs, context);
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
            in = new BufferedReader(new FileReader(new File(context.getUserDirectory(),"ic.txt")));
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
            cachedToday = countTodaysCacheFiles(context);
        } catch (ParseException e) {
            LOG.info("countTodaysCacheFiles throws", e);
        }

        context.sendMessage(DPUContext.MessageType.INFO, "I see " + ICs.size() + " ICs.");

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
        
        if (context.canceled()) {
            return;
        }
        

        //Download
        int toCache = (config.getPerDay() - cachedToday);
        Iterator<String> li = ICs.iterator();
        //context.sendMessage(DPUContext.MessageType.INFO, "I see " + ICs.size() + " ICs after deduplication.");

        try {
            while (li.hasNext() && !context.canceled() && (config.isUseCacheOnly() || (downloaded < (toCache - 1)))) {
                String currentIC = li.next();
                URL current;

                if (config.isDownloadBasic()) {
                    current = new URL("http://wwwinfo.mfcr.cz/cgi-bin/ares/darv_bas.cgi?ico=" + currentIC + (config.isBas_active() ? "" : "&aktivni=false") + (config.isBas_puvadr() ? "&adr_puv=true" : ""));

                    if (!Cache.isCached(current) && !config.isUseCacheOnly())
                    {
                        Document doc = Cache.getDocument(current, 10, "xml");
                        if (doc != null)
                        {
                            //logger.trace(doc.outerHtml());
                            if (config.isGenerateOutput()) {
                				try {
									File f = new File(URI.create(outBasic.addNewFile(current.toString())));
									FileUtils.writeStringToFile(f, doc.outerHtml());
								} catch (DataUnitException e) {
									LOG.error(e.getLocalizedMessage(), e);
								}
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
                				try {
									File f = new File(URI.create(outBasic.addNewFile(current.toString())));
									FileUtils.writeStringToFile(f, doc.outerHtml());
								} catch (DataUnitException e) {
									LOG.error(e.getLocalizedMessage(), e);
								}
                            }
                            LOG.debug("Got from cache {}:{}", cachedEarlier, current );
                        }
                        else
                        {
                            LOG.warn("Document null: {}", current);
                        }
                    }
                }

                if (context.canceled()) break;

                if (config.isDownloadOR()) {
                    current = new URL("http://wwwinfo.mfcr.cz/cgi-bin/ares/darv_or.cgi?ico=" + currentIC + (config.isOr_stdadr()? "&stdadr=true" : ""));

                    if (!Cache.isCached(current) && !config.isUseCacheOnly())
                    {
                        Document doc = Cache.getDocument(current, 10, "xml");
                        cachedEarlier++;
                        if (doc != null)
                        {
                            if (config.isGenerateOutput()) {
                				try {
									File f = new File(URI.create(outOR.addNewFile(current.toString())));
									FileUtils.writeStringToFile(f, doc.outerHtml());
								} catch (DataUnitException e) {
									LOG.error(e.getLocalizedMessage(), e);
								}
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
                				try {
									File f = new File(URI.create(outOR.addNewFile(current.toString())));
									FileUtils.writeStringToFile(f, doc.outerHtml());
								} catch (DataUnitException e) {
									LOG.error(e.getLocalizedMessage(), e);
								}
                            }
                            LOG.debug("Got from cache {}: {}", cachedEarlier, current);
                        }
                        else
                        {
                            LOG.warn("Document null: {}", current);
                        }
                    }
                }

                if (context.canceled()) break;

                if (config.isDownloadRZP()) {
                    current = new URL("http://wwwinfo.mfcr.cz/cgi-bin/ares/darv_rzp.cgi?ico=" + currentIC + "&rozsah=2");

                    if (!Cache.isCached(current) && !config.isUseCacheOnly())
                    {
                        Document doc = Cache.getDocument(current, 10, "xml");
                        cachedEarlier++;
                        if (doc != null)
                        {
                            if (config.isGenerateOutput()) {
                				try {
									File f = new File(URI.create(outRZP.addNewFile(current.toString())));
									FileUtils.writeStringToFile(f, doc.outerHtml());
								} catch (DataUnitException e) {
									LOG.error(e.getLocalizedMessage(), e);
								}
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
                				try {
									File f = new File(URI.create(outRZP.addNewFile(current.toString())));
									FileUtils.writeStringToFile(f, doc.outerHtml());
								} catch (DataUnitException e) {
									LOG.error(e.getLocalizedMessage(), e);
								}
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
            if (context.canceled()) LOG.error("Interrupted");
        } catch (BannedException e) {
            LOG.warn("Seems like we are banned for today");
        } catch (IOException e) {
            LOG.info("IOException", e);
        } catch (InterruptedException e) {
            LOG.error("Interrupted");
        }

        java.util.Date date2 = new java.util.Date();
        long end = date2.getTime();

        context.sendMessage(DPUContext.MessageType.INFO, "Processed in " + (end-start) + "ms, ICs on input: " + ICs.size() + (cachedEarlier > 0? ", files cached earlier: " + cachedEarlier : "") + ", files downloaded now: " + downloaded);
    }

}
