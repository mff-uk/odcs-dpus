package cz.opendata.linked.extractor.tabular.czso.vdb;

import java.io.File;
import java.util.LinkedHashMap;

import org.junit.Test;

import cz.cuni.mff.xrg.odcs.commons.configuration.ConfigException;
import cz.cuni.mff.xrg.odcs.commons.data.DataUnitException;
import cz.cuni.mff.xrg.odcs.dataunit.file.FileDataUnit;
import cz.cuni.mff.xrg.odcs.dataunit.file.options.OptionsAdd;
import cz.cuni.mff.xrg.odcs.dpu.test.TestEnvironment;
import cz.cuni.mff.xrg.odcs.rdf.enums.RDFFormatType;
import cz.cuni.mff.xrg.odcs.rdf.exceptions.RDFException;
import cz.cuni.mff.xrg.odcs.rdf.interfaces.RDFDataUnit;
import cz.opendata.linked.extractor.tabular.czso.vdb.CZSOVDBExtractor;
import cz.opendata.linked.extractor.tabular.czso.vdb.CZSOVDBExtractorConfig;

public class CZSOVDBExtractorTest {

	@Test
	public void testDEM0010PU_OK_2012() {

		try	{
		
			CZSOVDBExtractor extractor = new CZSOVDBExtractor();
			
			LinkedHashMap<Integer, String> columnPropertyMap = new LinkedHashMap<Integer, String>();
			columnPropertyMap.put(new Integer(1), "http://linked.opendata.cz/ontology/czso/okres");
			columnPropertyMap.put(new Integer(2), "http://linked.opendata.cz/ontology/czso/snatky");
			columnPropertyMap.put(new Integer(3), "http://linked.opendata.cz/ontology/czso/rozvody");
			columnPropertyMap.put(new Integer(4), "http://linked.opendata.cz/ontology/czso/zive-narozeni");
			columnPropertyMap.put(new Integer(5), "http://linked.opendata.cz/ontology/czso/potraty-celkem");
			columnPropertyMap.put(new Integer(6), "http://linked.opendata.cz/ontology/czso/potraty-upt");
			columnPropertyMap.put(new Integer(7), "http://linked.opendata.cz/ontology/czso/zemreli-celkem");
			columnPropertyMap.put(new Integer(8), "http://linked.opendata.cz/ontology/czso/zemreli-do-1-roku");
			columnPropertyMap.put(new Integer(9), "http://linked.opendata.cz/ontology/czso/zemreli-do-28-dnu");
			columnPropertyMap.put(new Integer(10), "http://linked.opendata.cz/ontology/czso/prirozeny-prirustek");
			columnPropertyMap.put(new Integer(11), "http://linked.opendata.cz/ontology/czso/pristehovali");
			columnPropertyMap.put(new Integer(12), "http://linked.opendata.cz/ontology/czso/vystehovali");
			columnPropertyMap.put(new Integer(13), "http://linked.opendata.cz/ontology/czso/prirustek-stehovanim");
			columnPropertyMap.put(new Integer(14), "http://linked.opendata.cz/ontology/czso/celkovy-prirustek");
			
			LinkedHashMap<Coordinates, String> dimensionValueMap = new LinkedHashMap<Coordinates, String>();
			Coordinates coordinates1 = new Coordinates(14, 3);
			dimensionValueMap.put(coordinates1, "http://linked.opendata.cz/ontology/czso/obdobi");
			
			
			CZSOVDBExtractorConfig config = new CZSOVDBExtractorConfig(columnPropertyMap, dimensionValueMap, null, -1, 7);
			
			extractor.configureDirectly(config);
			
			// prepare test environment, we use system tmp directory
			TestEnvironment env = TestEnvironment.create();
						
			FileDataUnit tableFile = env.createFileInput("table", new File("c:\\Users\\martin\\Documents\\WORK\\PROJECTS\\#FP7\\COMSODE\\_SUBPROJECTS\\CSU\\data\\40-demografie"));
			tableFile.getRootDir().addExistingFile(new File("c:\\Users\\martin\\Documents\\WORK\\PROJECTS\\#FP7\\COMSODE\\_SUBPROJECTS\\CSU\\data\\40-demografie\\tabulka_DEM0010PU_OK_2012.xls"), new OptionsAdd(false));
			tableFile.getRootDir().addExistingFile(new File("c:\\Users\\martin\\Documents\\WORK\\PROJECTS\\#FP7\\COMSODE\\_SUBPROJECTS\\CSU\\data\\40-demografie\\tabulka_DEM0010PU_OK_2011.xls"), new OptionsAdd(false));
			tableFile.getRootDir().addExistingFile(new File("c:\\Users\\martin\\Documents\\WORK\\PROJECTS\\#FP7\\COMSODE\\_SUBPROJECTS\\CSU\\data\\40-demografie\\tabulka_DEM0010PU_OK_2010.xls"), new OptionsAdd(false));
			tableFile.getRootDir().addExistingFile(new File("c:\\Users\\martin\\Documents\\WORK\\PROJECTS\\#FP7\\COMSODE\\_SUBPROJECTS\\CSU\\data\\40-demografie\\tabulka_DEM0010PU_OK_2009.xls"), new OptionsAdd(false));
			tableFile.getRootDir().addExistingFile(new File("c:\\Users\\martin\\Documents\\WORK\\PROJECTS\\#FP7\\COMSODE\\_SUBPROJECTS\\CSU\\data\\40-demografie\\tabulka_DEM0010PU_OK_2008.xls"), new OptionsAdd(false));
			tableFile.getRootDir().addExistingFile(new File("c:\\Users\\martin\\Documents\\WORK\\PROJECTS\\#FP7\\COMSODE\\_SUBPROJECTS\\CSU\\data\\40-demografie\\tabulka_DEM0010PU_OK_2007.xls"), new OptionsAdd(false));
			tableFile.getRootDir().addExistingFile(new File("c:\\Users\\martin\\Documents\\WORK\\PROJECTS\\#FP7\\COMSODE\\_SUBPROJECTS\\CSU\\data\\40-demografie\\tabulka_DEM0010PU_OK_2006.xls"), new OptionsAdd(false));
			RDFDataUnit triplifiedTable = env.createRdfOutput("triplifiedTable", false);
						
			try {
				// run the execution
				env.run(extractor);
			}
		    catch(Exception e) {
			    e.printStackTrace();
		    }
			
			triplifiedTable.loadToFile("C:\\temp\\DEM0010PU_OK_2012.ttl", RDFFormatType.TTL);
			
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
