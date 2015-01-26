package cz.cuni.mff.xrg.uv.boost.serialization;

import cz.cuni.mff.xrg.uv.utils.dataunit.FailedOperationException;

/**
 * Base exception used by serialization module.
 * 
 * @author Škoda Petr
 */
public class SerializationFailure extends FailedOperationException {

    public SerializationFailure(String message) {
        super(message);
    }

    public SerializationFailure(String message, Throwable cause) {
        super(message, cause);
    }

    public SerializationFailure(Throwable cause) {
        super(cause);
    }

}
