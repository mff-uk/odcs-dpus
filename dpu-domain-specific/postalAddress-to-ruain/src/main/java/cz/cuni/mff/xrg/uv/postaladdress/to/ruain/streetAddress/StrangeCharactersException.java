package cz.cuni.mff.xrg.uv.postaladdress.to.ruain.streetAddress;

/**
 * Report that address contains strange characters ['?'];
 * 
 * @author Škoda Petr
 */
public class StrangeCharactersException extends WrongAddressFormatException {

    public StrangeCharactersException(String message) {
        super(message);
    }
    
}
