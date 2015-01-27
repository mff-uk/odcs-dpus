package cz.cuni.mff.xrg.uv.transformer.tabular;

import cz.cuni.mff.xrg.uv.transformer.tabular.parser.ParseFailed;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.OperationFailedException;
import cz.cuni.mff.xrg.uv.transformer.tabular.column.ColumnInfo_V1;
import cz.cuni.mff.xrg.uv.transformer.tabular.column.ColumnType;
import cz.cuni.mff.xrg.uv.transformer.tabular.mapper.TableToRdf;
import cz.cuni.mff.xrg.uv.transformer.tabular.mapper.TableToRdfConfig;
import cz.cuni.mff.xrg.uv.transformer.tabular.csv.ParserCsv;
import cz.cuni.mff.xrg.uv.transformer.tabular.csv.ParserCsvConfig;
import eu.unifiedviews.dpu.DPUContext;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.uv.boost.rdf.simple.WritableSimpleRdf;
import cz.cuni.mff.xrg.uv.boost.serialization.rdf.SimpleRdfException;
import cz.cuni.mff.xrg.uv.test.boost.resources.ResourceUtils;
import cz.cuni.mff.xrg.uv.transformer.tabular.xls.ParserXls;
import cz.cuni.mff.xrg.uv.transformer.tabular.xls.ParserXlsConfig;

/**
 *
 * @author Škoda Petr
 */
public class CsvTest {

    private static final Logger LOG = LoggerFactory.getLogger(CsvTest.class);

    private static class WritableOut extends WritableSimpleRdf {

        @Override
        public WritableSimpleRdf add(Resource s, URI p, Value o) throws SimpleRdfException {
            LOG.debug("{} {} {}", s, p, o);
            return this;
        }
    
    }

    private static DPUContext context;

    @BeforeClass
    public static void init() {
        TabularOntology.init(new ValueFactoryImpl());
        // prepare context
        context = Mockito.mock(DPUContext.class);
        Mockito.when(context.canceled()).thenReturn(false);
    }

//    @Test
    public void csvw_csv2rdf_example_1() throws OperationFailedException, ParseFailed, SimpleRdfException {
        
        final File csvFile = ResourceUtils.getFile("csvw_csv2rdf_example_1.csv");
        LOG.info(">>>>> {} ", csvFile.toString());

        // prepare tabular parser config
        HashMap<String, ColumnInfo_V1> columnInfo = new HashMap<>();
        TableToRdfConfig tabularConfig = new TableToRdfConfig(null, 
                "http://localhost/", columnInfo, true, null, false,
                Collections.EMPTY_LIST, false, true, true, false, false);
        // prepare rdf data unit
        WritableSimpleRdf outRdf = new WritableOut();

        // prepare tabular parser
        TableToRdf tabular = new TableToRdf(tabularConfig, outRdf, new ValueFactoryImpl());

        // prepare csv configuration
        ParserCsvConfig csvConfig = new ParserCsvConfig(null, null, null, 0, null, true, true);

        // go go go
        ParserCsv parser = new ParserCsv(csvConfig, tabular, context);
        parser.parse(csvFile);
    }

//    @Test
    public void opendata_cssz() throws OperationFailedException, ParseFailed, SimpleRdfException {

        final File csvFile = ResourceUtils.getFile("opendata-cssz.csv");
        LOG.info(">>>>> {} ", csvFile.toString());

        // prepare tabular parser config
        HashMap<String, ColumnInfo_V1> columnInfo = new HashMap<>();
        columnInfo.put("Fakt", new ColumnInfo_V1(null, ColumnType.String));

        TableToRdfConfig tabularConfig = new TableToRdfConfig(null, 
                "http://localhost/", columnInfo, false, null, false,
                Collections.EMPTY_LIST, false, false, true, false, true);
        // prepare rdf data unit
        WritableSimpleRdf outRdf = new WritableOut();

        // prepare tabular parser
        TableToRdf tabular = new TableToRdf(tabularConfig, outRdf, new ValueFactoryImpl());
        tabular.setTableSubject((new ValueFactoryImpl()).createURI("http://opendata-cssz.csv"));

        // prepare csv configuration
        ParserCsvConfig csvConfig = new ParserCsvConfig(null, null, null,
                1, 10, true, true);

        // go go go
        ParserCsv parser = new ParserCsv(csvConfig, tabular, context);
        parser.parse(csvFile);
    }

//    @Test
    public void opendata_cssz_no_header() throws OperationFailedException, ParseFailed, SimpleRdfException {

        final File csvFile = ResourceUtils.getFile("opendata-cssz_no-header.csv");
        LOG.info(">>>>> {} ", csvFile.toString());

        // prepare tabular parser config
        HashMap<String, ColumnInfo_V1> columnInfo = new HashMap<>();

        TableToRdfConfig tabularConfig = new TableToRdfConfig(null, 
                "http://localhost/", columnInfo, true, null, false,
                Collections.EMPTY_LIST, false, false, true, false, false);
        // prepare rdf data unit
        WritableSimpleRdf outRdf = new WritableOut();

        // prepare tabular parser
        TableToRdf tabular = new TableToRdf(tabularConfig, outRdf,
                new ValueFactoryImpl());

        // prepare csv configuration
        ParserCsvConfig csvConfig = new ParserCsvConfig(null, null, null,
                1, 10, false, true);

        // go go go
        ParserCsv parser = new ParserCsv(csvConfig, tabular, context);
        parser.parse(csvFile);
    }

    //@Test
    public void DETIND2_140801() throws OperationFailedException, ParseFailed, SimpleRdfException {

        final File csvFile = ResourceUtils.getFile("DETIND2_140801.txt");
        LOG.info(">>>>> {} ", csvFile.toString());

        // prepare tabular parser config
        HashMap<String, ColumnInfo_V1> columnInfo = new HashMap<>();

        TableToRdfConfig tabularConfig = new TableToRdfConfig("col1",
                "http://localhost/", columnInfo, true, "http://localhost/Row",
                false, Collections.EMPTY_LIST, false, false, true, false, false);
        // prepare rdf data unit
        WritableSimpleRdf outRdf = new WritableOut();

        // prepare tabular parser
        TableToRdf tabular = new TableToRdf(tabularConfig, outRdf,
                new ValueFactoryImpl());

        // prepare csv configuration
        ParserCsvConfig csvConfig = new ParserCsvConfig(null, "|", "Cp1250",
                1, 10, false, true);

        // go go go
        ParserCsv parser = new ParserCsv(csvConfig, tabular, context);
        parser.parse(csvFile);
    }

//    @Test
    public void UZJ() throws OperationFailedException, ParseFailed, SimpleRdfException {
        // Test support for UTF-* with BOM
        //
        
        final File csvFile = ResourceUtils.getFile("UZJ.csv");
        LOG.info(">>>>> {} ", csvFile.toString());

        // prepare tabular parser config
        HashMap<String, ColumnInfo_V1> columnInfo = new HashMap<>();
        columnInfo.put("KODCIS", new ColumnInfo_V1("http://localhost/KODCIS") );
        TableToRdfConfig tabularConfig = new TableToRdfConfig("KODCIS",
                "http://localhost/", columnInfo, false, "http://localhost/Row",
                false, Collections.EMPTY_LIST, false, false, false, false, false);

        // prepare rdf data unit
        WritableSimpleRdf outRdf = new WritableOut();

        // prepare tabular parser
        TableToRdf tabular = new TableToRdf(tabularConfig, outRdf,
                new ValueFactoryImpl());

        // prepare csv configuration
        ParserCsvConfig csvConfig = new ParserCsvConfig(null, ";", "UTF-8",
                0, 10, true, true);

        // go go go
        ParserCsv parser = new ParserCsv(csvConfig, tabular, context);
        parser.parse(csvFile);
    }

//    @Test
    public void XSLS() throws OperationFailedException, ParseFailed, SimpleRdfException {
        // Test support for UTF-* with BOM
        //

        final File csvFile = new File("D:/Temp/2015_01-Seznam-prijemcu-(List-of-Beneficiaries)-01_2015.xlsx");
        LOG.info(">>>>> {} ", csvFile.toString());

        // prepare tabular parser config
        HashMap<String, ColumnInfo_V1> columnInfo = new HashMap<>();
        TableToRdfConfig tabularConfig = new TableToRdfConfig("col1",
                "http://localhost/", columnInfo, true, "http://localhost/Row",
                false, Collections.EMPTY_LIST, false, false, false, false, false);

        // prepare rdf data unit
        WritableSimpleRdf outRdf = new WritableOut();

        // prepare tabular parser
        TableToRdf tabular = new TableToRdf(tabularConfig, outRdf,
                new ValueFactoryImpl());

        // prepare csv configuration
        ParserXlsConfig xlsConfig = new ParserXlsConfig(null, 10, false, null, 1, false);

        // go go go
        ParserXls parser = new ParserXls(xlsConfig, tabular, context);
        parser.parse(csvFile);
    }

}
