package cz.cuni.mff.xrg.uv.boost.serialization.rdf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.Resource;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import cz.cuni.mff.xrg.uv.boost.serialization.SerializationFailure;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;

/**
 * Provide serialisation of simple POJO classes into rdf statements and back.
 *
 * Does not support generic classes. The only supported generic are those with whose super type is
 * {@link java.util.Collection}.
 *
 * Collection must be initialised in given object.
 *
 * @author Škoda Petr
 */
public interface SerializationRdf {



    /**
     * Configuration for {@link SerializationRdf}, reflects annotations. If this class is used
     * is must contains full information about every serialized object in a object tree.
     */
    public static class Configuration {

        public static class Property {

            public String uri;

            public Property(String uri) {
                this.uri = uri;
            }
            
        }

        public static class Entity {

            public String type;

            public Entity(String type) {
                this.type = type;
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

}
