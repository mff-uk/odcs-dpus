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
 * @author Škoda Petr
 */
public class CreateQueryCoiTest {

    private static final Logger LOG = LoggerFactory.getLogger(
            CreateQueryCoiTest.class);

    private static final String selectPostalAddress = "SELECT ?s WHERE {?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://schema.org/PostalAddress>}";

    @Test
    public void test() throws OperationFailedException, QueryEvaluationException {
        TestEnvironment env = new TestEnvironment();

        WritableRDFDataUnit inRdf = env.createRdfInput("ruian", false);
        SimpleRdfRead rdf = new SimpleRdfRead(inRdf, env.getContext());

        InputOutput.extractFromFile(new File(
                // PostalAddress-small
                "d:/Temp/02/PostalAddress-coi.ttl"),
                RDFFormat.TURTLE, inRdf);
        int failParse = 0;
        int totalCounter = 0;
        int failMap = 0;
        LOG.info("-------------------------------------------------");
        QueryCreator creator = new QueryCreator(true, rdf);
        try (ConnectionPair<TupleQueryResult> addresses = rdf
                .executeSelectQuery(selectPostalAddress)) {
            while (addresses.getObject().hasNext()) {
                BindingSet binding = addresses.getObject().next();
                // prepare query
                totalCounter++;
                try {
                    String query = creator.createQuery(binding.getValue("s"));
//                   LOG.info("Query:\n{}", query);
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
                    LOG.error("Failed to parse: '{}' \n\tex: {}",
                            binding.getValue("s"), ex.getMessage());
                    failParse++;
                }
            }
            LOG.info("-------------------------------------------------");
            LOG.info("Failed to parse: {}", failParse);
            LOG.info("Failed to map (get empty query): {}", failMap);
            LOG.info("Total: {}", totalCounter);
            LOG.info("-------------------------------------------------");
        }
        env.release();
    }
}
