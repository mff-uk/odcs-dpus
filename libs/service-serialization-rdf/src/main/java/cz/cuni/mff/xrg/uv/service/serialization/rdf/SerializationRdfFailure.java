package cz.cuni.mff.xrg.uv.service.serialization.rdf;

/**
 *
 * @author Škoda Petr
 */
public class SerializationRdfFailure extends Exception {

    public SerializationRdfFailure(String message) {
        super(message);
    }

    public SerializationRdfFailure(String message, Throwable cause) {
        super(message, cause);
    }

    public SerializationRdfFailure(Throwable cause) {
        super(cause);
    }

}
