import org.junit.Test;

import cz.cuni.mff.xrg.odcs.dpu.test.TestEnvironment;
import cz.cuni.mff.xrg.odcs.rdf.enums.RDFFormatType;
import cz.cuni.mff.xrg.odcs.rdf.interfaces.RDFDataUnit;
import cz.opendata.linked.cz.gov.smlouvy.Extractor;
import cz.opendata.linked.cz.gov.smlouvy.ExtractorConfig;


public class ScrapeTest {
	@Test
	public void constructAllTest() throws Exception {
		// prepare dpu instance and configure it
		Extractor extractor = new Extractor();
		ExtractorConfig config = new ExtractorConfig();
		
		config.interval = 0;
		config.timeout = 10000;
		config.rewriteCache = false;
		
		extractor.configureDirectly(config);
		
		// prepare test environment, we use system tmp directory
		TestEnvironment env = TestEnvironment.create();
		// prepare input and output data units
		
		RDFDataUnit smlouvy = env.createRdfOutput("XMLSmlouvy", false);
		RDFDataUnit objednavky = env.createRdfOutput("XMLObjednavky", false);
		RDFDataUnit plneni = env.createRdfOutput("XMLPlneni", false);
		RDFDataUnit smlouvy_roky = env.createRdfOutput("XMLSmlouvy-RocniSeznam", false);
		RDFDataUnit objednavky_roky = env.createRdfOutput("XMLObjednavky-RocniSeznam", false);
		RDFDataUnit plneni_roky = env.createRdfOutput("XMLPlneni-RocniSeznam", false);

		// here we can simply pre-fill input data unit with content from 
		// resource file
		
		try {
			// run the execution
			env.run(extractor);

			smlouvy.loadToFile("C:\\temp\\smlouvy.ttl", RDFFormatType.TTL);
			objednavky.loadToFile("C:\\temp\\objednavky.ttl", RDFFormatType.TTL);
			plneni.loadToFile("C:\\temp\\plneni.ttl", RDFFormatType.TTL);
			smlouvy_roky.loadToFile("C:\\temp\\smlouvy_roky.ttl", RDFFormatType.TTL);
			objednavky_roky.loadToFile("C:\\temp\\objednavky_roky.ttl", RDFFormatType.TTL);
			plneni_roky.loadToFile("C:\\temp\\plneni_roky.ttl", RDFFormatType.TTL);
			
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