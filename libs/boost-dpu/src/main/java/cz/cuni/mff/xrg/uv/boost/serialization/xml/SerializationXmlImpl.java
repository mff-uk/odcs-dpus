package cz.cuni.mff.xrg.uv.boost.serialization.xml;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.mapper.MapperWrapper;

import cz.cuni.mff.xrg.uv.boost.serialization.SerializationUtils;
import cz.cuni.mff.xrg.uv.boost.serialization.SerializationFailure;

/**
 *
 * @author Škoda Petr
 */
class SerializationXmlImpl implements SerializationXml {
 
    private static final Logger LOG = LoggerFactory.getLogger(SerializationXmlImpl.class);

    protected final XStream xStream;

    protected Class<?> loadedMainClass;

    /**
     * List of field loaded during object conversion by xStream.
     */
    protected final LinkedList<String> loadedFields = new LinkedList<>();

    SerializationXmlImpl() {
        // Create modified version of xStream.
        this.xStream = new XStream(new DomDriver("UTF-8")) {
            @Override
            protected MapperWrapper wrapMapper(MapperWrapper next) {
                return new MapperWrapper(next) {
                    @Override
                    public boolean shouldSerializeMember(Class definedIn, String fieldName) {
                        // The goal of this is to ignore missing fields.
                        if (definedIn == Object.class) {
                            // Skip the missing values (ie. deleted fields).
                            LOG.info("Skipping missing field: {}", fieldName);
                            return false;
                        }
                        if (super.shouldSerializeMember(definedIn, fieldName)) {
                            if (loadedMainClass == definedIn) {
                                // Support only 1. level setting.
                                loadedFields.add(fieldName);
                            } else {
                            }
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
    public synchronized <T> T convert(Class<T> clazz, String string)
            throws SerializationFailure, SerializationXmlFailure {
        // Clear the list of skipped fields and set main class.
        loadedMainClass = clazz;
        loadedFields.clear();
        T object = (T) convert(clazz.getClassLoader(), string);
        if (object == null) {
            return null;
        }
        // Load missing values from default instance.
        if (loadedFields.size() < clazz.getDeclaredFields().length) {
            final LinkedList<String> toCopy = new LinkedList<>();
            final Field[] declaredFields = clazz.getDeclaredFields();
            for (Field field : declaredFields) {
                final int modifiers = field.getModifiers();
                // We do not set static or final.
                if (Modifier.isFinal(modifiers) || Modifier.isStatic(modifiers)) {
                    continue;
                }
                if (!loadedFields.contains(field.getName())) {
                    // Filed has not been loaded, we have to copy it.
                    toCopy.add(field.getName());
                }
            }
            final T objectDefault = SerializationUtils.createInstance(clazz);
            copyFields(clazz, objectDefault, object, toCopy);
        }
        return object;
    }

    private Object convert(ClassLoader classLoader, String string) throws SerializationXmlFailure {
        // Check for empty input.
        if (string == null || string.isEmpty()) {
            return null;
        }
        // Set class so xStream use proper clas loader.
        this.xStream.setClassLoader(classLoader);
        // Conversion.
        final byte[] bytes = string.getBytes(Charset.forName("UTF-8"));
        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
                ObjectInputStream objIn = xStream.createObjectInputStream(byteIn);) {
            return objIn.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new SerializationXmlFailure(e);
        }
    }

    @Override
    public synchronized <T> String convert(T object) throws SerializationXmlFailure {
        // Check for empty input.
        if (object == null) {
            return null;
        }
        // Set class so xStream use proper clas loader.
        this.xStream.setClassLoader(object.getClass().getClassLoader());
        // Conversion.
        byte[] result = null;
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream()) {
            // Use XStream for serialisation.
            try (ObjectOutputStream objOut = xStream.createObjectOutputStream(byteOut)) {
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
        this.xStream.alias(alias, clazz);
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
    protected <T> void copyFields(Class<T> clazz, T source, T target, List<String> fieldNames) {
        for (String fieldName : fieldNames) {
            try {
                final PropertyDescriptor descriptor = new PropertyDescriptor(fieldName, clazz);
                final Method readMethod = descriptor.getReadMethod();
                final Method writeMethod = descriptor.getWriteMethod();
                if (readMethod == null) {
                    LOG.warn("Missing getter for {}.{}", clazz.getSimpleName(), fieldName);
                    continue;
                }
                if (writeMethod == null) {
                    LOG.warn("Missing setter for {}.{}", clazz.getSimpleName(), fieldName);
                    continue;
                }
                // Copy value.
                final Object value = readMethod.invoke(source);
                writeMethod.invoke(target, value);
                // Read value from target back.
                final Object valueCheck = readMethod.invoke(target);
                // Check the result.
                if (valueCheck == null) {
                    if (value != null) {
                        LOG.error("{} : Target value is null but source not!", fieldName);
                    } else {
                        // Both values are equal.
                    }
                } else if (valueCheck.equals(value)) {
                    // Both values are equal.
                } else {
                    LOG.error("{} : Not equals! source: {} target: {}", fieldName, value, valueCheck);

                }

            } catch (IntrospectionException ex) {
                LOG.error("Failed to set value for: {}.{} ", clazz.getSimpleName(), fieldName, ex);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                LOG.error("Failed to set value for: {}.{} ", clazz.getSimpleName(), fieldName, ex);
            }
        }
    }
}
