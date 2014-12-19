package cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.sparql;

/**
 *
 * @author Škoda Petr
 */
public class SparqlUtilsException extends Exception {

    public SparqlUtilsException(String message) {
        super(message);
    }

    public SparqlUtilsException(String message, Throwable cause) {
        super(message, cause);
    }

    public SparqlUtilsException(Throwable cause) {
        super(cause);
    }

}
