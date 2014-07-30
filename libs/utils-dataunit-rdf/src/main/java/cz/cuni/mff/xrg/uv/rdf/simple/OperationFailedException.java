package cz.cuni.mff.xrg.uv.rdf.simple;

import eu.unifiedviews.dataunit.DataUnitException;

/**
 * General exception used to report operation failure in {@link SimpleRdfRead}
 * and {@link SimpleRdfWrite}.
 *
 * @author Škoda Petr
 */
public class OperationFailedException extends DataUnitException {

	OperationFailedException(String message, Throwable cause) {
		super(message, cause);
	}

}
