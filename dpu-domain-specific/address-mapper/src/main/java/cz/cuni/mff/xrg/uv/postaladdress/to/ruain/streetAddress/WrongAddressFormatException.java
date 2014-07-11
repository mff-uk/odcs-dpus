package cz.cuni.mff.xrg.uv.postaladdress.to.ruain.streetAddress;

/**
 *
 * @author Škoda Petr
 */
public class WrongAddressFormatException extends Exception {

    public WrongAddressFormatException(String message) {
        super(message);
    }

    public WrongAddressFormatException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
