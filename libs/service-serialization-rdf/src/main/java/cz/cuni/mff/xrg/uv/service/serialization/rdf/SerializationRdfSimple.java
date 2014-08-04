package cz.cuni.mff.xrg.uv.service.serialization.rdf;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.slf4j.LoggerFactory;

/**
 * Very simple rdf serialisation class for string and integers.
 * 
 * The null values are not serialised.
 * 
 * @author Škoda Petr
 * @param <T>
 */
public class SerializationRdfSimple<T> implements SerializationRdf<T> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(
            SerializationRdfSimple.class);

    private final Class<T> clazz;

    SerializationRdfSimple(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public void convert(List<Statement> rdf, T object) throws SerializationRdfFailure {
        
        for (Statement statement : rdf) {
            if (Ontology.P_VALUE.compareTo(statement.getPredicate()
                    .stringValue()) == 0) {
                final String valueStr = statement.getObject().stringValue();
                String fieldName = statement.getSubject().stringValue();
                // take the string after last /
                fieldName = fieldName.substring(fieldName.lastIndexOf("/") + 1);
                // ok set
                try {
                    final PropertyDescriptor desc
                            = new PropertyDescriptor(fieldName, clazz);
                    final Method writeMethod = desc.getWriteMethod();
                    final Field field = clazz.getDeclaredField(fieldName);

                    switch(field.getType().getName()){
                        case "java.lang.String": 
                            writeMethod.invoke(object, valueStr);
                            break;
                        case "java.lang.Integer":
                            writeMethod.invoke(object, Integer.parseInt(valueStr));
                        default:
                            LOG.info("Unknown: {}", field.getType().getName());
                    }
                } catch (IntrospectionException | IllegalAccessException | 
                        IllegalArgumentException | InvocationTargetException | 
                        NoSuchFieldException | SecurityException ex) {
                    LOG.warn("Failed to set {}.{}", clazz.getSimpleName(),
                            fieldName, ex);
                }

            }
        }
    }

    @Override
    public List<Statement> convert(T object, URI rootUri,
            ValueFactory valueFactory) throws SerializationRdfFailure {
        final URI o_type_property
                = valueFactory.createURI(Ontology.O_TYPE_PROPERTY);
        final URI p_value = valueFactory.createURI(Ontology.P_VALUE);
        final URI p_has_property
                = valueFactory.createURI(Ontology.P_HAS_PROPERTY);
        final URI p_rdf_type = valueFactory.createURI(Ontology.P_RDF_TYPE);

        final List<Statement> result = new LinkedList<>();
        // add info about serialized object to main uri
        result.add(valueFactory.createStatement(rootUri, p_rdf_type,
                valueFactory.createURI(Ontology.O_TYPE_CLASS)));
        result.add(valueFactory.createStatement(rootUri,
                valueFactory.createURI(Ontology.P_VERSION),
                valueFactory.createLiteral("1.0.0")));
        
        final Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            // try store single field
            final String fieldName = field.getName();
            final String valueStr;
            try {

                final PropertyDescriptor desc
                        = new PropertyDescriptor(fieldName, clazz);
                final Method readMethod = desc.getReadMethod();
                if (readMethod == null) {
                    LOG.warn("Missing getter for {}.{}", clazz.getSimpleName(),
                            fieldName);
                    continue;
                }
                final Object value = readMethod.invoke(object);
                if (value == null) {
                    // skip 
                    continue;
                }                
                valueStr = value.toString();
            } catch (IntrospectionException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                LOG.warn("Failed to serialize {}.{}", clazz.getSimpleName(),
                        fieldName, ex);
                continue;
            }
            // add triple
            final URI propertyNode = valueFactory.createURI(
                    Ontology.PREFIX_PROPERTY + fieldName);
            // bind to root
            result.add(valueFactory.createStatement(rootUri,
                    p_has_property, propertyNode));
            // type
            result.add(valueFactory.createStatement(propertyNode, p_rdf_type,
                    o_type_property));
            // value name
            result.add(valueFactory.createStatement(propertyNode, p_value,
                    valueFactory.createLiteral(valueStr)));
        }

        return result;
    }

}
