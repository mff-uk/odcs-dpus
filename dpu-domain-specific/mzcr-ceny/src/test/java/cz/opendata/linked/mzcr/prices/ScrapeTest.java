package cz.opendata.linked.mzcr.prices;

import org.junit.Test;
import org.openrdf.rio.RDFFormat;

import cz.cuni.mff.xrg.odcs.dpu.test.TestEnvironment;
import cz.cuni.mff.xrg.odcs.rdf.RDFDataUnit;

public class ScrapeTest {

	@Test
	public void constructAllTest() throws Exception {
		// prepare dpu instance and configure it
		Extractor extractor = new Extractor();
		ExtractorConfig config = new ExtractorConfig();

		config.setRewriteCache(true);
		config.setInterval(2000);
		config.setTimeout(10000);
		extractor.configureDirectly(config);

		// prepare test environment, we use system tmp directory
		TestEnvironment env = new TestEnvironment();
		// prepare input and output data units

		// here we can simply pre-fill input data unit with content from 
		// resource file
//		RDFDataUnit input = env.createRdfInputFromResource("input", false,
//				"prefixes.ttl", RDFFormat.TURTLE);
//		RDFDataUnit output = env.createRdfOutput("output", false);

		// first test - check that something has been loaded into input data unit 
//		assertTrue(input.getTripleCount() > 0);

		try {
			// run the execution
//			env.run(extractor);

//			output.loadToFile("C:\\temp\\out.ttl", RDFFormatType.TTL);

			// verify result
//			assertTrue(input.getTripleCount() == output.getTripleCount());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// release resources
			env.release();
		}
	}
}
