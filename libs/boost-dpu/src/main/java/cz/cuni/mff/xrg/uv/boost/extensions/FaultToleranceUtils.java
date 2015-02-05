package cz.cuni.mff.xrg.uv.boost.extensions;

import org.openrdf.model.ValueFactory;

import cz.cuni.mff.xrg.uv.boost.rdf.simple.SimpleRdf;
import eu.unifiedviews.dpu.DPUException;

/**
 * Contains code for common operation with {@link FaultTolerance}.
 *
 * @author Škoda Petr
 */
public class FaultToleranceUtils {

    private FaultToleranceUtils() {

    }

    /**
     *
     * @param faultTolerance
     * @param simpleRdf
     * @return {@link ValueFactory} obtained from given {@link SimpleRdf} instance.
     * @throws DPUException
     */
    public static ValueFactory getValuFactory(FaultTolerance faultTolerance,
            final SimpleRdf simpleRdf) throws DPUException {
        return faultTolerance.execute(
                new FaultTolerance.ActionReturn<ValueFactory>() {

                    @Override
                    public ValueFactory action() throws Exception {
                        return simpleRdf.getValueFactory();
                    }
                });
    }

}
