package cz.cuni.mff.xrg.odcs.rdf.simple;

import cz.cuni.mff.xrg.odcs.commons.data.DataUnitException;

/**
 *
 * @author Škoda Petr
 */
public class OperationFailedException extends DataUnitException {

	public OperationFailedException(Throwable cause) {
		super(cause);
	}

	public OperationFailedException(String cause) {
		super(cause);
	}

	public OperationFailedException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
