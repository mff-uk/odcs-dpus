package cz.cuni.mff.xrg.uv.service.serialization.rdf;

import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide serialisation of simple POJO classes into rdf statements and back.
 *
 * Does not support generic classes, objects. The only supported generic are those with whose super type is
 * {@link java.util.Collection}.
 *
 * Collection must be initialised in given object.
 *
 * @author Škoda Petr
 * @param <T>
 */
public interface SerializationRdf<T> {

    /**
     * Configuration for {@link SerializationRdf}.
     */
    public class Configuration {

        private static final Logger LOG = LoggerFactory.getLogger(SerializationRdf.class);

        /**
         * Must end with "/".
         */
        private String ontologyPrefix = SerializationRdfOntology.BASE_URI_ONTOLOGY;

        /**
         * Must end with "/". Used only for conversion to rdf.
         */
        private String resourcesPrefix = SerializationRdfOntology.BASE_URI_RESOURCE;

        /**
         * Property map (uri, property). To denote property under some other property use '.'.
         * Must not have null values as "property".
         */
        private final Map<String, String> propertyMap = new HashMap<>();

        public Configuration() {
        }

        /**
         * Create a new configuration for given property.
         *
         * @param config
         * @param property
         */
        Configuration(Configuration config, String property) {
            this.ontologyPrefix = config.ontologyPrefix + property + "/";
            this.resourcesPrefix = config.resourcesPrefix + property + "/";
            // Copy properties.
            LOG.trace("Sub-config for: {}", property);
            for (String propertyUri : config.propertyMap.keySet()) {
                final String propertyName = config.propertyMap.get(propertyUri);
                // Check if it's under given property and check that we do not insert out selfs.
                if (propertyName.startsWith(property) && propertyName.length() > property.length()) {
                    final String newPropertyName = propertyName.substring(property.length() + 1);
                    LOG.trace("{} -> {} (old: '{}')", propertyUri, newPropertyName, propertyName);
                    propertyMap.put(propertyUri, newPropertyName);
                }
            }
        }

        public String getOntologyPrefix() {
            return ontologyPrefix;
        }

        public void setOntologyPrefix(String ontologyPrefix) {
            this.ontologyPrefix = ontologyPrefix;
        }

        public String getResourcesPrefix() {
            return resourcesPrefix;
        }

        public void setResourcesPrefix(String resourcesPrefix) {
            this.resourcesPrefix = resourcesPrefix;
        }

        /**
         *
         * @return Property map (uri; property).
         */
        public Map<String, String> getPropertyMap() {
            return propertyMap;
        }
        
    }

    /**
     *
     * @param rdf
     * @param rootUri Root subject for object representation.
     * @param object
     * @param config
     * @throws SerializationRdfFailure
     */
    void rdfToObject(RDFDataUnit rdf, URI rootUri, T object, Configuration config)
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
    List<Statement> objectToRdf(T object, URI rootUri, ValueFactory valueFactory, Configuration config)
            throws SerializationRdfFailure;

}
