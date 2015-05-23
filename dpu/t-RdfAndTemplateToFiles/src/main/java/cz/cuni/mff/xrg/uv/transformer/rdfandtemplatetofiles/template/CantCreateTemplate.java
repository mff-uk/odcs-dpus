package cz.cuni.mff.xrg.uv.transformer.rdfandtemplatetofiles.template;

/**
 *
 * @author Škoda Petr
 */
public class CantCreateTemplate extends Exception {

    public CantCreateTemplate(String message) {
        super(message);
    }

    public CantCreateTemplate(Throwable cause) {
        super(cause);
    }

    public CantCreateTemplate(String message, Throwable cause) {
        super(message, cause);
    }

}
