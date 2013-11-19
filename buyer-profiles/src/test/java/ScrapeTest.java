import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;
import org.openrdf.rio.RDFFormat;

import cz.cuni.mff.xrg.odcs.dpu.test.TestEnvironment;
import cz.cuni.mff.xrg.odcs.rdf.enums.RDFFormatType;
import cz.cuni.mff.xrg.odcs.rdf.interfaces.RDFDataUnit;
import cz.opendata.linked.buyer_profiles.Extractor;
import cz.opendata.linked.buyer_profiles.ExtractorConfig;


public class ScrapeTest {
	@Test
	public void constructAllTest() throws Exception {
		// prepare dpu instance and configure it
		Extractor extractor = new Extractor();
		ExtractorConfig config = new ExtractorConfig();
		
		config.rewriteCache = false;
		config.interval = 2000;
		config.timeout = 10000;
		config.currentYearOnly = true;
		config.accessProfiles = true;
		
		
		extractor.configureDirectly(config);
		
	
		// prepare test environment, we use system tmp directory
		TestEnvironment env = TestEnvironment.create();
		// prepare input and output data units
		
		// here we can simply pre-fill input data unit with content from 
		// resource file
		
		RDFDataUnit contracts = env.createRdfOutput("contracts", false);
		RDFDataUnit profiles = env.createRdfOutput("profiles", false);
		
		try {
			// run the execution
			env.run(extractor);
			
			contracts.loadToFile("C:\\temp\\contracts.ttl", RDFFormatType.TTL);
			profiles.loadToFile("C:\\temp\\profiles.ttl", RDFFormatType.TTL);
			
		}
	    catch(Exception e) {
		    	e.printStackTrace();
	    } finally {
			// release resources
			env.release();
		}
	}
}