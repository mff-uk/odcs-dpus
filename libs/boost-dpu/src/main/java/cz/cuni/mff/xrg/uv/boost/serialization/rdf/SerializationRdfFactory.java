package cz.cuni.mff.xrg.uv.boost.serialization.rdf;

/**
 * Factory to create instances of {@link SerializationRdf}.
 *
 * @author Škoda Petr
 */
public class SerializationRdfFactory {

    private SerializationRdfFactory() {
    }

    /**
     *
     * @return 
     */
    public static SerializationRdf rdfSimple() {
        return new SerializationRdfSimple();
    }

}
