package cz.opendata.linked.geocoder.google;

import org.junit.Test;

import cz.cuni.mff.xrg.odcs.dpu.test.TestEnvironment;
import cz.cuni.mff.xrg.odcs.rdf.RDFDataUnit;

public class ScrapeTest {
	@Test
	public void constructAllTest() throws Exception {
		// prepare dpu instance and configure it
		Extractor extractor = new Extractor();
		ExtractorConfig config = new ExtractorConfig();
		
		
		extractor.configureDirectly(config);
		
		// prepare test environment, we use system tmp directory
		TestEnvironment env = new TestEnvironment();
		// prepare input and output data units
		
//		RDFDataUnit sAddrs = env.createRdfInputFromResource("Schema.org addresses", false,
//				"addresses.ttl", RDFFormat.TURTLE);
		RDFDataUnit geoCoord = env.createRdfOutput("Geocoordinates", false);

		// here we can simply pre-fill input data unit with content from 
		// resource file
		
		try {
			// run the execution
			env.run(extractor);

//			geoCoord.loadToFile("C:\\temp\\geo.ttl", RDFFormatType.TTL);
			
			// verify result
		}
	    catch(Exception e) {
		    	e.printStackTrace();
	    } finally {
			// release resources
			env.release();
		}
	}
}