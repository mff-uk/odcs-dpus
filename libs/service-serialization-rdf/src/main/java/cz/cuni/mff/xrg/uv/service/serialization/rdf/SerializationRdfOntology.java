package cz.cuni.mff.xrg.uv.service.serialization.rdf;

/**
 *
 * @author Škoda Petr
 */
public class SerializationRdfOntology {

    public static final String BASE_URI_ONTOLOGY = "http://linked.opendata.cz/ontology/libs/serialization-rdf/";

    public static final String BASE_URI_RESOURCE = "http://linked.opendata.cz/resource/libs/serialization-rdf/";

    /**
     * Predicate for connecting properties with class.
     */
    public static final String P_HAS_VALUE = "value";

    /**
     * Base URI for property URI generation.
     */
    public static final String PREFIX_PROPERTY = "property/";

    public static final String PREFIX_OBJECT = "object/";
    
    private SerializationRdfOntology() {
    }

}
