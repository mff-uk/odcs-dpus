package cz.opendata.linked.extractor.tabular;

import java.io.File;
import java.util.HashMap;

import org.junit.Test;

import cz.cuni.mff.xrg.odcs.commons.configuration.ConfigException;
import cz.cuni.mff.xrg.odcs.commons.data.DataUnitException;
import cz.cuni.mff.xrg.odcs.dataunit.file.FileDataUnit;
import cz.cuni.mff.xrg.odcs.dataunit.file.options.OptionsAdd;
import cz.cuni.mff.xrg.odcs.dpu.test.TestEnvironment;
import cz.cuni.mff.xrg.odcs.rdf.enums.RDFFormatType;
import cz.cuni.mff.xrg.odcs.rdf.exceptions.RDFException;
import cz.cuni.mff.xrg.odcs.rdf.interfaces.RDFDataUnit;
import cz.opendata.linked.extractor.tabular.Extractor;
import cz.opendata.linked.extractor.tabular.ExtractorConfig;

public class ExtractorTest {

	@Test
	public void testKLK() {

		try	{
		
			Extractor extractor = new Extractor();
			
			
			HashMap<String, String> columnPropertyMap = new HashMap<String, String>();
			columnPropertyMap.put("ATC_WHO", "http://linked.opendata.cz/ontology/sukl/hasATCCode");
			ExtractorConfig config = new ExtractorConfig(columnPropertyMap, "http://linked.opendata.cz/resource/sukl/medicinal-product-packaging/", "KOD", "CP852");
			
			extractor.configureDirectly(config);
			
			// prepare test environment, we use system tmp directory
			TestEnvironment env = TestEnvironment.create();
						
			FileDataUnit tableFile = env.createFileInput("table", new File("c:\\Users\\martin\\Documents\\WORK\\PROJECTS\\#NASE\\EHEALTH\\DATASOURCES\\SUKL\\KLK"));
			tableFile.getRootDir().addExistingFile(new File("c:\\Users\\martin\\Documents\\WORK\\PROJECTS\\#NASE\\EHEALTH\\DATASOURCES\\SUKL\\KLK\\KLK.DBF"), new OptionsAdd(false));
			RDFDataUnit triplifiedTable = env.createRdfOutput("triplifiedTable", false);
						
			try {
				// run the execution
				env.run(extractor);
			}
		    catch(Exception e) {
			    e.printStackTrace();
		    }
			
			triplifiedTable.loadToFile("C:\\temp\\klk.ttl", RDFFormatType.TTL);
			
			env.release();
			
		} catch (RDFException e)	{
			e.printStackTrace();
		} catch (ConfigException e) {
			e.printStackTrace();
		} catch (DataUnitException e)	{
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void testLEKFORMY() {

		try	{
		
			Extractor extractor = new Extractor();
			
			
			HashMap<String, String> columnPropertyMap = new HashMap<String, String>();
			columnPropertyMap.put("ATC_WHO", "http://linked.opendata.cz/ontology/sukl/hasATCCode");
			ExtractorConfig config = new ExtractorConfig(columnPropertyMap, "http://linked.opendata.cz/resource/sukl/df/", "LEK_FORMA", "CP852");
			
			extractor.configureDirectly(config);
			
			// prepare test environment, we use system tmp directory
			TestEnvironment env = TestEnvironment.create();
						
			FileDataUnit tableFile = env.createFileInput("table", new File("c:\\Users\\martin\\Documents\\WORK\\PROJECTS\\#NASE\\EHEALTH\\DATASOURCES\\SUKL"));
			tableFile.getRootDir().addExistingFile(new File("c:\\Users\\martin\\Documents\\WORK\\PROJECTS\\#NASE\\EHEALTH\\DATASOURCES\\SUKL\\LEKFORMY.DBF"), new OptionsAdd(false));
			RDFDataUnit triplifiedTable = env.createRdfOutput("triplifiedTable", false);
						
			try {
				// run the execution
				env.run(extractor);
			}
		    catch(Exception e) {
			    e.printStackTrace();
		    }
			
			triplifiedTable.loadToFile("C:\\temp\\lekformy.ttl", RDFFormatType.TTL);
			
			env.release();
			
		} catch (RDFException e)	{
			e.printStackTrace();
		} catch (ConfigException e) {
			e.printStackTrace();
		} catch (DataUnitException e)	{
			e.printStackTrace();
		}
		
	}

}
