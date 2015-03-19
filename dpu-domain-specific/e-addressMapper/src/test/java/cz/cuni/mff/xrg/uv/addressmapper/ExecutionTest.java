package cz.cuni.mff.xrg.uv.addressmapper;

import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import java.io.File;
import org.junit.Test;
import org.openrdf.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.odcs.dpu.test.TestEnvironment;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.helpers.dataunit.rdf.RdfDataUnitUtils;
import eu.unifiedviews.helpers.dpu.test.config.ConfigurationBuilder;
import eu.unifiedviews.helpers.dpu.test.rdf.InputOutputUtils;
import eu.unifiedviews.helpers.dpu.test.resources.ResourceUtils;

/**
 *
 * @author Škoda Petr
 */
public class ExecutionTest {

    private static final Logger LOG = LoggerFactory.getLogger(ExecutionTest.class);

    @Test
    public void ulice_Delnicka() throws Exception {
        final TestEnvironment testEnv = new TestEnvironment();

        // Prepare input and output.
        final WritableRDFDataUnit input = testEnv.createRdfInput("input", false);
        final WritableRDFDataUnit output = testEnv.createRdfOutput("output", false);

        final RDFDataUnit.Entry entry = RdfDataUnitUtils.addGraph(input, "ulice");
        InputOutputUtils.extractFromFile(ResourceUtils.getFile("ulice-Delnicka.ttl"), RDFFormat.TURTLE,
                input, entry);

        // Prepare configuration - default is enough.
        final AddressMapperConfig_V1 config = new AddressMapperConfig_V1();

        // Create DPU and set configuration.
        final AddressMapper dpu = new AddressMapper();
        dpu.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());

        try {
            testEnv.run(dpu);
            // Dump output to file ..

            InputOutputUtils.loadToFile(output, new File("d:/Temp/03/out.ttl"), RDFFormat.TURTLE);
            
        } finally {
            testEnv.release();
        }
    }

}