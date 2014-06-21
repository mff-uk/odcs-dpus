package cz.cuni.mff.xrg.uv.external;

/**
 *
 * @author Škoda Petr
 */
public class ExternalFailure extends Exception {

    public ExternalFailure(String message) {
        super(message);
    }

    public ExternalFailure(String message, Throwable cause) {
        super(message, cause);
    }
    
}
