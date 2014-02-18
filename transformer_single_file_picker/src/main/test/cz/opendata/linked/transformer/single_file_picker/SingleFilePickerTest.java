package cz.opendata.linked.transformer.single_file_picker;

import java.io.IOException;

import org.junit.Test;

import cz.cuni.mff.xrg.odcs.commons.configuration.ConfigException;
import cz.cuni.mff.xrg.odcs.dataunit.file.FileDataUnit;
import cz.cuni.mff.xrg.odcs.dataunit.file.handlers.DirectoryHandler;
import cz.cuni.mff.xrg.odcs.dpu.test.TestEnvironment;
import cz.opendata.linked.transformer.single_file_picker.SingleFilePicker;
import cz.opendata.linked.transformer.single_file_picker.SingleFilePickerConfig;

public class SingleFilePickerTest {

	@Test
	public void test() {

		try	{
		
			SingleFilePicker extractor = new SingleFilePicker();
			SingleFilePickerConfig config = new SingleFilePickerConfig("http://www.sukl.cz/file/76663_3_1/");
			
			extractor.configureDirectly(config);
			
			// prepare test environment, we use system tmp directory
			TestEnvironment env = TestEnvironment.create();
			// prepare input and output data units
			
			//FileDataUnit extractedFiles = env.createRdfOutput("extractedFiles", false);
			FileDataUnit extractedFiles = env.createFileOutput("extractedFiles");
	
			// here we can simply pre-fill input data unit with content from 
			// resource file
			
			try {
				// run the execution
				env.run(extractor);
				DirectoryHandler handler = extractedFiles.getRootDir();
				System.out.println("Resulting directory: " + handler.getRootedPath());
			}
		    catch(Exception e) {
			    e.printStackTrace();
		    } finally {
				// release resources
		    	env.release();
			}
			
		} catch (IOException e)	{
			e.printStackTrace();
		} catch (ConfigException e) {
			e.printStackTrace();
		}
		
	}

}
