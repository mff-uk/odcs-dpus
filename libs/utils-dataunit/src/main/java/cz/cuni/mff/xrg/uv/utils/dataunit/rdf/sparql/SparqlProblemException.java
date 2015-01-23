package cz.cuni.mff.xrg.uv.utils.dataunit.rdf.sparql;

import cz.cuni.mff.xrg.uv.utils.dataunit.FailedOperationException;

/**
 *
 * @author Škoda Petr
 */
public class SparqlProblemException extends FailedOperationException {

    public SparqlProblemException(String message) {
        super(message);
    }

    public SparqlProblemException(String message, Throwable cause) {
        super(message, cause);
    }

    public SparqlProblemException(Throwable cause) {
        super(cause);
    }

}
