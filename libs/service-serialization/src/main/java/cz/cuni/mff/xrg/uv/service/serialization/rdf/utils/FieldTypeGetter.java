package cz.cuni.mff.xrg.uv.service.serialization.rdf.utils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Škoda Petr
 */
public class FieldTypeGetter {

    private static final Logger LOG = LoggerFactory.getLogger(FieldTypeGetter.class);

    private FieldTypeGetter() {
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

}
