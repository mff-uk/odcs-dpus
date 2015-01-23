package cz.cuni.mff.xrg.uv.utils.dataunit;

import eu.unifiedviews.dpu.DPUException;

/**
 * Base exception used by helpers.
 * 
 * @author Škoda Petr
 */
public class FailedOperationException extends DPUException {

    public FailedOperationException(String message) {
        super(message);
    }

    public FailedOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public FailedOperationException(Throwable cause) {
        super(cause);
    }

}
