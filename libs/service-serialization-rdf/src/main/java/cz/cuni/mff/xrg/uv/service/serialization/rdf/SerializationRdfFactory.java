package cz.cuni.mff.xrg.uv.service.serialization.rdf;

/**
 *
 * @author Škoda Petr
 */
public class SerializationRdfFactory {

    private SerializationRdfFactory() {
    }

    /**
     *
     * @param <T>
     * @param clazz
     * @return Class for very simple rdf serialisation.
     */
    public static <T> SerializationRdf<T> serializationRdfSimple(Class<T> clazz) {
        return new SerializationRdfSimple<>();
    }

}
