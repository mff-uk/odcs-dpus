package cz.opendata.linked.geocoder.krovak;

import java.io.File;

import org.junit.Test;
import org.openrdf.rio.RDFFormat;

import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import cz.cuni.mff.xrg.uv.test.boost.rdf.InputOutput;

public class ScrapeTest {

//    @Test
//    public void constructAllTest() throws Exception {
//        // prepare dpu instance and configure it
//        Extractor extractor = new Extractor();
//        ExtractorConfig config = new ExtractorConfig();
//        config.setNumofrecords(100);
//
//        extractor.configureDirectly(config);
//
//        // prepare test environment, we use system tmp directory
//        TestEnvironment env = new TestEnvironment();
//        // prepare input and output data units
//
//        WritableRDFDataUnit sPoints = env.createRdfInput("points", false);
//        InputOutput.extractFromFile(new File("C:\\temp\\addresses.ttl"), RDFFormat.TURTLE, sPoints);
//        WritableRDFDataUnit geoCoord = env.createRdfOutput("Geocoordinates", false);
//
//        // here we can simply pre-fill input data unit with content from 
//        // resource file
//        try {
//            // run the execution
//            env.run(extractor);
//
//            InputOutput.loadToFile(geoCoord, new File("C:\\temp\\geo.ttl"), RDFFormat.TURTLE);
//
//            // verify result
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            // release resources
//            env.release();
//        }
//    }
}
