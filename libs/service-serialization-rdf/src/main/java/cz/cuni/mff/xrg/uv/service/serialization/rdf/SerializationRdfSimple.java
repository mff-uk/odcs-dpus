package cz.cuni.mff.xrg.uv.service.serialization.rdf;

import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.openrdf.model.*;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.slf4j.LoggerFactory;

/**
 * Very simple rdf serialisation class for string and integers.
 *
 * Known limitations:
 * <ul>
 * <li>The null values are not serialised.</li>
 * <li>Can't serialise cycles, will cause program to endless loop.</li>
 * <li>Does not support serialisation for now!</li>
 * </ul>
 * @author Škoda Petr
 * @param <T>
 */
class SerializationRdfSimple<T> implements SerializationRdf<T> {

    /**
     * Context used to group information for deserialise from rdf.
     */
    private class FromRdfContext {
        
        RepositoryConnection conn;
        
        URI[] graphs;

        public FromRdfContext(RepositoryConnection conn, URI[] graphs) {
            this.conn = conn;
            this.graphs = graphs;
        }
                
    }

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SerializationRdfSimple.class);

    SerializationRdfSimple() {

    }

    @Override
    public List<Statement> objectToRdf(T object, URI rootUri, ValueFactory valueFactory, Configuration config)
            throws SerializationRdfFailure {
        throw new UnsupportedOperationException();
    }


    @Override
    public void rdfToObject(RDFDataUnit rdf, URI rootUri, T object, Configuration config)
            throws SerializationRdfFailure {
        RepositoryConnection conn = null;
        try {
            conn = rdf.getConnection();
            // Get read graphs.
            List<URI> sourceGraphs = new LinkedList<>();
            try (RDFDataUnit.Iteration iter = rdf.getIteration()) {
                while (iter.hasNext()) {
                    sourceGraphs.add(iter.next().getDataGraphURI());
                }
            }
            final FromRdfContext context = new FromRdfContext(conn, sourceGraphs.toArray(new URI[0]));
            convertFromRdf(context, rootUri, object, config);
        } catch (DataUnitException | RepositoryException ex) {
            throw new SerializationRdfFailure("Can't get satements about: " + rootUri.stringValue(), ex);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (RepositoryException ex) {
                    LOG.warn("Error in close.", ex);
                }
            }
        }
    }

    private void convertFromRdf(FromRdfContext context, URI rootUri, Object object, Configuration config)
            throws SerializationRdfFailure, RepositoryException {
        // Get rdf data about given URI.
        final List<Statement> statements = new ArrayList<>(20);
        RepositoryResult<Statement> repoResult = 
                context.conn.getStatements(rootUri, null, null, true, context.graphs);
        while (repoResult.hasNext()) {
            statements.add(repoResult.next());
        }

        LOG.debug("Statements about subject({}): {}", rootUri.stringValue(), statements.size());

        final Class<?> clazz = object.getClass();
        for (Statement statement : statements) {
            if (statement.getSubject().stringValue().compareTo(rootUri.stringValue()) != 0) {
                // Skip as it does not corespond to us - our object, subject.
                continue;
            }
            final String predicateStr = statement.getPredicate().stringValue();
            // Get property name.
            String fieldName = config.getPropertyMap().get(predicateStr);
            if (fieldName == null) {
                // No mapping for this, so we parse the uri by hand.
                fieldName = predicateStr.substring(config.getOntologyPrefix().length());
            }
            // Get string value of current statement.
            final String objectStr = statement.getObject().stringValue();
            // Get field type.
            try {
                final PropertyDescriptor propDesc = new PropertyDescriptor(fieldName, clazz);
                final Class<?> propClass = propDesc.getPropertyType();
                final Method writeMethod = propDesc.getWriteMethod();
                // Try to convert as a primitive type.
                final Object value = convertPrimitiveFromRdf(propClass, objectStr);
                if (value != null) {
                    // It's a primitive type.
                    writeMethod.invoke(object, value);
                } else if (Collection.class.isAssignableFrom(propClass)) {
                    // It's a collections.
                    final Field field = clazz.getDeclaredField(fieldName);
                    final Class<?> innerClass = getCollectionGenericType(field.getGenericType());
                    if (innerClass == null) {
                        throw new SerializationRdfFailure("Can't get type of Collection for: " + fieldName);
                    }

                    // TODO Cache type here!

                    // Get read method as we need to call add method on given collection.
                    final Method readMethod = propDesc.getReadMethod();
                    final Collection collection = (Collection) readMethod.invoke(object);
                    // try conversion again
                    final Object valueInCollection = convertPrimitiveFromRdf(innerClass, objectStr);
                    if (value != null) {
                        // It's primitive in collection, so just add the value.
                        collection.add(valueInCollection);
                    } else {
                        // Complex type.
                        final Object objectValue = convertObjectFromRdf(context, innerClass,
                                statement.getObject(), fieldName, config);
                        collection.add(objectValue);
                    }
                } else {
                    // It's object = complex type.
                    final Object objectValue = convertObjectFromRdf(context, propClass,
                            statement.getObject(), fieldName, config);
                    writeMethod.invoke(object, objectValue);
                }

            } catch (IntrospectionException | IllegalAccessException | IllegalArgumentException |
                    InvocationTargetException | NoSuchFieldException | SecurityException ex) {
                LOG.warn("Failed to set {}.{}", clazz.getSimpleName(), fieldName, ex);
            }

        }
    }

    /**
     * Convert string into object.
     *
     * @param clazz
     * @param objectStr
     * @return Null if object is not of a primitive type.
     */
    private Object convertPrimitiveFromRdf(Class<?> clazz, String objectStr) {
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
     * Get type of collection.
     *
     * @param genType
     * @return Null if type can not be obtained.
     */
    private Class<?> getCollectionGenericType(Type genType) {
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
     * Try to deserialise an object. If only literal is given then tries to construct the object directly,
     * otherwise it create new object and call
     * {@link #convertFromRdf(cz.cuni.mff.xrg.uv.service.serialization.rdf.SerializationRdfSimple.FromRdfContext, org.openrdf.model.URI, java.lang.Object, cz.cuni.mff.xrg.uv.service.serialization.rdf.SerializationRdf.Configuration)}
     * to deserialise newly created object.
     *
     * @param context
     * @param clazz
     * @param value
     * @param propertyName
     * @param config
     * @return
     * @throws SerializationRdfFailure
     * @throws RepositoryException
     */
    private Object convertObjectFromRdf(FromRdfContext context, Class<?> clazz, Value value,
            String propertyName, Configuration config) throws SerializationRdfFailure, RepositoryException {
        if (value instanceof URI) {
            // Complex object -> create a new instance.
            final Object result;
            try {
                result = clazz.newInstance();
            } catch (IllegalAccessException | InstantiationException ex) {
                throw new SerializationRdfFailure("Can't create instance of object.", ex);
            }
            final URI baseUri = (URI) value;
            // Use new configuration.
            final Configuration newConfiguration = new Configuration(config, propertyName);
            convertFromRdf(context, baseUri, result, newConfiguration);
            return result;
        } else {
            // Ctor from string - literal, etc ..
            try {
                Constructor<?> ctr = clazz.getConstructor(String.class);
                // Just create and return.
                return ctr.newInstance(value.stringValue());
            } catch (NoSuchMethodException | SecurityException |
                    IllegalAccessException | IllegalArgumentException |
                    InstantiationException | InvocationTargetException ex) {
                throw new SerializationRdfFailure("Can't create instance of object, from ctor(String).", ex);
            }
        }
    }

}
