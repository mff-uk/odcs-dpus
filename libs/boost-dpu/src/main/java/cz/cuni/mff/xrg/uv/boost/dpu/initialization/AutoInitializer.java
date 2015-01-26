package cz.cuni.mff.xrg.uv.boost.dpu.initialization;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

import cz.cuni.mff.xrg.uv.boost.dpu.context.Context;
import eu.unifiedviews.dpu.DPUException;

/**
 * Based on annotation performs DPU initialization.
 *
 * @author Škoda Petr
 */
public class AutoInitializer {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Init {

        /**
         *
         * @return String parameter.
         */
        String param();

    }

    /**
     * Interface for objects that can be initialized.
     */
    public interface Initializable {

        /**
         * Called to set initial properties. Context is not ready to be used besides some special functions.
         * 
         * @param context
         * @param param
         * @throws DPUException
         */
        public void init(Context context, String param) throws DPUException;

    }

    /**
     * Use can register this call back to be notified whenever field is set during initialization.
     */
    public interface FieldSetListener {
        
        /**
         * Called on every field that has been set.
         * 
         * @param field
         * @param value 
         */
        public void onField(Field field, Object value);
        
    }

    private final List<FieldSetListener> listeners = new LinkedList<>();

    /**
     * Initialize given object.
     *
     * @param object
     * @param context Context used during initialization.
     * @throws DPUException
     */
    public void init(Object object, Context context) throws DPUException {
        final List<Field> fields = scanForFields(object.getClass(), Init.class);
        for (Field field : fields) {
            initField(field, object, context);
        }
    }

    /**
     * Register callback.
     *
     * @param callback
     */
    public void addCallback(FieldSetListener callback) {
        this.listeners.add(callback);
    }

    /**
     * Return fields with given annotation.
     *
     * @param <T>
     * @param clazz
     * @param annotationClass
     * @return All field with given annotation.
     */
    private <T extends Annotation >List<Field> scanForFields(Class<?> clazz, Class<T> annotationClass) {
        final Field[] allFields = clazz.getFields();
        final List<Field> result = new LinkedList<>();
        for (Field field : allFields) {
            if (field.getAnnotation(annotationClass) != null) {
                result.add(field);
            }
        }
        return result;
    }

    /**
     * Initialize a single field on given object.
     *
     * @param field
     * @param object
     * @param context
     */
    private void initField(Field field, Object object, Context context) throws DPUException {
        final Init annotation = field.getAnnotation(Init.class);
        if (annotation == null) {
            throw new DPUException("Missing annotation for: " + field.getName());
        }
        if (!Initializable.class.isAssignableFrom(field.getType())) {
            throw new DPUException("Init annotation miss used for non Initializable type, field: "
                    + field.getName());            
        }
        // Create object instance.
        final Initializable fieldValue;
        try {
            fieldValue = (Initializable)field.getType().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new DPUException(e);
        }
        // Call init.
        fieldValue.init(context, annotation.param());
        // Set new value to given object.
        if (field.getModifiers() == Modifier.PUBLIC) {
            // It's public we set it directly.
            try {
                field.set(object, fieldValue);
            } catch (IllegalAccessException | IllegalArgumentException ex) {
                throw new DPUException("Can't set public field: " + field.getName() + " with class: "
                        + fieldValue.getClass().getSimpleName(), ex);
            }
        } else {
            // It's private or protected we need to use setter.
            final PropertyDescriptor fieldDesc;
            try {
                fieldDesc = new PropertyDescriptor(field.getName(), object.getClass());
            } catch (IntrospectionException ex) {
                throw new DPUException("Can't get property descriptor for: " + field.getName(), ex);
            }
            final Method fieldSetter = fieldDesc.getWriteMethod();
            try {
                fieldSetter.invoke(object, fieldValue);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                throw new DPUException("Can't set field: " + field.getName() + " with class: "
                        + fieldValue.getClass().getSimpleName(), ex);
            }
        }
        // Notify others.
        for (FieldSetListener listener : listeners) {
            listener.onField(field, fieldValue);
        }
    }

}
