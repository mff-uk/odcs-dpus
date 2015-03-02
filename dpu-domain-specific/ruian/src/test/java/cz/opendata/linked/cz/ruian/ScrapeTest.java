package cz.opendata.linked.cz.ruian;

import org.junit.Test;

import cz.cuni.mff.xrg.odcs.dpu.test.TestEnvironment;
import eu.unifiedviews.helpers.dpu.test.config.ConfigurationBuilder;

public class ScrapeTest {

    @Test
    public void constructAllTest() throws Exception {
        // prepare dpu instance and configure it
        Extractor extractor = new Extractor();
        ExtractorConfig config = new ExtractorConfig();

        config.setInterval(0);
        config.setTimeout(10000);
        config.setRewriteCache(false);
        config.setInclGeoData(true);
        config.setPassToOutput(false);

        extractor.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());

        // prepare test environment, we use system tmp directory
        TestEnvironment env = new TestEnvironment();
        // prepare input and output data units

        env.createFilesOutput("XMLObce");
        env.createFilesOutput("XMLZsj");

        // here we can simply pre-fill input data unit with content from 
        // resource file
        try {
            // run the execution
            env.run(extractor);

            // verify result
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // release resources
            env.release();
        }
    }
}
