

import cz.cuni.mff.xrg.intlib.extractor.simplexslt.SimpleXSLT;
import cz.cuni.mff.xrg.intlib.extractor.simplexslt.SimpleXSLTConfig;
import cz.cuni.mff.xrg.odcs.commons.module.utils.DataUnitUtils;
import cz.cuni.mff.xrg.odcs.dataunit.file.FileDataUnit;
import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.rio.RDFFormat;

import cz.cuni.mff.xrg.odcs.dpu.test.TestEnvironment;
import cz.cuni.mff.xrg.odcs.rdf.enums.RDFFormatType;
import cz.cuni.mff.xrg.odcs.rdf.interfaces.RDFDataUnit;
import java.io.File;

public class XSLTTest {

	@BeforeClass
	public static void virtuoso() {
		// Adjust this to your virtuoso configuration.
		TestEnvironment.virtuosoConfig.host = "localhost";
		TestEnvironment.virtuosoConfig.port = "1111";
		TestEnvironment.virtuosoConfig.user = "dba";
		TestEnvironment.virtuosoConfig.password = "dba";
	}

        
	//@Test
	public void executeXSLTNsoudMetadataFileDataUnitOnly() throws Exception {
		
                SimpleXSLT trans = new SimpleXSLT();
		
		

                String xslt = DataUnitUtils.readFile("src/test/resources/decisionsMetadataExtractor.xslt");
		SimpleXSLTConfig config = new SimpleXSLTConfig(xslt,"name", SimpleXSLTConfig.OutputType.TTL, "text", "", 10);


		trans.configureDirectly(config);

		// prepare test environment
		TestEnvironment env = TestEnvironment.create();
		// prepare data units
//		RDFDataUnit input = env.createRdfInputFromResource("input", false,
//				"metadata.ttl", RDFFormat.TURTLE);
		FileDataUnit fileInput= env.createFileInputFromResource("fileInput", "input01.xml");
                RDFDataUnit rdfInput = env.createRdfInput("rdfInput", false);
                RDFDataUnit rdfOutput = env.createRdfOutput("rdfOutput", false);
                //RDFDataUnit rdfInput = 
                env.setJarPath("");

//		// some triples has been loaded 
//		assertTrue(input.getTripleCount() > 0);
		// run
		try {
			env.run(trans);

                        //stores output of the transformation
                        rdfOutput.loadToFile(new File("src/test/resources/output01.ttl"), RDFFormatType.TTL);
			// verify result
			assertTrue(rdfOutput.getTripleCount() > 0);
		} finally {
			// release resources
			env.release();
		}
	}
        
        
        //@Test
	public void executeXSLTNsoudMetadataFileDataUnitOnlyWithXSLTParams() throws Exception {
		
                SimpleXSLT trans = new SimpleXSLT();
		
		

                String xslt = DataUnitUtils.readFile("src/test/resources/decisionsMetadataExtractorParams.xslt");
		SimpleXSLTConfig config = new SimpleXSLTConfig(xslt,"name", SimpleXSLTConfig.OutputType.TTL, "text", "", 10);


		trans.configureDirectly(config);

		// prepare test environment
		TestEnvironment env = TestEnvironment.create();
		// prepare data units
//		RDFDataUnit input = env.createRdfInputFromResource("input", false,
//				"metadata.ttl", RDFFormat.TURTLE);
		FileDataUnit fileInput= env.createFileInputFromResource("fileInput", "input01.xml");
                RDFDataUnit rdfInput = env.createRdfInput("rdfInput", false);
                rdfInput.addFromFile(new File("src/test/resources/input01BxsltParams.ttl"), RDFFormat.TURTLE);
                RDFDataUnit rdfOutput = env.createRdfOutput("rdfOutput", false);
                //RDFDataUnit rdfInput = 
                env.setJarPath("");

//		// some triples has been loaded 
//		assertTrue(input.getTripleCount() > 0);
		// run
		try {
			env.run(trans);

                        //stores output of the transformation
                        rdfOutput.loadToFile(new File("src/test/resources/output01B.ttl"), RDFFormatType.TTL);
			// verify result
			assertTrue(rdfOutput.getTripleCount() > 0);
		} finally {
			// release resources
			env.release();
		}
	}
        
        
        @Test
	public void executeXSLTNsoudMetadataFileDataUnitWithFileURISpecified() throws Exception {
		
                SimpleXSLT trans = new SimpleXSLT();
		
		

                String xslt = DataUnitUtils.readFile("src/test/resources/decisionsMetadataExtractor.xslt");
		SimpleXSLTConfig config = new SimpleXSLTConfig(xslt,"name", SimpleXSLTConfig.OutputType.Literal, "text", "", 10);


		trans.configureDirectly(config);

		// prepare test environment
		TestEnvironment env = TestEnvironment.create();
		// prepare data units
//		RDFDataUnit input = env.createRdfInputFromResource("input", false,
//				"metadata.ttl", RDFFormat.TURTLE);
		FileDataUnit fileInput= env.createFileInputFromResource("fileInput", "input01.xml");
                
              
               
                RDFDataUnit rdfInput = env.createRdfInput("rdfInput", false);
                rdfInput.addFromFile(new File("src/test/resources/input01SubjectMappings.ttl"), RDFFormat.TURTLE);
                RDFDataUnit rdfOutput = env.createRdfOutput("rdfOutput", false);
                //RDFDataUnit rdfInput = 
                env.setJarPath("");

//		// some triples has been loaded 
//		assertTrue(input.getTripleCount() > 0);
		// run
		try {
			env.run(trans);

                        //stores output of the transformation
                        rdfOutput.loadToFile(new File("src/test/resources/output02.ttl"), RDFFormatType.TTL);
			// verify result
			assertTrue(rdfOutput.getTripleCount() > 0);
		} finally {
			// release resources
			env.release();
		}
	}
        
        //@Test
	public void executeXSLTNsoudMetadataRDFDataUnitONLY() throws Exception {
		
                SimpleXSLT trans = new SimpleXSLT();
		
		

                String xslt = DataUnitUtils.readFile("src/test/resources/decisionsMetadataExtractor.xslt");
		SimpleXSLTConfig config = new SimpleXSLTConfig(xslt,"name", SimpleXSLTConfig.OutputType.TTL, "text", "", 10);


		trans.configureDirectly(config);

		// prepare test environment
		TestEnvironment env = TestEnvironment.create();
		// prepare data units
//		RDFDataUnit input = env.createRdfInputFromResource("input", false,
//				"metadata.ttl", RDFFormat.TURTLE);
		//FileDataUnit fileInput= env.createFileInputFromResource("fileInput", "input01.xml");
                //RDFDataUnit rdfInput= env.createRdfInputFromResource("rdfInput", false, "input02.xml", RDFFormat.TURTLE);
                RDFDataUnit rdfInput = env.createRdfInput("rdfInput", false);
                rdfInput.addFromFile(new File("src/test/resources/input02.xml"), RDFFormat.TURTLE);
                RDFDataUnit rdfOutput = env.createRdfOutput("rdfOutput", false);
                //RDFDataUnit rdfInput = 
                env.setJarPath("");

//		// some triples has been loaded 
//		assertTrue(input.getTripleCount() > 0);
		// run
		try {
			env.run(trans);

                        //stores output of the transformation
                        rdfOutput.loadToFile(new File("src/test/resources/output03.ttl"), RDFFormatType.TTL);
			// verify result
			//assertTrue(input.getTripleCount() == output.getTripleCount());
                        assertTrue(rdfOutput.getTripleCount() > 0);
		} finally {
			// release resources
			env.release();
		}
	}
        
        //@Test
	public void executeXSLTNsoudMetadataBothDataUnits() throws Exception {
		
                SimpleXSLT trans = new SimpleXSLT();
		
		

                String xslt = DataUnitUtils.readFile("src/test/resources/decisionsMetadataExtractor.xslt");
		SimpleXSLTConfig config = new SimpleXSLTConfig(xslt,"name", SimpleXSLTConfig.OutputType.TTL, "text", "", 10);


		trans.configureDirectly(config);

		// prepare test environment
		TestEnvironment env = TestEnvironment.create();
		// prepare data units
//		RDFDataUnit input = env.createRdfInputFromResource("input", false,
//				"metadata.ttl", RDFFormat.TURTLE);
		FileDataUnit fileInput= env.createFileInputFromResource("fileInput", "input01.xml");
                RDFDataUnit rdfInput= env.createRdfInputFromResource("rdfInput", false, "input02.xml", RDFFormat.TURTLE);
                 RDFDataUnit rdfInputTest= env.createRdfInputFromResource("rdfTest", false, "input01B.xml", RDFFormat.TURTLE);
                //RDFDataUnit rdfInput = env.createRdfInput("rdfInput", false);
                RDFDataUnit rdfOutput = env.createRdfOutput("rdfOutput", false);
                //RDFDataUnit rdfInput = 
                env.setJarPath("");

//		// some triples has been loaded 
//		assertTrue(input.getTripleCount() > 0);
		// run
		try {
			env.run(trans);

                        //stores output of the transformation
                        rdfOutput.loadToFile(new File("src/test/resources/output04.ttl"), RDFFormatType.TTL);
			// verify result
			assertTrue(rdfOutput.getTripleCount() > rdfInput.getTripleCount());
                       
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
