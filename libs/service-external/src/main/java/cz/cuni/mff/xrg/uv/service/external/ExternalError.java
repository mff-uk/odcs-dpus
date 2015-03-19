package cz.cuni.mff.xrg.uv.service.external;

/**
 * Used to report possibly recoverable failures.
 *
 * @author Škoda Petr
 */
public class ExternalError extends Exception {

    public ExternalError(String message) {
        super(message);
    }

    public ExternalError(String message, Throwable cause) {
        super(message, cause);
    }

    public ExternalError(Throwable cause) {
        super(cause);
    }

}
