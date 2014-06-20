package cz.cuni.mff.xrg.uv.postaladdress.to.ruain.mapping;

/**
 *
 * @author Škoda Petr
 */
public class MappingException extends Exception {

    public MappingException(String message) {
        super(message);
    }

    public MappingException(String message, Throwable cause) {
        super(message, cause);
    }

    public MappingException(Throwable cause) {
        super(cause);
    }

}
