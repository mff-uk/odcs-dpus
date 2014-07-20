package cz.cuni.mff.xrg.uv.existencechecker;

import cz.cuni.mff.xrg.odcs.commons.configuration.ConfigException;
import cz.cuni.mff.xrg.odcs.dpu.test.TestEnvironment;
import cz.cuni.mff.xrg.odcs.rdf.WritableRDFDataUnit;
import cz.cuni.mff.xrg.odcs.rdf.exceptions.RDFException;
import cz.cuni.mff.xrg.uv.external.ExternalFailure;
import cz.cuni.mff.xrg.uv.rdf.simple.OperationFailedException;
import cz.cuni.mff.xrg.uv.test.boost.rdf.InputOutput;
import java.io.File;
import org.junit.Test;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Škoda Petr
 */
public class ExecutionTest {

    private static final Logger LOG = LoggerFactory.getLogger(
            ExecutionTest.class);

    //@Test
    public void execute() throws OperationFailedException, QueryEvaluationException, ExternalFailure, RDFException, ConfigException {
        TestEnvironment env = new TestEnvironment();

        // create data units
        WritableRDFDataUnit input = env.createRdfInput("input", false);
        WritableRDFDataUnit output = env.createRdfOutput("output", false);
        // load data
        try {
            // aditional data
            InputOutput.extractFromFile(new File("d:/Temp/01/input-test.ttl"),
                    RDFFormat.TURTLE, input);
        } catch (Exception ex) {
            env.release();
            LOG.error("Faield to load input data.", ex);
            return;
        }
        // execute
        Main main = new Main();
        try {
            env.run(main);
            // store results
            InputOutput.loadToFile(output,
                    new File("d:/Temp/01/out-existenceChecker.ttl"),
                    RDFFormat.TURTLE);
        } catch (Exception ex) {
            LOG.error("DPU failed", ex);
        } finally {
            env.release();
        }

    }

}
