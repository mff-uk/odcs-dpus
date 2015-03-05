package cz.opendata.linked.buyer_profiles;

import cz.cuni.mff.xrg.odcs.dpu.test.TestEnvironment;

import org.junit.Test;

import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;

import java.io.File;

import org.openrdf.rio.RDFFormat;

import eu.unifiedviews.helpers.dpu.test.config.ConfigurationBuilder;
import eu.unifiedviews.helpers.dpu.test.rdf.InputOutputUtils;

public class ScrapeTest {
    @Test
    public void constructAllTest() throws Exception {
        // prepare dpu instance and configure it
        Extractor extractor = new Extractor();

        ExtractorConfig config = new ExtractorConfig();        
        config.setRewriteCache(false);
        config.setInterval(0);
        config.setTimeout(2000);
        config.setMaxAttempts(1);
        config.setValidateXSD(true);
        config.setCurrentYearOnly(true);
        config.setAccessProfiles(true);        

        // configure DPU
        extractor.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());
       
        // prepare test environment, we use system tmp directory
        TestEnvironment env = new TestEnvironment();
        // prepare input and output data units
        
        // here we can simply pre-fill input data unit with content from 
        // resource file
        
        WritableFilesDataUnit contracts = env.createFilesOutput("contracts");
        WritableFilesDataUnit profiles = env.createFilesOutput("profiles");
        RDFDataUnit profile_stats = env.createRdfOutput("profile_statistics", false);
        
        try {
            // run the execution
            env.run(extractor);
// TODO: add loadToFile method - possibly into test environment?
            //InputOutput.loadToFile(contracts, new File("C:\\temp\\contracts.ttl"), RDFFormat.TURTLE);
            //InputOutput.loadToFile(profiles, new File("C:\\temp\\profiles.ttl"), RDFFormat.TURTLE);
           // InputOutputUtils.loadToFile(profile_stats, new File("C:\\temp\\profile_stats.ttl"), RDFFormat.TURTLE);
        }
        catch(Exception e) {
            e.printStackTrace();
        } finally {
            // release resources
            env.release();
        }
    }
}