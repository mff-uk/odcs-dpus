package cz.cuni.mff.xrg.uv.boost.extensions;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.uv.test.boost.dpu.DPUContextMockBase;
import eu.unifiedviews.dpu.DPUException;

/**
 *
 * @author Škoda Petr
 */
public class FaultToleranceTest {

    private static final Logger LOG = LoggerFactory.getLogger(FaultToleranceTest.class);

    public static class Counter {
        
        int value = 0;
        
    }

    @Test
    public void testDirect() throws DPUException {
        FaultTolerance ft = new FaultTolerance();

        FaultTolerance.Configuration_V1 config = new FaultTolerance.Configuration_V1();
        config.setEnabled(true);
        config.getExceptionNames().add("eu.unifiedviews.dpu.DPUException");
        config.setMaxRetryCount(-1);

        ft.configure(config, new DPUContextMockBase());

        final Counter counter = new Counter();

        ft.execute(new FaultTolerance.Action() {

            @Override
            public void action() throws Exception {
                LOG.debug("Executing {} ", counter.value);
                // Fail for two times.
                ++counter.value;
                if (counter.value < 2) {
                    throw new DPUException("");
                }
                LOG.debug("Done ..");
            }
        });

    }

    @Test
    public void testExceptionCause() throws DPUException {
        FaultTolerance ft = new FaultTolerance();

        FaultTolerance.Configuration_V1 config = new FaultTolerance.Configuration_V1();
        config.setEnabled(true);
        config.getExceptionNames().add("eu.unifiedviews.dpu.DPUException");
        config.setMaxRetryCount(-1);

        ft.configure(config, new DPUContextMockBase());

        final Counter counter = new Counter();

        ft.execute(new FaultTolerance.Action() {

            @Override
            public void action() throws Exception {
                LOG.debug("Executing {} ", counter.value);
                // Fail for two times.
                ++counter.value;
                if (counter.value < 2) {
                    throw new RuntimeException(new DPUException(""));
                }
                LOG.debug("Done ..");
            }
        });

    }

}
