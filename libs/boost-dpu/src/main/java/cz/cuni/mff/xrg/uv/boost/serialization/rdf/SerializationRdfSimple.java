package cz.cuni.mff.xrg.uv.boost.serialization.rdf;

import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.openrdf.model.*;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.uv.boost.serialization.SerializationUtils;
import cz.cuni.mff.xrg.uv.boost.serialization.SerializationFailure;
import cz.cuni.mff.xrg.uv.utils.dataunit.rdf.RdfDataUnitUtils;

/**
 * Very simple rdf serialisation class for string and integers.
 *
 * Known limitations:
 * <ul>
 * <li>The null values are not serialised.</li>
 * <li>Can't serialise cycles, will cause program to endless loop.</li>
 * <li>Does not support serialisation for now!</li>
 * </ul>
 *
 * @author Škoda Petr
 * @param <T>
 */
class SerializationRdfSimple implements SerializationRdf {

    private static final Logger LOG = LoggerFactory.getLogger(SerializationRdfSimple.class);

    /**
     * Holder for access to RDF just to decrease number of parameters in functions.
     */
    private class Context {
        
        final RepositoryConnection connection;
        
        final URI[] graphs;
        
        final Configuration config;

        public Context(RepositoryConnection connection, URI[] graphs, Configuration config) {
            this.connection = connection;
            this.graphs = graphs;
            this.config = config;
        }
        
    }

    @Override
    public void convert(RepositoryConnection connection, Resource rootResource,
            List<RDFDataUnit.Entry> context, Object object, Configuration config)
            throws SerializationFailure, SerializationRdfFailure, DataUnitException, RepositoryException {
        if (config == null) {
            config = SerializationUtils.createConfiguration(object.getClass());
        }
        // Get read graphs.
        final URI[] graphs = RdfDataUnitUtils.asGraphs(context);
        // Load object.
        loadIntoObject(new Context(connection, graphs, config), rootResource, object);
    }

    @Override
    public List<Statement> convert(Object object, Resource rootResource, ValueFactory valueFactory,
            Configuration config) throws SerializationFailure, SerializationRdfFailure {
        
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Load configuration into givne object.
     *
     * @param context
     * @param rootResource
     * @param object
     * @throws SerializationFailure
     * @throws SerializationRdfFailure
     * @throws DataUnitException
     * @throws RepositoryException
     */
    private void loadIntoObject(Context context, Resource rootResource, Object object)
            throws SerializationFailure, SerializationRdfFailure, DataUnitException, RepositoryException {
        LOG.debug("loadIntoObject(, {}, {})", rootResource.stringValue(), object.getClass().getSimpleName());
        // Get rdf data about given resource.
        final List<Statement> statements = new ArrayList<>(20);
        final RepositoryResult<Statement> repoResult  =
                context.connection.getStatements(rootResource, null, null, true, context. graphs);
        try {
            while (repoResult.hasNext()) {
                statements.add(repoResult.next());
            }
        } finally {
            repoResult.close();
        }
        LOG.debug("Statements about subject(<{}>): {}", rootResource.stringValue(), statements.size());
        // Parse loaded statements.
        final Class<?> clazz = object.getClass();
        for (Statement statement : statements) {
            // We know that all subjects are equal to rootResource. The predicates
            // need to be map to configurations.
            final String predicateStr = statement.getPredicate().stringValue();
            final String propertyName = getPropertyName(context.config, predicateStr);
            if (propertyName == null) {
                // Uknown predicate log and skip the predicate.
                LOG.debug("Ignoring unknown predicate: {}", predicateStr);
                continue;
            }
            // Get field type.

            // Property might be doneted with parent class.
            final String newFieldName = propertyName.substring(propertyName.lastIndexOf('.') + 1);
            // Get setter and informations about type.
            final PropertyDescriptor fieldDesc;
            try {
                fieldDesc = new PropertyDescriptor(newFieldName, clazz);
            } catch (IntrospectionException ex) {
                throw new SerializationRdfFailure("Can't get property descriptor for: " + propertyName
                        + " and class: " + clazz.getSimpleName(), ex);
            }
            final Class<?> fieldClass = fieldDesc.getPropertyType();
            final Method fieldSetter = fieldDesc.getWriteMethod();
            // Try conversion for primitive type.
            Object value = loadPrimitiveType(fieldClass, statement.getObject().stringValue());
            if (value != null) {
                // It's primitive type.
            } else if (Collection.class.isAssignableFrom(fieldClass)) {                    
                // It's a collection, analyze type.
                final Field field;
                try {
                    field = clazz.getDeclaredField(newFieldName);
                } catch (NoSuchFieldException | SecurityException ex) {
                    throw new SerializationRdfFailure("Can't get field class.", ex);
                }
                // We will add new value into collection.
                loadIntoCollection(context, propertyName, object, field, fieldDesc, statement.getObject());
                // And continue to not set the value.
                continue;
            } else if (Map.class.isAssignableFrom(fieldClass)) {
                LOG.warn("Map<> class is not suported! Ignoring field: {}", propertyName);
            } else {
                // It's complex type.
                value = loadNewObject(context, clazz, statement.getObject());
            }
            // Set field.
            try {
                fieldSetter.invoke(object, value);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                throw new SerializationRdfFailure("Can't set propety " + propertyName
                        + " during rdf deserialization to value: " + value, ex);
            }
        } 
    }

    /**
     *
     * @param config
     * @param uri
     * @return Full name of property or null if no match for given URI has been found.
     */
    private String getPropertyName(Configuration config, String uri) {
        for (String propertyName : config.getProperties().keySet()) {
            if (config.getProperties().get(propertyName).uri.compareTo(uri) == 0) {
                return propertyName;
            }
        }
        return null;
    }

    /**
     * Convert string into primitive type of given class.
     *
     * @param clazz
     * @param objectStr
     * @return Null if object is not of a primitive type.
     */
    private Object loadPrimitiveType(Class<?> clazz, String objectStr) {
        if (clazz == String.class) {
            return objectStr;
        } else if (clazz == boolean.class || clazz == Boolean.class) {
            return Boolean.parseBoolean(objectStr);
        } else if (clazz == byte.class || clazz == Byte.class) {
            return Byte.parseByte(objectStr);
        } else if (clazz == short.class || clazz == Short.class) {
            return Short.parseShort(objectStr);
        } else if (clazz == int.class || clazz == Integer.class) {
            return Integer.parseInt(objectStr);
        } else if (clazz == long.class || clazz == Long.class) {
            return Long.parseLong(objectStr);
        } else if (clazz == float.class || clazz == Float.class) {
            return Float.parseFloat(objectStr);
        } else if (clazz == double.class || clazz == Double.class) {
            return Double.parseDouble(objectStr);
        } else {
            return null;
        }
    }

    /**
     * Create and add new object into collection.
     *
     * @param context
     * @param fieldName Full name of field with class name.
     * @param ownerObject Owner object.
     * @param collectionField Field of collection.
     * @param descriptor Property descriptor of collection.
     * @param collectionValue RDF value of new item.
     * @throws SerializationFailure
     * @throws SerializationRdfFailure
     * @throws DataUnitException
     * @throws RepositoryException
     */
    private void loadIntoCollection(Context context, String fieldName, Object ownerObject,
              Field collectionField, PropertyDescriptor descriptor, Value collectionValue)
            throws SerializationFailure, SerializationRdfFailure, DataUnitException, RepositoryException {
        LOG.debug("loadIntoCollection(, {}, {}, ... ,{})", fieldName, ownerObject.getClass().getSimpleName(), collectionValue);
        // Get collection type.
        final Class<?> innerClass = SerializationUtils.getCollectionGenericType(collectionField.getGenericType());
        if (innerClass == null) {
            throw new SerializationFailure("Can't get type of Collection for: " + fieldName);
        }
        // Get read method as we need to call add method on given collection.
        final Method readMethod = descriptor.getReadMethod();        
        final Collection collection;
        try {
            collection = (Collection) readMethod.invoke(ownerObject);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new SerializationRdfFailure("Can't read value of collection: " + fieldName, ex);
        }
        if (collection == null) {
            throw new SerializationRdfFailure("Collection must be initialized prio to loading. Collection: "
                    + fieldName + " on class: " + ownerObject.getClass().getCanonicalName());
        }
        // try conversion again
        Object value = loadPrimitiveType(innerClass, collectionValue.stringValue());
        if (value != null) {
            // It's primitive in collection, so just add the value.
        } else {
            // Complex type.
            value = loadNewObject(context, innerClass, collectionValue);
        }
        // Add to collection.
        collection.add(value);
    }

    /**
     * Create new object and load data into it.
     *
     * @param context
     * @param objectType
     * @param objectValue
     * @return
     * @throws SerializationFailure
     * @throws SerializationRdfFailure
     * @throws DataUnitException
     * @throws RepositoryException
     */
    private Object loadNewObject(Context context, Class<?> objectType, Value objectValue)
            throws SerializationFailure, SerializationRdfFailure, DataUnitException, RepositoryException {
        LOG.debug("loadNewObject(, {}, {})", objectType.getSigners(), objectValue.stringValue());
        Object result;
        // It's a complex object - class.
        if (objectValue instanceof URI) {
            // Another resource in rdf, create and load data into it.
            result = SerializationUtils.createInstance(objectType);
            loadIntoObject(context, (URI)objectValue, result);
        } else {
            // Ctor from string - literal, etc ..
            try {
                final Constructor<?> ctr = objectType.getConstructor(String.class);
                result = ctr.newInstance(objectValue.stringValue());
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException |
                    IllegalArgumentException | InstantiationException | InvocationTargetException ex) {
                throw new SerializationRdfFailure(
                        "Can't create instance of object, from ctor(String).", ex);
            }
        }
        return result;
    }

}
