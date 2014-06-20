package cz.cuni.mff.xrg.uv.postaladdress.to.ruain;

import cz.cuni.mff.xrg.odcs.dpu.test.TestEnvironment;
import cz.cuni.mff.xrg.odcs.rdf.WritableRDFDataUnit;
import cz.cuni.mff.xrg.odcs.rdf.simple.ConnectionPair;
import cz.cuni.mff.xrg.odcs.rdf.simple.OperationFailedException;
import cz.cuni.mff.xrg.odcs.rdf.simple.SimpleRdfRead;
import cz.cuni.mff.xrg.uv.postaladdress.to.ruain.query.EmptyQueryException;
import cz.cuni.mff.xrg.uv.postaladdress.to.ruain.query.QueryCreator;
import cz.cuni.mff.xrg.uv.postaladdress.to.ruain.query.QueryException;
import cz.cuni.mff.xrg.uv.test.boost.rdf.InputOutput;
import java.io.File;
import org.junit.Test;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Škoda Petr
 */
public class CreateQueryTest {

    private static final Logger LOG = LoggerFactory.getLogger(
            CreateQueryTest.class);

    private static final String selectPostalAddress = "SELECT ?s WHERE {?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://schema.org/PostalAddress>}";

    @Test
    public void test() throws OperationFailedException, QueryEvaluationException {
        //parse("d:/Temp/02/fit2-postalAddress/cenia.cz-irz.ttl");
//        parse("d:/Temp/02/fit2-postalAddress/coi.cz-kontroly.ttl");
//        parse("d:/Temp/02/fit2-postalAddress/eagri.cz-kontroly.ttl");
//        parse("d:/Temp/02/fit2-postalAddress/eagri.cz-provozovny.ttl");
//        parse("d:/Temp/02/fit2-postalAddress/mzp.cz-ippc.ttl"); // long regExp
        //parse("d:/Temp/02/fit2-postalAddress/mzp.cz-ippc-metadata.ttl");
//        parse("d:/Temp/02/fit2-postalAddress/seznam.gov.cz-ovm.ttl");
//        parse("d:/Temp/02/fit2-postalAddress/seznam.gov.cz-ovm-details.ttl");
//        parse("d:/Temp/02/fit2-postalAddress/seznam.gov.cz-ovm-list.ttl");
    }

    public void parse(String fileName) throws OperationFailedException, QueryEvaluationException {
        TestEnvironment env = new TestEnvironment();

        WritableRDFDataUnit inRdf = env.createRdfInput("ruian", false);
        SimpleRdfRead rdf = new SimpleRdfRead(inRdf, env.getContext());
        try {
            InputOutput.extractFromFile(new File(fileName),
                    RDFFormat.TURTLE, inRdf);
        } catch (Exception ex) {
            env.release();
            LOG.info("-------------------------------------------------");
            LOG.info("File: {}", fileName.substring(fileName.lastIndexOf("/")));
            LOG.info("Can't load file with input.");
            LOG.info("-------------------------------------------------");
            return;
        }
        int failParse = 0;
        int totalCounter = 0;
        int failMap = 0;
      
        LOG.info("===========================================================");
        QueryCreator creator = new QueryCreator(true, rdf);
        try (ConnectionPair<TupleQueryResult> addresses = rdf
                .executeSelectQuery(selectPostalAddress)) {
            
            while (addresses.getObject().hasNext()) {
                
                BindingSet binding = addresses.getObject().next();
                // prepare query
                totalCounter++;
                try {
                    String query = creator.createQuery(binding.getValue("s"));
                    //LOG.info("Query:\n{}", query);
                } catch (EmptyQueryException ex) {
                    failMap++;
                    // log the state
//                    LOG.warn("Failed to map: '{}' ex: {}",
//                            binding.getValue("s"),
//                            ex.getMessage());
//                    LOG.info("QueryCreator dump:\n{}", creator.getDump());
                } catch (QueryException ex) {
                    if (ex.getMessage().contains("?")) {
//                        // wrong source
//                        System.out.println(binding.getValue("s").stringValue());
//                        System.out.println("\t" + ex.getMessage());
                        continue;
                    }
//                    LOG.error("Failed to map: '{}' \n\tex: {}",
//                            binding.getValue("s"), ex.getMessage());
                    failParse++;
                }
            }
        }
        env.release();

        LOG.info("-------------------------------------------------");
        LOG.info("File: {}", fileName.substring(fileName.lastIndexOf("/")));
        LOG.info("Failed to parse: {}", failParse);
        LOG.info("Failed to map: {}", failMap);
        LOG.info("Total: {}", totalCounter);
        LOG.info("-------------------------------------------------");
    }

}
