package cz.cuni.mff.xrg.intlib.extractor.legislation.decisions.uriGenerator;

import com.google.gwt.thirdparty.guava.common.io.Files;
import static org.junit.Assert.*;

import cz.cuni.mff.xrg.intlib.extractor.legislation.decisions.UriGenerator;
import cz.cuni.mff.xrg.intlib.extractor.legislation.decisions.UriGeneratorConfig;
import org.junit.Test;
import org.openrdf.rio.RDFFormat;

import cz.cuni.mff.xrg.odcs.dpu.test.TestEnvironment;
import cz.cuni.mff.xrg.odcs.dpu.test.context.TestContext;
import cz.cuni.mff.xrg.odcs.rdf.enums.RDFFormatType;
import cz.cuni.mff.xrg.odcs.rdf.interfaces.RDFDataUnit;
import java.io.File;
import java.util.UUID;

public class URIGenTest {

	@Test
	public void testActsURICreation() throws Exception {
		// prepare dpu
		
                File conf = new File("src/test/resources/config/uriGenConfig.xml");
                
//  
//                //copy to temp folder, so that it is easily accessible by the URI Generator called from the DPU
//                File tempConf =  new File(System.getProperty("java.io.tmpdir") + File.separator + "xsltDPU" + UUID.randomUUID().toString());
//                Files.copy(conf, tempConf);
//            
                UriGenerator trans = new UriGenerator();
		UriGeneratorConfig config = new UriGeneratorConfig("",conf.getCanonicalPath());
            
		
		trans.configureDirectly(config);

		// TODO prepare test environment (specify the directory for working dir, you can leave empty for temp dir )
		TestEnvironment env = TestEnvironment.create(new File("/Users/tomasknap/Documents/tmp/test"));
		// prepare data units
		RDFDataUnit input = env.createRdfInputFromResource("input",
				"input/1.ttl", RDFFormat.TURTLE);
		RDFDataUnit output = env.createRdfOutput("output");
            
               
                
                
		// some triples has been loaded 
		assertTrue(input.getTripleCount() > 0);
		// run
		try {
			env.run(trans);

                        //to write output to file:
                        output.loadToFile("/Users/tomasknap/Documents/tmp/1.ttl", RDFFormatType.TTL);
                        
                        //test certain aspects
			// assertTrue(input.getTripleCount() == output.getTripleCount());
		} finally {
			// release resources
			env.release();
		}
	}

}
