package cz.cuni.mff.xrg.uv.addressmapper.query;

/**
 *
 * @author Škoda Petr
 */
public class QueryException extends Exception {

    public QueryException(String message) {
        super(message);
    }

    public QueryException(String message, Throwable cause) {
        super(message, cause);
    }

    public QueryException(Throwable cause) {
        super(cause);
    }

}
