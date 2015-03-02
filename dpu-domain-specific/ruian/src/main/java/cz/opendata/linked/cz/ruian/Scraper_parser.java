package cz.opendata.linked.cz.ruian;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;

import cz.cuni.mff.xrg.scraper.lib.template.ParseEntry;
import cz.cuni.mff.xrg.scraper.lib.template.ScrapingTemplate;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.extension.files.simple.WritableSimpleFiles;

/**
 * Scraper pro RUIAN
 * 
 * @author Jakub Klímek
 */

public class Scraper_parser extends ScrapingTemplate{
    
    public WritableSimpleFiles obce, zsj;
    private int numDetails = 0;
    private int current;
    public boolean outputFiles;
    
    @Override
    protected LinkedList<ParseEntry> getLinks(String doc, String docType) {
        final LinkedList<ParseEntry> out = new LinkedList<>();
        
        if (docType.equals("init") || docType.equals("initStat"))
        {
            String[] lines = doc.split("\\r\\n");
            String maxdate = "";
            for (String line : lines)
            {
                String current = line.substring(line.lastIndexOf('/') + 1, line.lastIndexOf('/') + 9);
                if (current.compareTo(maxdate) > 0) maxdate = current;
            }
            numDetails = 0;
            current = 0;
            for (String line : lines) { if (line.contains(maxdate)) numDetails++;    }
            
            logger.info("I see " + numDetails + " current files from " + maxdate + ", " + lines.length + " total.");

            for (String line : lines)
            {
                try {
                    if (line.contains(maxdate)) {
                        if (docType.equals("init")) out.add(new ParseEntry(new URL(line),"obec","gz"));
                        else if (docType.equals("initStat")) out.add(new ParseEntry(new URL(line),"zsj","gz"));
                    }
                } catch (MalformedURLException e) {
                    logger.warn(e.getLocalizedMessage());
                }
            }
        }
        return out;
    }
    
    @Override
    protected void parse(String doc, String docType, URL url) {
        if (docType.equals("obec"))
        {
            logger.debug("Processing detail " + ++current + "/" + numDetails + ": " + url.toString());
            if (outputFiles) {
                File file = new File(doc);
				try {
					obce.add(file, file.getName());
				} catch (DPUException e) {
					logger.error(e.getLocalizedMessage(), e);
				}
            }
        }
        else if (docType.equals("zsj"))
        {
            logger.debug("Processing detail " + ++current + "/" + numDetails + ": " + url.toString());
            if (outputFiles) {
				File file = new File(doc);
				try {
					zsj.add(file, file.getName());
				} catch (DPUException e) {
					logger.error(e.getLocalizedMessage(), e);
				}
			}
        }
    }
}
