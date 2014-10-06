package cz.cuni.mff.xrg.uv.service.serialization.rdf;

import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
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
 * The null values are not serialised.
 *
 * @author Škoda Petr
 * @param <T>
 */
class SerializationRdfSimple<T> implements SerializationRdf<T> {

    private class Context {

        public final ValueFactory valueFactory;

        public Integer subObjectCounter = 0;

        private final List<Statement> result;

        public Context(ValueFactory valueFactory, List<Statement> result) {
            this.valueFactory = valueFactory;
            this.result = result;
        }

        public void add(Resource rsrc, URI uri, Value value) {
            result.add(valueFactory.createStatement(rsrc, uri, value));
        }

    }

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(
            SerializationRdfSimple.class);

    SerializationRdfSimple() {

    }

    @Override
    public List<Statement> objectToRdf(T object, URI rootUri,
            ValueFactory valueFactory) throws SerializationRdfFailure {
        return objectToRdf(object, rootUri, valueFactory, new Configuration());
    }

    @Override
    public List<Statement> objectToRdf(T object, URI rootUri,
            ValueFactory valueFactory, Configuration config) throws SerializationRdfFailure {
        final Context ctx = new Context(valueFactory,
                new LinkedList<Statement>());
        convertToRdf(object, rootUri, ctx, null, true, config);
        return ctx.result;
    }

    @Override
    public void rdfToObject(RDFDataUnit rdf, URI rootUri, T object) throws SerializationRdfFailure {
        rdfToObject(rdf, rootUri, object, new Configuration());
    }

    @Override
    public void rdfToObject(RDFDataUnit rdf, URI rootUri, T object,
            Configuration config) throws SerializationRdfFailure {
        RepositoryConnection conn = null;
        try {
            conn = rdf.getConnection();

            // get read graphs
            List<URI> sourceGraphs = new LinkedList<>();
            RDFDataUnit.Iteration iter = rdf.getIteration();
            while (iter.hasNext()) {
                sourceGraphs.add(iter.next().getDataGraphURI());
            }
            URI[] graphs = sourceGraphs.toArray(new URI[0]);
            iter.close();

            convertFromRdf(conn, graphs, rootUri, object, config);
        } catch (DataUnitException | RepositoryException ex) {
            throw new SerializationRdfFailure("Can't get satements about: "
                    + rootUri.stringValue(), ex);
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

    private void convertToRdf(Object toConvert, URI rootUri, Context ctx,
            URI preferedPredicate, boolean firstLevel, Configuration config) {
        if (preferedPredicate == null) {
            preferedPredicate = ctx.valueFactory.createURI(
                    config.getBasePredicateUri() + SerializationRdfOntology.P_HAS_VALUE);
        }
        // check for type
        if (toConvert instanceof String || toConvert instanceof Number
                || toConvert instanceof Boolean || toConvert.getClass()
                .isPrimitive()) {
            final String valueStr = toConvert.toString();
            ctx.add(rootUri, preferedPredicate,
                    ctx.valueFactory.createLiteral(valueStr));
        } else if (toConvert instanceof Iterable) {
            final Iterator iterator = ((Iterable) toConvert).iterator();
            while (iterator.hasNext()) {
                final Object obj = iterator.next();
                // if it's simple object then it will use given
                // predicate and just add values, if it's complex then
                // it will create a new root
                convertToRdf(obj, rootUri, ctx, preferedPredicate, false, config);
            }
        } else {
            // complex object, we need to decide if we create new root
            // object or not
            // we create if we are in list, we do not create if
            // this is firt level
            if (!firstLevel) {
                final URI subURI = ctx.valueFactory.createURI(
                        config.getBaseResourceUri()
                        + SerializationRdfOntology.PREFIX_OBJECT
                        + (++ctx.subObjectCounter).toString());
                // connect with predicate
                ctx.add(rootUri, preferedPredicate, subURI);
                // and substitute as rootUri
                rootUri = subURI;
            }
            // parse object
            final Class<?> clazz = toConvert.getClass();
            final Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                final String fieldName = field.getName();
                try {
                    final PropertyDescriptor desc = new PropertyDescriptor(
                            fieldName,
                            clazz);
                    final Method readMethod = desc.getReadMethod();
                    if (readMethod == null) {
                        LOG.warn("Missing getter for {}.{}",
                                clazz.getSimpleName(),
                                fieldName);
                        continue;
                    }
                    final Object value = readMethod.invoke(toConvert);
                    if (value == null) {
                        // skip
                        continue;
                    }
                    // prepare predicate and use translation if it exists
                    String fieldPredicateName = fieldName;
                    if (config.getPropertyMap().containsKey(fieldName)) {
                        fieldPredicateName = config.getPropertyMap().get(fieldName);
                    }
                    final URI propertyURI = ctx.valueFactory.createURI(
                            config.getBasePredicateUri()
                            + SerializationRdfOntology.PREFIX_PROPERTY
                            + fieldPredicateName);
                    // parse
                    convertToRdf(value, rootUri, ctx, propertyURI, false, config);
                } catch (IntrospectionException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    LOG.warn("Failed to serialize {}.{}", clazz.getSimpleName(),
                            fieldName); // ex
                }
            }
        }
    }

    private void convertFromRdf(RepositoryConnection conn, URI[] graphs,
            URI rootUri, Object object, Configuration config)
            throws SerializationRdfFailure, RepositoryException {
        // get rdf data about given URI
        final List<Statement> statements = new ArrayList<>(20);
        RepositoryResult<Statement> repoResult
                = conn.getStatements(rootUri, null, null, true, graphs);
        while (repoResult.hasNext()) {
            statements.add(repoResult.next());
        }

        LOG.debug("Statements about subject({}): {}", rootUri.stringValue(), statements.size());

        final Class<?> clazz = object.getClass();
        for (Statement statement : statements) {
            if (statement.getSubject().stringValue().compareTo(
                    rootUri.stringValue()) != 0) {
                // skip as it does not corespond to us - our object
                continue;
            }
            final String predicateStr = statement.getPredicate().stringValue();
            String fieldName = predicateStr.substring(
                    predicateStr.lastIndexOf('/') + 1);
            // if translation exists then apply it
            if (config.getPropertyMap().containsKey(fieldName)) {
                fieldName = config.getPropertyMap().get(fieldName);
            }

            // get field type
            try {
                final PropertyDescriptor propDesc
                        = new PropertyDescriptor(fieldName, clazz);
                final Class<?> propClass = propDesc.getPropertyType();
                final Method writeMethod = propDesc.getWriteMethod();

                // get string value
                final String objectStr = statement.getObject().stringValue();

                Object value = convertPrimitiveFromRdf(propClass, objectStr);
                if (value != null) {
                    // it's a primitive type
                    writeMethod.invoke(object, value);
                } else if (Collection.class.isAssignableFrom(propClass)) {
                    // it's a collections
                    final Field field = clazz.getDeclaredField(fieldName);
                    final Class<?> innerClass = getCollectionGenericType(
                            field.getGenericType());
                    if (innerClass == null) {
                        throw new SerializationRdfFailure(
                                "Can't get type of Collection for: " + fieldName);
                    }

                    // TODO Cache type here!
                    // get read method as we need to call add method on
                    // given collection
                    final Method readMethod = propDesc.getReadMethod();
                    final Collection collection = (Collection) readMethod
                            .invoke(object);
                    // try conversion
                    value = convertPrimitiveFromRdf(innerClass, objectStr);
                    if (value != null) {
                        // it's primitive
                        collection.add(value);
                    } else {
                        // complex type
                        value = convertObjectFromRdf(conn, graphs, innerClass,
                                statement.getObject(), config);
                        collection.add(value);
                    }
                } else {
                    // it's object = complex type
                    value = convertObjectFromRdf(conn, graphs, propClass,
                            statement.getObject(), config);
                    writeMethod.invoke(object, value);
                }

            } catch (IntrospectionException | IllegalAccessException |
                    IllegalArgumentException | InvocationTargetException |
                    NoSuchFieldException | SecurityException ex) {
                LOG.warn("Failed to set {}.{}", clazz.getSimpleName(),
                        fieldName, ex);
            }

        }
    }

    /**
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
     *
     * @param genType
     * @return Null if type can not be obtained.
     */
    private Class<?> getCollectionGenericType(Type genType) {
        if (!(genType instanceof ParameterizedType)) {
            LOG.warn(
                    "Superclass it not ParameterizedType");
            return null;
        }
        final Type[] params
                = ((ParameterizedType) genType)
                .getActualTypeArguments();
        // we know there should be just one for Collection
        if (params.length != 1) {
            LOG.warn(
                    "Unexpected number of generic types: {} (1 expected)",
                    params.length);
            return null;
        }
        if (!(params[0] instanceof Class)) {
            LOG.warn("Unexpected type '{}'",
                    params[0]
                    .toString());
            return null;
        }
        return (Class<?>) params[0];
    }

    /**
     *
     * @param rdf
     * @param clazz
     * @param value
     * @return
     */
    private Object convertObjectFromRdf(RepositoryConnection conn, URI[] graphs,
            Class<?> clazz, Value value, Configuration config)
            throws SerializationRdfFailure, RepositoryException {
        if (value instanceof URI) {
            // complex object
            final Object result;
            try {
                result = clazz.newInstance();
            } catch (IllegalAccessException | InstantiationException ex) {
                throw new SerializationRdfFailure(
                        "Can't create instance of object.", ex);
            }
            final URI baseUri = (URI) value;
            convertFromRdf(conn, graphs, baseUri, result, config);
            return result;
        } else {
            // ctor from string - literal, etc ..
            try {
                Constructor<?> ctr = clazz.getConstructor(String.class);
                // just create and return
                return ctr.newInstance(value.stringValue());
            } catch (NoSuchMethodException | SecurityException |
                    IllegalAccessException | IllegalArgumentException |
                    InstantiationException | InvocationTargetException ex) {
                throw new SerializationRdfFailure(
                        "Can't create instance of object, from ctor(String).",
                        ex);
            }
        }
    }

}
