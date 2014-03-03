import org.junit.Test;

import cz.cuni.mff.xrg.odcs.dataunit.file.FileDataUnit;
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
		
		FileDataUnit smlouvy = env.createFileOutput("XMLSmlouvy");
		FileDataUnit objednavky = env.createFileOutput("XMLObjednavky");
		FileDataUnit plneni = env.createFileOutput("XMLPlneni");
		FileDataUnit smlouvy_roky = env.createFileOutput("XMLSmlouvy-RocniSeznam");
		FileDataUnit objednavky_roky = env.createFileOutput("XMLObjednavky-RocniSeznam");
		FileDataUnit plneni_roky = env.createFileOutput("XMLPlneni-RocniSeznam");
		RDFDataUnit smlouvy_meta = env.createRdfOutput("Smlouvy-Metadata", false);
		RDFDataUnit objednavky_meta = env.createRdfOutput("Objednavky-Metadata", false);
		RDFDataUnit plneni_meta = env.createRdfOutput("Plneni-Metadata", false);

		// here we can simply pre-fill input data unit with content from 
		// resource file
		
		try {
			// run the execution
			env.run(extractor);

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