package cz.opendata.linked.buyer_profiles;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.css_parser.utils.Cache;
import cz.cuni.xrg.intlib.commons.data.DataUnitCreateException;
import cz.cuni.xrg.intlib.commons.data.DataUnitType;
import cz.cuni.xrg.intlib.commons.extractor.Extract;
import cz.cuni.xrg.intlib.commons.extractor.ExtractContext;
import cz.cuni.xrg.intlib.commons.extractor.ExtractException;
import cz.cuni.xrg.intlib.commons.module.dpu.ConfigurableBase;
import cz.cuni.xrg.intlib.commons.web.AbstractConfigDialog;
import cz.cuni.xrg.intlib.commons.web.ConfigDialogProvider;
import cz.cuni.xrg.intlib.rdf.exceptions.RDFException;
import cz.cuni.xrg.intlib.rdf.interfaces.RDFDataRepository;

public class Extractor 
        extends ConfigurableBase<ExtractorConfig> 
        implements Extract, ConfigDialogProvider<ExtractorConfig> {

	/**
	 * DPU's configuration.
	 */
	
	private Logger logger = LoggerFactory.getLogger(Extract.class);

    public Extractor() {
        super(new ExtractorConfig());
    }
	 
    @Override
	public AbstractConfigDialog<ExtractorConfig> getConfigurationDialog() {		
		return new ExtractorDialog();
	}
	
	public void extract(ExtractContext ctx) throws ExtractException
	{
        // vytvorime si parser
        
    	Cache.logger = logger;
    	Cache.rewriteCache = config.rewriteCache;
    	Cache.setBaseDir(ctx.getUserDirectory() + "/cache/");
    	Cache.setTimeout(config.timeout);
    	Cache.setInterval(config.interval);
        try {
			Cache.setTrustAllCerts();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        Scraper_parser s = new Scraper_parser();
        s.AccessProfiles = config.accessProfiles;
        s.logger = logger;
        
        String profilyname = ctx.getWorkingDir() + "/profily.ttl";
        String zakazkyname = ctx.getWorkingDir() + "/zakazky.ttl";
        try {
			s.ps = new PrintStream(profilyname, "UTF-8");
			s.zak_ps = new PrintStream(zakazkyname, "UTF-8");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	    	
	    	s.parse(new URL("http://www.vestnikverejnychzakazek.cz/en/Searching/ShowPublicPublisherProfiles"), "first");
		    s.parse(new URL("http://www.vestnikverejnychzakazek.cz/en/Searching/ShowRemovedProfiles"), "firstCancelled");
		} catch (MalformedURLException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    java.util.Date date2 = new java.util.Date();
	    long end = date2.getTime();
	    logger.info("Processed in " + (end-start) + "ms");
	    logger.info("Rows: " + s.numrows);
	    logger.info("Cancelled rows: " + s.totalcancellednumrows);
	    logger.info("Warnings: " + s.numwarnings);
	    logger.info("Errors: " + s.numerrors);
	    logger.info("Missing ICOs on profile details: " + s.missingIco);
	    logger.info("Missing ICOs in profile XML: " + s.missingIcoInProfile);
	    logger.info("Invalid XML: " + s.invalidXML + " (" + Math.round((double)s.invalidXML*100/(double)s.numprofiles) + "%)");
	    logger.info("Profiles: " + s.numprofiles);
	    logger.info("Zakázky: " + s.numzakazky);
	    logger.info("Uchazeči: " + s.numuchazeci);
	    logger.info("Dodavatelé: " + s.numdodavatele);
	    logger.info("Subdodavatelé: " + s.numsub);
	    logger.info("Více dodavatelů u jedné zakázky: " + s.multiDodavatel);
        
        s.ps.close();
        s.zak_ps.close();

        //give ttl to odcs
        RDFDataRepository contractsRepository;
        RDFDataRepository profilesRepository;
        try {
        	contractsRepository = (RDFDataRepository) ctx.addOutputDataUnit(DataUnitType.RDF, "contracts");
        	profilesRepository = (RDFDataRepository) ctx.addOutputDataUnit(DataUnitType.RDF, "profiles");
        } catch (DataUnitCreateException e) {
            logger.error("Can't create DataUnit");
        	throw new ExtractException("Can't create DataUnit", e);
        }
        try {
        	contractsRepository.extractFromLocalTurtleFile(zakazkyname);
        	profilesRepository.extractFromLocalTurtleFile(profilyname);
        }
        catch (RDFException e)
        {
        	logger.error("Cannot put TTL to repository.");
        	throw new ExtractException("Cannot put TTL to repository.", e);
        }
        
    }
}
