package cz.cuni.mff.xrg.uv.boost.serialization.rdf;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import cz.cuni.mff.xrg.uv.boost.serialization.SerializationFailure;
import cz.cuni.mff.xrg.uv.boost.serialization.SerializationFailure;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;

/**
 * Provide serialisation of simple POJO classes into rdf statements and back.
 *
 * Does not support generic classes, objects. The only supported generic are those with whose super type is
 * {@link java.util.Collection}.
 *
 * Collection must be initialised in given object.
 *
 * @author Škoda Petr
 */
public interface SerializationRdf {

    /**
     * Map RDF object to property.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Property {

        /**
         *
         * @return URI of predicate for this property.
         */
        String uri();

        /**
         *
         * @return Property description in RDF, used only during serialization into RDF.
         */
        String description() default "";

    }

    /**
     * Map object to RDF subject of given class. Used only for serialization into RDF.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Entity {

        /**
         *
         * @return Fully denoted entity class type as URI.
         */
        String type();

        /**
         *
         * @return If this object is used in other object that is being serialized then resource URI for this
         *         object must be created. The URI of parent is taken appended by number and then by this
         *         value.
         */
        String resourceSuffix() default "";

    }

    /**
     * Configuration for {@link SerializationRdf}, reflects annotations. If this class is used
     * is must contains full information about every serialized object in a object tree.
     */
    public static class Configuration {

        public static class Property {

            public String uri;

            public String description;

            public Property(String uri, String description) {
                this.uri = uri;
                this.description = description;
            }
            
        }

        public static class Entity {

            public String type;

            public String resourceSuffix;

            public Entity(String type, String resourceSuffix) {
                this.type = type;
                this.resourceSuffix = resourceSuffix;
            }

        }

        /**
         * Key is full name of the field: "{canonical name of class}.{field name}"
         */
        private final Map<String, Property> properties = new HashMap<>();

        /**
         * Used only during serialization to RDF.
         */
        private final Map<String, Entity> entities = new HashMap<>();

        public Map<String, Property> getProperties() {
            return properties;
        }

        public Map<String, Entity> getEntities() {
            return entities;
        }

    }

    /**
     *
     * @param connection
     * @param rootResource Root subject for object representation.
     * @param context Graphs from which read triples.
     * @param object Object to load data into.
     * @param config Null to load configuration from object.
     * @throws cz.cuni.mff.xrg.uv.service.serialization.SerializationFailure
     * @throws SerializationRdfFailure
     * @throws eu.unifiedviews.dataunit.DataUnitException
     * @throws org.openrdf.repository.RepositoryException
     */
    public void convert(RepositoryConnection connection, Resource rootResource, 
            List<RDFDataUnit.Entry> context, Object object, Configuration config)
            throws SerializationFailure, SerializationRdfFailure, DataUnitException, RepositoryException;

    /**
     *
     * @param object Instance of object for conversion.
     * @param rootResource
     * @param valueFactory
     * @param config
     * @return List of statement representing the object.
     * @throws cz.cuni.mff.xrg.uv.service.serialization.SerializationFailure
     * @throws SerializationRdfFailure
     */
    public List<Statement> convert(Object object, Resource rootResource, ValueFactory valueFactory,
            Configuration config) throws SerializationFailure, SerializationRdfFailure;

}
