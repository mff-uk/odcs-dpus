package cz.cuni.mff.xrg.uv.serialization.rdf;

/**
 *
 * @author Škoda Petr
 */
public class Ontology {

    private static final String BASE_URI = "http://linked.opendata.cz/resource/libs/serialization-rdf/";

    /**
     * Used as a type for base URI.
     */
    public static final String O_TYPE_CLASS = BASE_URI + "Class";

    /**
     * Type for property representation.
     */
    public static final String O_TYPE_PROPERTY = BASE_URI + "Property";

    public static final String P_RDF_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

    /**
     * Predicate for connecting properties with class.
     */
    public static final String P_HAS_PROPERTY = BASE_URI + "hasProperty";

    /**
     * Predicate representing string value of property.
     */
    public static final String P_VALUE = BASE_URI + "value";

    /**
     * Base URI for property URI generation.
     */
    public static final String PREFIX_PROPERTY = BASE_URI + "property/";

    /**
     * Version of used serialisation.
     */
    public static final String P_VERSION = BASE_URI + "version";
    
    private Ontology() {
    }

}
