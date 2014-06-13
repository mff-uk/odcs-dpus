package cz.opendata.linked.ehealth.ndfrt;

import org.junit.Test;

import cz.cuni.mff.xrg.odcs.commons.configuration.ConfigException;
import cz.cuni.mff.xrg.odcs.dpu.test.TestEnvironment;
import cz.cuni.mff.xrg.odcs.rdf.enums.RDFFormatType;
import cz.cuni.mff.xrg.odcs.rdf.exceptions.RDFException;
import cz.cuni.mff.xrg.odcs.rdf.interfaces.RDFDataUnit;
import cz.opendata.linked.ehealth.ndfrt.Extractor;
import cz.opendata.linked.ehealth.ndfrt.ExtractorConfig;

public class ExtractorTest {

	@Test
	public void test() {

		try	{
		
			Extractor extractor = new Extractor();
			ExtractorConfig config = new ExtractorConfig();
			
			extractor.configureDirectly(config);
			
			// prepare test environment, we use system tmp directory
			TestEnvironment env = TestEnvironment.create();
			// prepare input and output data units
			
			RDFDataUnit rdfOutput = env.createRdfOutput("XMLNDFRT", false);
	
			// here we can simply pre-fill input data unit with content from 
			// resource file
			
			try {
				// run the execution
				env.run(extractor);
				rdfOutput.loadToFile("C:\\temp\\ndfrt.ttl", RDFFormatType.TTL);
			}
		    catch(Exception e) {
			    e.printStackTrace();
		    } finally {
				// release resources
		    	env.release();
			}
			
		} catch (RDFException e)	{
			e.printStackTrace();
		} catch (ConfigException e) {
			e.printStackTrace();
		}
		
	}

}
