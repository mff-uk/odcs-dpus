package cz.opendata.linked.transformer.multiple_files_picker;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import cz.cuni.mff.xrg.odcs.commons.configuration.ConfigException;
import cz.cuni.mff.xrg.odcs.commons.data.DataUnitException;
import cz.cuni.mff.xrg.odcs.dataunit.file.FileDataUnit;
import cz.cuni.mff.xrg.odcs.dataunit.file.handlers.DirectoryHandler;
import cz.cuni.mff.xrg.odcs.dataunit.file.options.OptionsAdd;
import cz.cuni.mff.xrg.odcs.dpu.test.TestEnvironment;
import cz.opendata.linked.transformer.multiple_files_picker.MultipleFilesPicker;
import cz.opendata.linked.transformer.multiple_files_picker.MultipleFilesPickerConfig;

public class MultipleFilesPickerTest {

	@Test
	public void test() {

		try	{
		
			MultipleFilesPicker extractor = new MultipleFilesPicker();
			MultipleFilesPickerConfig config = new MultipleFilesPickerConfig("tabulka_DEM0010PU_OK_[0-9]{4}\\.xls");
			
			extractor.configureDirectly(config);
			
			// prepare test environment, we use system tmp directory
			TestEnvironment env = new TestEnvironment();
			
			// prepare input and output data units
			
			FileDataUnit inputFiles = env.createFileInput("inputFiles", new File("c:\\temp\\odcstest\\inputFiles"));
			inputFiles.getRootDir().addExistingFile(new File("c:\\Users\\martin\\Documents\\WORK\\PROJECTS\\#FP7\\COMSODE\\_SUBPROJECTS\\CSU\\data\\40-demografie\\tabulka_DEM0010PU_OK_2006.xls"), new OptionsAdd(false, true));
			inputFiles.getRootDir().addExistingFile(new File("c:\\Users\\martin\\Documents\\WORK\\PROJECTS\\#FP7\\COMSODE\\_SUBPROJECTS\\CSU\\data\\40-demografie\\tabulka_DEM0010PU_OK_2007.xls"), new OptionsAdd(false, true));
			inputFiles.getRootDir().addExistingFile(new File("c:\\Users\\martin\\Documents\\WORK\\PROJECTS\\#FP7\\COMSODE\\_SUBPROJECTS\\CSU\\data\\40-demografie\\tabulka_DEM0010PU_OK_2008.xls"), new OptionsAdd(false, true));
			inputFiles.getRootDir().addExistingFile(new File("c:\\Users\\martin\\Documents\\WORK\\PROJECTS\\#FP7\\COMSODE\\_SUBPROJECTS\\CSU\\data\\40-demografie\\tabulka_DEM0010PU_OK_2009.xls"), new OptionsAdd(false, true));
			inputFiles.getRootDir().addExistingFile(new File("c:\\Users\\martin\\Documents\\WORK\\PROJECTS\\#FP7\\COMSODE\\_SUBPROJECTS\\CSU\\data\\40-demografie\\tabulka_DEM0010PU_OK_2010.xls"), new OptionsAdd(false, true));
			inputFiles.getRootDir().addExistingFile(new File("c:\\Users\\martin\\Documents\\WORK\\PROJECTS\\#FP7\\COMSODE\\_SUBPROJECTS\\CSU\\data\\40-demografie\\tabulka_DEM0010PU_OK_2011.xls"), new OptionsAdd(false, true));
			inputFiles.getRootDir().addExistingFile(new File("c:\\Users\\martin\\Documents\\WORK\\PROJECTS\\#FP7\\COMSODE\\_SUBPROJECTS\\CSU\\data\\40-demografie\\tabulka_DEM0010PU_OK_2012.xls"), new OptionsAdd(false, true));
			inputFiles.getRootDir().addExistingFile(new File("c:\\Users\\martin\\Documents\\WORK\\PROJECTS\\#FP7\\COMSODE\\_SUBPROJECTS\\CSU\\data\\40-demografie\\tabulka_DEM0010PU_KR_2006.xls"), new OptionsAdd(false, true));
			inputFiles.getRootDir().addExistingFile(new File("c:\\Users\\martin\\Documents\\WORK\\PROJECTS\\#FP7\\COMSODE\\_SUBPROJECTS\\CSU\\data\\40-demografie\\tabulka_DEM0010PU_KR_2007.xls"), new OptionsAdd(false, true));
			inputFiles.getRootDir().addExistingFile(new File("c:\\Users\\martin\\Documents\\WORK\\PROJECTS\\#FP7\\COMSODE\\_SUBPROJECTS\\CSU\\data\\40-demografie\\tabulka_DEM0010PU_KR_2008.xls"), new OptionsAdd(false, true));
			inputFiles.getRootDir().addExistingFile(new File("c:\\Users\\martin\\Documents\\WORK\\PROJECTS\\#FP7\\COMSODE\\_SUBPROJECTS\\CSU\\data\\40-demografie\\tabulka_DEM0010PU_KR_2009.xls"), new OptionsAdd(false, true));
			inputFiles.getRootDir().addExistingFile(new File("c:\\Users\\martin\\Documents\\WORK\\PROJECTS\\#FP7\\COMSODE\\_SUBPROJECTS\\CSU\\data\\40-demografie\\tabulka_DEM0010PU_KR_2010.xls"), new OptionsAdd(false, true));
			inputFiles.getRootDir().addExistingFile(new File("c:\\Users\\martin\\Documents\\WORK\\PROJECTS\\#FP7\\COMSODE\\_SUBPROJECTS\\CSU\\data\\40-demografie\\tabulka_DEM0010PU_KR_2011.xls"), new OptionsAdd(false, true));
			inputFiles.getRootDir().addExistingFile(new File("c:\\Users\\martin\\Documents\\WORK\\PROJECTS\\#FP7\\COMSODE\\_SUBPROJECTS\\CSU\\data\\40-demografie\\tabulka_DEM0010PU_KR_2012.xls"), new OptionsAdd(false, true));
			
			FileDataUnit pickedFiles = env.createFileOutput("pickedFiles");
	
			// here we can simply pre-fill input data unit with content from 
			// resource file
			
			try {
				// run the execution
				env.run(extractor);
				DirectoryHandler handler = pickedFiles.getRootDir();
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
		} catch (DataUnitException e) {
			e.printStackTrace();
		}
		
	}

}
