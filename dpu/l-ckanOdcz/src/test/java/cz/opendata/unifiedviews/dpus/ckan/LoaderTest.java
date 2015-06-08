package cz.opendata.unifiedviews.dpus.ckan;

import org.junit.Test;
import org.openrdf.rio.RDFFormat;

import cz.cuni.mff.xrg.odcs.dpu.test.TestEnvironment;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.helpers.dataunit.rdf.RdfDataUnitUtils;
import eu.unifiedviews.helpers.dpu.test.config.ConfigurationBuilder;
import eu.unifiedviews.helpers.dpu.test.rdf.InputOutputUtils;
import eu.unifiedviews.helpers.dpu.test.resources.ResourceUtils;

public class LoaderTest {

    @Test
    public void constructAllTest() throws Exception {
        // prepare dpu instance and configure it
        CKANLoader loader = new CKANLoader();
        CKANLoaderConfig_V3 config = new CKANLoaderConfig_V3();

//        config.setDatasetID("cz-test");
        config.setApiKey("848ee776-4003-4f20-80fc-44ffad44087f");
        config.setLoadToCKAN(false);

        loader.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());

        // prepare test environment, we use system tmp directory
        TestEnvironment env = new TestEnvironment();
        // prepare input and output data units

        WritableRDFDataUnit input = env.createRdfInput("metadata", false);
        WritableFilesDataUnit json = env.createFilesOutput("JSON");

        // here we can simply pre-fill input data unit with content from 
        // resource file
        
        InputOutputUtils.extractFromFile(ResourceUtils.getFile("input.ttl"), RDFFormat.TURTLE, input,
                RdfDataUnitUtils.addGraph(input, "input"));        
        try {
        	// run the execution
            env.run(loader);

            // verify result
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // release resources
            env.release();
        }
    }
}
