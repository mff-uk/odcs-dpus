package cz.opendata.linked.buyer_profiles;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import cz.cuni.mff.css_parser.utils.Cache;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;

import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.files.simple.WritableSimpleFiles;
import eu.unifiedviews.helpers.dpu.extension.rdf.simple.WritableSimpleRdf;

@DPU.AsExtractor
public class Extractor extends AbstractDpu<ExtractorConfig> {
    
    @DataUnit.AsOutput(name = "contracts")
    public WritableFilesDataUnit filesContractsDataUnit;

    @DataUnit.AsOutput(name = "profiles")
    public WritableFilesDataUnit filesProfilesDataUnit;
   
    @DataUnit.AsOutput(name = "profile_statistics")
    public WritableRDFDataUnit filesProfileStatistics;

    @ExtensionInitializer.Init(param = "filesContractsDataUnit")
    public WritableSimpleFiles contractsDataUnit;

    @ExtensionInitializer.Init(param = "filesProfilesDataUnit")
    public WritableSimpleFiles profilesDataUnit;

    @ExtensionInitializer.Init(param = "filesProfileStatistics")
    public WritableSimpleRdf profileStatistics;


    private static final Logger LOG = LoggerFactory.getLogger(DPU.class);

    public Extractor() {
        super(ExtractorDialog.class, ConfigHistory.noHistory(ExtractorConfig.class));
    }
    
    @Override
    protected void innerExecute() throws DPUException
    {
        DPUContext context = ctx.getExecMasterContext().getDpuContext();

        Cache.logger = LOG;
        Cache.rewriteCache = config.isRewriteCache();
        Cache.setBaseDir(context.getUserDirectory() + "/cache/");
        Cache.setTimeout(config.getTimeout());
        Cache.setInterval(config.getInterval());
        Cache.stats = profileStatistics;
        Cache.validate = config.isValidateXSD();
                
        /*set up xsd validation*/
        // create a SchemaFactory capable of understanding WXS schemas
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        // load a WXS schema, represented by a Schema instance
        // http://www.isvz.cz/ProfilyZadavatelu/Profil_Zadavatele_SchemaVZ.xsd
        Source schemaFile = new StreamSource("http://www.isvz.cz/ProfilyZadavatelu/Profil_Zadavatele_SchemaVZ.xsd");
        //Source schemaFile = new StreamSource(new File(context.getUserDirectory(), "Profil_Zadavatele_SchemaVZ.xsd"));
        Schema schema;
        try {
            schema = factory.newSchema(schemaFile);
        
            // create a Validator instance, which can be used to validate an instance document
            Cache.validator = schema.newValidator();        
        } catch (SAXException e) {
            LOG.error("Failed to create validator", e);
        }
        /*end of set up xsd validation*/
        
        try {
            Cache.setTrustAllCerts();
        } catch (Exception e) {
            LOG.error("Unexpected error when setting trust to all certificates. ", e);
        }
        
        Scraper_parser s = new Scraper_parser();
        s.AccessProfiles = config.isAccessProfiles();
        s.CurrentYearOnly = config.isCurrentYearOnly();
        s.maxAttempts = config.getMaxAttempts();
        s.logger = LOG;
        s.context = context;
        
        String profilyname = context.getWorkingDir() + "/profily.ttl";
        String zakazkyname = context.getWorkingDir() + "/zakazky.ttl";
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
                "@prefix s:             <http://schema.org/> .\n" +
                "@prefix authkinds:  <http://purl.org/procurement/public-contracts-authority-kinds#> .\n" +
                "@prefix proctypes:  <http://purl.org/procurement/public-contracts-procedure-types#> .\n" +
                "@prefix countries:  <http://linked.opendata.cz/resource/domain/buyer-profiles/country#> .\n" +
                "@prefix czstatus:   <http://purl.org/procurement/public-contracts-czech-statuses#> .\n" +
                    "\n" +
                "@prefix czbe:     <http://linked.opendata.cz/resource/business-entity/> .\n";        

        s.ps.println(prefixes);
        s.zak_ps.println(prefixes);
        s.pstats = profileStatistics;
        s.valueFactory = profileStatistics.getValueFactory();
        // a spustim na vychozi stranku
        
        java.util.Date date = new java.util.Date();
        long start = date.getTime();
        
        try {
            //TODO: Vyresit cisteni cache... jak seznamy, tak profily.
            if (!context.canceled())
            {
                s.parse(new URL("http://www.vestnikverejnychzakazek.cz/en/Searching/ShowPublicPublisherProfiles"), "first");
                s.parse(new URL("http://www.vestnikverejnychzakazek.cz/en/Searching/ShowRemovedProfiles"), "firstCancelled");
                
                LOG.info("Parsing done. Passing RDF to UV");
                contractsDataUnit.add(new File(zakazkyname), zakazkyname);
                profilesDataUnit.add(new File(profilyname), profilyname);
            }
            if (context.canceled()) LOG.error("Interrputed");
        } catch (MalformedURLException e) {
            LOG.error("Unexpected malformed URL exception", e);
        } catch (InterruptedException e) {
            LOG.error("Interrputed");
        }
        
        s.ps.close();
        s.zak_ps.close();
        
        java.util.Date date2 = new java.util.Date();
        long end = date2.getTime();
        context.sendMessage(DPUContext.MessageType.INFO, "");
        context.sendMessage(DPUContext.MessageType.INFO, "Processed in " + (end-start) + "ms");
        context.sendMessage(DPUContext.MessageType.INFO, "Rows: " + s.numrows);
        context.sendMessage(DPUContext.MessageType.INFO, "Cancelled rows: " + s.totalcancellednumrows);
        context.sendMessage(DPUContext.MessageType.INFO, "Warnings: " + s.numwarnings);
        context.sendMessage(DPUContext.MessageType.INFO, "Errors: " + s.numerrors);
        context.sendMessage(DPUContext.MessageType.INFO, "Missing ICOs on profile details: " + s.missingIco);
        context.sendMessage(DPUContext.MessageType.INFO, "Missing ICOs in profile XML: " + s.missingIcoInProfile);
        context.sendMessage(DPUContext.MessageType.INFO, "Invalid XML: " + s.invalidXML + " (" + Math.round((double)s.invalidXML*100/(double)s.numprofiles) + "%)");
        if (config.isValidateXSD()) {
            context.sendMessage(DPUContext.MessageType.INFO, "Valid XSD/XML: " + Cache.validXML);
            context.sendMessage(DPUContext.MessageType.INFO, "Invalid XSD/XML: " + Cache.invalidXML);
            context.sendMessage(DPUContext.MessageType.INFO, "Time spent validating XSD/XML: " + Cache.timeValidating);
        }
        context.sendMessage(DPUContext.MessageType.INFO, "Profiles: " + s.numprofiles);
        context.sendMessage(DPUContext.MessageType.INFO, "Zakázky: " + s.numzakazky);
        context.sendMessage(DPUContext.MessageType.INFO, "Uchazeči: " + s.numuchazeci);
        context.sendMessage(DPUContext.MessageType.INFO, "Dodavatelé: " + s.numdodavatele);
        context.sendMessage(DPUContext.MessageType.INFO, "Subdodavatelé: " + s.numsub);
        context.sendMessage(DPUContext.MessageType.INFO, "Více dodavatelů u jedné zakázky: " + s.multiDodavatel);        
    }

}
