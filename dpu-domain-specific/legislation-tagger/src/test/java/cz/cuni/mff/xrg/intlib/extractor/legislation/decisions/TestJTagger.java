package cz.cuni.mff.xrg.intlib.extractor.legislation.decisions;

import org.junit.Test;

import cz.cuni.mff.xrg.odcs.dpu.test.TestEnvironment;
import cz.cuni.mff.xrg.odcs.rdf.RDFDataUnit;

public class TestJTagger {

//	@BeforeClass
//	public static void virtuoso() {
//		// Adjust this to your virtuoso configuration.
//		TestEnvironment.virtuosoConfig.host = "localhost";
//		TestEnvironment.virtuosoConfig.port = "1111";
//		TestEnvironment.virtuosoConfig.user = "dba";
//		TestEnvironment.virtuosoConfig.password = "dba";
//	}
	@Test
	public void testLocally() throws Exception {

		JTaggerAnnotator trans = new JTaggerAnnotator();

		JTaggerAnnotatorConfig config = new JTaggerAnnotatorConfig();

		trans.configureDirectly(config);

		// prepare test environment
		TestEnvironment env = new TestEnvironment();
		// prepare data units
		RDFDataUnit rdfInput = env.createRdfInput("input", false);
//		rdfInput.addFromFile(new File("src/test/resources/input.ttl"),
//				RDFFormat.TURTLE);
		RDFDataUnit rdfOutput = env.createRdfOutput("output", false);
		env.setJarPath("");

		//run 
		try {
			env.run(trans);
		} finally {
			// release resources
			env.release();
		}
	}

}
