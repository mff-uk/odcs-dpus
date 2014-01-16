import org.junit.Test;
import org.openrdf.rio.RDFFormat;

import cz.cuni.mff.xrg.odcs.dpu.test.TestEnvironment;
import cz.cuni.mff.xrg.odcs.rdf.enums.RDFFormatType;
import cz.cuni.mff.xrg.odcs.rdf.interfaces.RDFDataUnit;
import cz.opendata.linked.geocoder.krovak.Extractor;
import cz.opendata.linked.geocoder.krovak.ExtractorConfig;


public class ScrapeTest {
	@Test
	public void constructAllTest() throws Exception {
		// prepare dpu instance and configure it
		Extractor extractor = new Extractor();
		ExtractorConfig config = new ExtractorConfig();
		
		
		extractor.configureDirectly(config);
		
		// prepare test environment, we use system tmp directory
		TestEnvironment env = TestEnvironment.create();
		// prepare input and output data units
		
		RDFDataUnit sPoints = env.createRdfInputFromResource("gmlPoint points", false,
				"addresses.ttl", RDFFormat.TURTLE);
		RDFDataUnit geoCoord = env.createRdfOutput("Geocoordinates", false);

		// here we can simply pre-fill input data unit with content from 
		// resource file
		
		try {
			// run the execution
			env.run(extractor);

			geoCoord.loadToFile("C:\\temp\\geo.ttl", RDFFormatType.TTL);
			
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