package cz.cuni.mff.xrg.uv.service.serialization.rdf;

import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;

/**
 * Provide serialisation of simple POJO classes into rdf statements and back.
 *
 * Does not support generic classes, objects. The only supported generic are
 * those with whose super type is {@link java.util.Collection}.
 *
 * Collection must be initialised in given object.
 *
 * @author Škoda Petr
 * @param <T>
 */
public interface SerializationRdf<T> {

    class Configuration {

        /**
         * Must end with "/";
         */
        private String basePredicateUri =
                SerializationRdfOntology.BASE_URI_ONTOLOGY;

        /**
         * Must end with "/";
         */
        private String baseResourceUri =
                SerializationRdfOntology.BASE_URI_RESOURCE;

        /**
         * Enables translation-mapping of properties to different names.
         * Map store pair fieldName,newName.
         *
         * Given mapping is used for all serialized and deserialised objects.
         */
        private final Map<String, String> propertyMap = new HashMap<>();

        /**
         * Default configuration.
         */
        public Configuration() {
        }

        /**
         *
         * @param basePredicateUri Based uri for predicates, must end with '/'.
         * @param baseResourceUri  Base uri for resources, must end with '/'.
         */
        public Configuration(String basePredicateUri, String baseResourceUri) {
            this.basePredicateUri = basePredicateUri;
            this.baseResourceUri = baseResourceUri;
        }

        public String getBasePredicateUri() {
            return basePredicateUri;
        }

        public String getBaseResourceUri() {
            return baseResourceUri;
        }

        public Map<String, String> getPropertyMap() {
            return propertyMap;
        }

    }

    /**
     *
     * @param rdf
     * @param rootUri Root subject for object representation.
     * @param object
     * @throws SerializationRdfFailure
     */
    void rdfToObject(RDFDataUnit rdf, URI rootUri, T object)
            throws SerializationRdfFailure;

    /**
     *
     * @param rdf
     * @param rootUri Root subject for object representation.
     * @param object
     * @param config
     * @throws SerializationRdfFailure
     */
    void rdfToObject(RDFDataUnit rdf, URI rootUri, T object,
            Configuration config)
            throws SerializationRdfFailure;

    /**
     *
     * @param object Instance of object for conversion.
     * @param rootUri
     * @param valueFactory
     * @return
     * @throws SerializationRdfFailure
     */
    List<Statement> objectToRdf(T object, URI rootUri,
            ValueFactory valueFactory)
            throws SerializationRdfFailure;
    
    /**
     *
     * @param object Instance of object for conversion.
     * @param rootUri
     * @param valueFactory
     * @param config
     * @return
     * @throws SerializationRdfFailure
     */
    List<Statement> objectToRdf(T object, URI rootUri,
            ValueFactory valueFactory, Configuration config)
            throws SerializationRdfFailure;

}
