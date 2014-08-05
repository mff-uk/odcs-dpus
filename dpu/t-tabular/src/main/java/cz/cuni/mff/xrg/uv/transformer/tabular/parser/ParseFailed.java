package cz.cuni.mff.xrg.uv.transformer.tabular.parser;

/**
 *
 * @author Škoda Petr
 */
public class ParseFailed extends Exception {

    public ParseFailed(String message) {
        super(message);
    }

    public ParseFailed(String message, Throwable cause) {
        super(message, cause);
    }

}
