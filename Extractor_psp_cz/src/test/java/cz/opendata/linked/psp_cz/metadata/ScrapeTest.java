package cz.opendata.linked.psp_cz.metadata;

import org.junit.Test;
import cz.cuni.mff.xrg.odcs.dpu.test.TestEnvironment;
import cz.cuni.mff.xrg.odcs.rdf.enums.RDFFormatType;
import cz.cuni.mff.xrg.odcs.rdf.interfaces.RDFDataUnit;

public class ScrapeTest {

	@Test
	public void constructAllTest() throws Exception {
		// prepare dpu instance and configure it
		Extractor extractor = new Extractor();
		ExtractorConfig config = new ExtractorConfig();

		config.setStart_year(2001);
		config.setEnd_year(2001);

		extractor.configureDirectly(config);

		// prepare test environment, we use system tmp directory
		TestEnvironment env = TestEnvironment.create();
		// prepare input and output data units

		RDFDataUnit psp_cz_metadata = env.createRdfOutput("output", false);

		// here we can simply pre-fill input data unit with content from 
		// resource file
		try {
			// run the execution
			env.run(extractor);

			psp_cz_metadata.loadToFile("C:\\temp\\pspcz.ttl", RDFFormatType.TTL);

			// verify result
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// release resources
			env.release();
		}
	}
}
