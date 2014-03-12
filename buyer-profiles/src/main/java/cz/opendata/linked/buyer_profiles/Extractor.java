package cz.opendata.linked.buyer_profiles;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.css_parser.utils.Cache;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPU;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUContext;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUException;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.AsExtractor;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.OutputDataUnit;
import cz.cuni.mff.xrg.odcs.commons.message.MessageType;
import cz.cuni.mff.xrg.odcs.commons.module.dpu.ConfigurableBase;
import cz.cuni.mff.xrg.odcs.commons.web.AbstractConfigDialog;
import cz.cuni.mff.xrg.odcs.commons.web.ConfigDialogProvider;
import cz.cuni.mff.xrg.odcs.rdf.exceptions.RDFException;
import cz.cuni.mff.xrg.odcs.rdf.interfaces.RDFDataUnit;

@AsExtractor
public class Extractor 
        extends ConfigurableBase<ExtractorConfig> 
        implements DPU, ConfigDialogProvider<ExtractorConfig> {
	
	@OutputDataUnit(name = "contracts")
	public RDFDataUnit contractsDataUnit;

	@OutputDataUnit(name = "profiles")
	public RDFDataUnit profilesDataUnit;

	private static final Logger LOG = LoggerFactory.getLogger(DPU.class);

    public Extractor() {
        super(ExtractorConfig.class);
    }
	 
    @Override
	public AbstractConfigDialog<ExtractorConfig> getConfigurationDialog() {		
		return new ExtractorDialog();
	}
	
	@Override
	public void execute(DPUContext ctx) throws DPUException
	{
        // vytvorime si parser
        
    	Cache.logger = LOG;
    	Cache.rewriteCache = config.isRewriteCache();
    	Cache.setBaseDir(ctx.getUserDirectory() + "/cache/");
    	Cache.setTimeout(config.getTimeout());
    	Cache.setInterval(config.getInterval());
		try {
			Cache.setTrustAllCerts();
		} catch (Exception e) {
			LOG.error("Unexpected error when setting trust to all certificates. ", e);
		}
		
        Scraper_parser s = new Scraper_parser();
        s.AccessProfiles = config.isAccessProfiles();
        s.CurrentYearOnly = config.isCurrentYearOnly();
        s.logger = LOG;
        s.ctx = ctx;
        
        String profilyname = ctx.getWorkingDir() + "/profily.ttl";
        String zakazkyname = ctx.getWorkingDir() + "/zakazky.ttl";
        try {
			s.ps = new PrintStream(profilyname, "UTF-8");
			s.zak_ps = new PrintStream(zakazkyname, "UTF-8");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			LOG.error("Unexpected error opening filestreams for temp files", e);
		}

        String prefixes =
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
                "@prefix s:			 <http://schema.org/> .\n" +
                "@prefix authkinds:  <http://purl.org/procurement/public-contracts-authority-kinds#> .\n" +
                "@prefix proctypes:  <http://purl.org/procurement/public-contracts-procedure-types#> .\n" +
                "@prefix countries:  <http://linked.opendata.cz/resource/domain/buyer-profiles/country#> .\n" +
                "@prefix czstatus:   <http://purl.org/procurement/public-contracts-czech-statuses#> .\n" +
                    "\n" +
                "@prefix czbe:     <http://linked.opendata.cz/resource/business-entity/> .\n";        

        s.ps.println(prefixes);
        s.zak_ps.println(prefixes);

        // a spustim na vychozi stranku
        
	    java.util.Date date = new java.util.Date();
	    long start = date.getTime();
	    
	    try {
			//TODO: Vyresit cisteni cache... jak seznamy, tak profily.
	    	if (!ctx.canceled())
	    	{
		    	s.parse(new URL("http://www.vestnikverejnychzakazek.cz/en/Searching/ShowPublicPublisherProfiles"), "first");
			    s.parse(new URL("http://www.vestnikverejnychzakazek.cz/en/Searching/ShowRemovedProfiles"), "firstCancelled");
		        
	        	LOG.info("Parsing done. Passing RDF to ODCS");
		        try {
		        	contractsDataUnit.addFromTurtleFile(new File(zakazkyname));
		        	profilesDataUnit.addFromTurtleFile(new File(profilyname));
		        }
		        catch (RDFException e)
		        {
		        	LOG.error("Cannot put TTL to repository: " + e.getLocalizedMessage());
		        	throw new DPUException("Cannot put TTL to repository.", e);
		        }
	    	}
	    	if (ctx.canceled()) LOG.error("Interrputed");
		} catch (MalformedURLException e) {
			LOG.error("Unexpected malformed URL exception", e);
		} catch (InterruptedException e) {
			LOG.error("Interrputed");
		}
        
	    s.ps.close();
        s.zak_ps.close();
        
        java.util.Date date2 = new java.util.Date();
	    long end = date2.getTime();
	    ctx.sendMessage(MessageType.INFO, "");
	    ctx.sendMessage(MessageType.INFO, "Processed in " + (end-start) + "ms");
	    ctx.sendMessage(MessageType.INFO, "Rows: " + s.numrows);
	    ctx.sendMessage(MessageType.INFO, "Cancelled rows: " + s.totalcancellednumrows);
	    ctx.sendMessage(MessageType.INFO, "Warnings: " + s.numwarnings);
	    ctx.sendMessage(MessageType.INFO, "Errors: " + s.numerrors);
	    ctx.sendMessage(MessageType.INFO, "Missing ICOs on profile details: " + s.missingIco);
	    ctx.sendMessage(MessageType.INFO, "Missing ICOs in profile XML: " + s.missingIcoInProfile);
	    ctx.sendMessage(MessageType.INFO, "Invalid XML: " + s.invalidXML + " (" + Math.round((double)s.invalidXML*100/(double)s.numprofiles) + "%)");
	    ctx.sendMessage(MessageType.INFO, "Profiles: " + s.numprofiles);
	    ctx.sendMessage(MessageType.INFO, "Zakázky: " + s.numzakazky);
	    ctx.sendMessage(MessageType.INFO, "Uchazeči: " + s.numuchazeci);
	    ctx.sendMessage(MessageType.INFO, "Dodavatelé: " + s.numdodavatele);
	    ctx.sendMessage(MessageType.INFO, "Subdodavatelé: " + s.numsub);
	    ctx.sendMessage(MessageType.INFO, "Více dodavatelů u jedné zakázky: " + s.multiDodavatel);
        
    }
	
}
