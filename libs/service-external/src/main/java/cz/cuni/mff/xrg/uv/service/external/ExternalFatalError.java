package cz.cuni.mff.xrg.uv.service.external;

import eu.unifiedviews.dpu.DPUException;

/**
 * Used to report failures that user should not try to recover from.
 *
 * @author Škoda Petr
 */
public class ExternalFatalError extends DPUException {

    public ExternalFatalError(String message) {
        super(message);
    }

    public ExternalFatalError(String message, Throwable cause) {
        super(message, cause);
    }

    public ExternalFatalError(Throwable cause) {
        super(cause);
    }

}
