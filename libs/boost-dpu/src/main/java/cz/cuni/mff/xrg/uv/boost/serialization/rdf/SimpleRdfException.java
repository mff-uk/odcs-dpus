package cz.cuni.mff.xrg.uv.boost.serialization.rdf;

import eu.unifiedviews.dpu.DPUException;

/**
 *
 * @author Škoda Petr
 */
public class SimpleRdfException extends DPUException {

    public SimpleRdfException(String message) {
        super(message);
    }

    public SimpleRdfException(String message, Throwable cause) {
        super(message, cause);
    }

    public SimpleRdfException(Throwable cause) {
        super(cause);
    }

}
