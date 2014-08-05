package cz.cuni.mff.xrg.uv.service.serialization.xml;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.mapper.MapperWrapper;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Is not thread save.
 *
 * @author Škoda Petr
 */
public class SerializationXmlGeneralImpl implements SerializationXmlGeneral {

    private static final Logger LOG = LoggerFactory.getLogger(
            SerializationXmlGeneralImpl.class);

    protected final XStream xstream;

    protected final LinkedList<String> loadedFields = new LinkedList<>();

    SerializationXmlGeneralImpl() {

        this.xstream = new XStream() {
            @Override
            protected MapperWrapper wrapMapper(MapperWrapper next) {
                return new MapperWrapper(next) {
                    @Override
                    public boolean shouldSerializeMember(Class definedIn,
                            String fieldName) {
                        // the goal of this is to ignore missing fields
                        if (definedIn == Object.class) {
                            // skip the missing
                            LOG.warn("Skipping missing field: {}", fieldName);
                            return false;
                        }

                        if (super.shouldSerializeMember(definedIn, fieldName)) {
                            loadedFields.add(fieldName);
                            return true;
                        } else {
                            return false;
                        }
                    }
                };
            }
        };
    }

    @Override
    public <T> T createInstance(Class<T> clazz) throws SerializationXmlFailure {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            LOG.error("Failed to create instance", e);
            throw new SerializationXmlFailure(e);
        }
    }

    @Override
    public synchronized <T> T convert(Class<T> clazz, String string) throws SerializationXmlFailure {
        this.xstream.setClassLoader(clazz.getClassLoader());

        if (string == null || string.isEmpty()) {
            return null;
        }
        // clear the skip list
        loadedFields.clear();
        T object = null;
        // convert
        final byte[] bytes = string.getBytes(Charset.forName("UTF-8"));
        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
                ObjectInputStream objIn = xstream
                .createObjectInputStream(byteIn);) {

            final Object objectTemp = objIn.readObject();
            object = (T) objectTemp;
        } catch (IOException | ClassNotFoundException e) {
            throw new SerializationXmlFailure(e);
        }
        // load missing values from
        if (loadedFields.size() < clazz.getDeclaredFields().length) {
            final LinkedList<String> toCopy = new LinkedList<>();
            final Field[] declaredFields = clazz.getDeclaredFields();
            for (Field field : declaredFields) {

                final int modifiers = field.getModifiers();
                // we do not set static or final
                if (Modifier.isFinal(modifiers) || Modifier.isStatic(modifiers)) {
                    continue;
                }

                if (loadedFields.contains(field.getName())) {
                    // ok, has been loaded
                } else {
                    // has not been loaded, we have to copy it
                    toCopy.add(field.getName());
                }
            }
            final T objectDefault = createInstance(clazz);
            copyFields(clazz, objectDefault, object, toCopy);
        }
        return object;
    }

    @Override
    public synchronized <T> String convert(T object) throws SerializationXmlFailure {
        if (object == null) {
            return null;
        }

        this.xstream.setClassLoader(object.getClass().getClassLoader());

        byte[] result = null;
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream()) {
            // use XStream for serialisation
            try (ObjectOutputStream objOut = xstream.createObjectOutputStream(
                    byteOut)) {
                objOut.writeObject(object);
            }
            result = byteOut.toByteArray();
        } catch (IOException e) {
            throw new SerializationXmlFailure("Can't serialize object.", e);
        }
        return new String(result, Charset.forName("UTF-8"));
    }

    @Override
    public void addAlias(Class<?> clazz, String alias) {
        this.xstream.alias(alias, clazz);
    }

    /**
     * Copy values of certain fields from source to target.
     *
     * @param <T>
     * @param clazz
     * @param source
     * @param target
     * @param fieldNames Names of fields to copy.
     */
    protected <T> void copyFields(Class<T> clazz, T source, T target,
            List<String> fieldNames) {
        for (String fieldName : fieldNames) {
            try {
                final PropertyDescriptor descriptor = new PropertyDescriptor(
                        fieldName, clazz);

                final Method readMethod = descriptor.getReadMethod();
                final Method writeMethod = descriptor.getWriteMethod();

                if (readMethod == null) {
                    LOG.warn("Missing getter for {}.{}", clazz.getSimpleName(),
                            fieldName);
                    continue;
                }
                if (writeMethod == null) {
                    LOG.warn("Missing setter for {}.{}", clazz.getSimpleName(),
                            fieldName);
                    continue;
                }
                // get from default
                Object value = readMethod.invoke(source);
                // set to object
                writeMethod.invoke(target, value);
            } catch (IntrospectionException ex) {
                LOG.error("Failed to set value for: {}.{} ",
                        clazz.getSimpleName(), fieldName, ex);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                LOG.error("Failed to set value for: {}.{} ",
                        clazz.getSimpleName(), fieldName, ex);
            }
        }
    }

}
