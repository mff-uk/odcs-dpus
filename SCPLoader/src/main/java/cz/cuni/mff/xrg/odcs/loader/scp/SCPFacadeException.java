package cz.cuni.mff.xrg.odcs.loader.scp;

/**
 * Exception used by {@link SCPFacade}.
 * 
 * @author Škoda Petr
 */
public class SCPFacadeException extends Exception {

	public SCPFacadeException(String message) {
		super(message);
	}

	public SCPFacadeException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
