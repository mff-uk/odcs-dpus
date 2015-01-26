package cz.cuni.mff.xrg.uv.boost.serialization;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.uv.boost.ontology.Ontology;
import cz.cuni.mff.xrg.uv.boost.serialization.rdf.SerializationRdf;

/**
 * Common utility class for serialization.
 *
 * @author Škoda Petr
 */
public class SerializationUtils {

    private static final Logger LOG = LoggerFactory.getLogger(SerializationUtils.class);

    private SerializationUtils() {
    }

    /**
     *
     * @param <T>
     * @param clazz
     * @return Instance of given class.
     * @throws SerializationFailure
     */
    public static <T> T createInstance(Class<T> clazz) throws SerializationFailure {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new SerializationFailure(e);
        }
    }

    /**
     * Get type of collection.
     *
     * @param genType
     * @return Null if type can not be obtained.
     */
    public static Class<?> getCollectionGenericType(Type genType) {
        if (!(genType instanceof ParameterizedType)) {
            LOG.warn("Superclass it not ParameterizedType");
            return null;
        }
        final Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        // We know there should be just one for Collection.
        if (params.length != 1) {
            LOG.warn("Unexpected number of generic types: {} (1 expected)", params.length);
            return null;
        }
        if (!(params[0] instanceof Class)) {
            LOG.warn("Unexpected type '{}'", params[0].toString());
            return null;
        }
        return (Class<?>) params[0];
    }

    /**
     * Generate RDF serialization configuration (description) for given class.
     *
     * @param clazz
     * @return
     * @throws cz.cuni.mff.xrg.uv.boost.serialization.SerializationFailure
     */
    public static SerializationRdf.Configuration createConfiguration(Class<?> clazz) throws SerializationFailure {
        return createConfiguration(clazz, null);
    }

    /**
     *
     * @param clazz
     * @param config Configuration class to use. Use null to use empty configuration instance.
     * @return
     */
    private static SerializationRdf.Configuration createConfiguration(Class<?> clazz,
            SerializationRdf.Configuration config) throws SerializationFailure {
        if (config == null) {
            config = new SerializationRdf.Configuration();
        }
        final String className = clazz.getCanonicalName();
        // Load entity annotation.
        final Ontology.Entity entity = clazz.getAnnotation(Ontology.Entity.class);
        final SerializationRdf.Configuration.Entity entityConfig;
        final String baseOntologyURI;
        if (entity != null) {
            entityConfig = new SerializationRdf.Configuration.Entity(entity.type(), entity.resourceSuffix());
            // Use type as default ontology prefix.
            baseOntologyURI = entity.type();
        } else {
            throw new SerializationFailure("Missing entity annotaation for class: " + clazz.getSimpleName());
        }
        config.getEntities().put(className, entityConfig);
        // Iterate over fields.
        final Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            final Class<?> fieldClass = field.getType();
            // Load property anottation.
            final Ontology.Property property = field.getAnnotation(Ontology.Property.class);
            final SerializationRdf.Configuration.Property propertyConfig;
            if (property != null) {
                propertyConfig = new SerializationRdf.Configuration.Property(property.uri(),
                        property.description());
            } else {
                // We need to generate uri -> we need base URI.
                if (baseOntologyURI == null) {
                    throw new SerializationFailure("Missing uri for: " + field.getName() +
                            " and no general base URI is provided.");
                }
                propertyConfig = new SerializationRdf.Configuration.Property(
                        baseOntologyURI + "/" + field.getName(), "");
            }
            config.getProperties().put(className + "." + field.getName(), propertyConfig);
            // Check if we do not need to load other object.
            if (Collection.class.isAssignableFrom(fieldClass)) {
                // It's a collection, analyze type.
                createConfiguration(getCollectionGenericType(field.getGenericType()), config);
            } else if (fieldClass.isSynthetic()) {
                createConfiguration(fieldClass, config);
            }
        }
        return config;
    }

}
