package cz.cuni.mff.xrg.intlib.extractor.legislation.decisions.usoud;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.rio.RDFFormat;

import cz.cuni.mff.xrg.odcs.dpu.test.TestEnvironment;
import cz.cuni.mff.xrg.odcs.rdf.interfaces.RDFDataUnit;

public class ExtractorTest {

	@BeforeClass
	public static void virtuoso() {
		// Adjust this to your virtuoso configuration.
		TestEnvironment.virtuosoConfig.host = "localhost";
		TestEnvironment.virtuosoConfig.port = "1111";
		TestEnvironment.virtuosoConfig.user = "dba";
		TestEnvironment.virtuosoConfig.password = "dba";
	}

	//@Test
	public void constructAllTest() throws Exception {
		// prepare dpu
		Extractor trans = new Extractor();
		
		

		ExtractorConfig config = new ExtractorConfig();


		trans.configureDirectly(config);

		// prepare test environment
		TestEnvironment env = TestEnvironment.create();
		// prepare data units
//		RDFDataUnit input = env.createRdfInputFromResource("input", false,
//				"metadata.ttl", RDFFormat.TURTLE);
		RDFDataUnit output = env.createRdfOutput("output", false);
                env.setJarPath("");

//		// some triples has been loaded 
//		assertTrue(input.getTripleCount() > 0);
		// run
		try {
			env.run(trans);

			// verify result
			//assertTrue(input.getTripleCount() == output.getTripleCount());
		} finally {
			// release resources
			env.release();
		}
	}

//	// @Test
//	public void constructAllTestVirtuoso() throws Exception {
//		// prepare dpu
//		SPARQLTransformer trans = new SPARQLTransformer();
//		boolean isConstructType = true;
//		String SPARQL_Update_Query = "CONSTRUCT {?s ?p ?o} where {?s ?p ?o }";
//		
//		SPARQLTransformerConfig config = new SPARQLTransformerConfig(
//				SPARQL_Update_Query, isConstructType);
//		
//		trans.configureDirectly(config);
//
//		// prepare test environment
//		TestEnvironment env = TestEnvironment.create();
//		// prepare data units
//		RDFDataUnit input = env.createRdfInputFromResource("input", true,
//				"metadata.ttl", RDFFormat.TURTLE);
//		RDFDataUnit output = env.createRdfOutput("output", true);
//
//		// some triples has been loaded 
//		assertTrue(input.getTripleCount() > 0);
//		// run
//		try {
//			env.run(trans);
//
//			// verify result
//			assertTrue(input.getTripleCount() == output.getTripleCount());
//		} finally {
//			// release resources
//			env.release();
//		}
//	}
}
