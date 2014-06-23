package cz.cuni.mff.xrg.uv.postaladdress.to.ruain.query;

/**
 *
 * @author Škoda Petr
 */
public class EmptyQueryException extends QueryException {

    public EmptyQueryException() {
        super("No requirementes have been found.");
    }
    
}
