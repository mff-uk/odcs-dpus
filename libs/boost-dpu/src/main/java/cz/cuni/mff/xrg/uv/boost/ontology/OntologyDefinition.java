package cz.cuni.mff.xrg.uv.boost.ontology;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;

import eu.unifiedviews.dpu.DPUException;

/**
 * Base class for ontology definition.
 *
 * Sample {@link #load(java.lang.Class)} method call, this code should be located in
 * {@code
 * <pre>
 * @Override
 * protected void loadExternal() throws DPUException {
 *  try {
 *      load(cz.cuni.mff.xrg.uv.transformer.xslt.XsltTOntology.class);
 *  } catch (Exception ex) {
 *      // Do nothing here. Ontology is just not available, default values will be used.
 *  }
 *  try {
 *      // Use another try-vatch block to load next depedency.
 *      load(cz.cuni.mff.xrg.uv.transformer.xslt.XsltTOntology.class);
 *  } catch (DPUException ex) {
 *      throw ex;
 *  } catch (Throwable ex) {
 *      // Do nothing here. Ontology is just not available, default values will be used.
 *  }
 * }
 * </pre> }
 *
 * Use conditions:
 * <ul>
 * <li>This class assumes that all public static fields are of type string.</li>
 * <li>External annotation should be used only on on public static non-final string fields.</li>
 * </ul>
 *
 * TODO Petr: Implements test!
 *
 * @author Škoda Petr
 */
public class OntologyDefinition {

    /**
     * Can be used only on public static non-final fields.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface External {

        /**
         *
         * @return Full path to field. The value of given field may be loaded into this variable.
         */
        String path();

    }

    public OntologyDefinition() {
        
    }

    /**
     * Store initialized URIs.
     */
    private final Map<String, URI> dictionary = new HashMap<>();

    /**
     * Call this to load {@link OntologyDefinition} from other DPU. Call of this function will require address
     * to other DPU project. As given DPU may not be available this call can throw
     * {@link java.lang.ClassNotFoundException}.
     *
     * So wrap this function into try-catch block. See class documentation for example.
     *
     * @param clazz
     * @throws DPUException
     * @throws ReflectiveOperationException
     */
    public void load(Class<?> clazz) throws DPUException, ReflectiveOperationException {
        final String className = clazz.getCanonicalName();
        // Scan for annotations with this class.
        for (Field field : this.getClass().getFields()) {            
            final External annotation = field.getAnnotation(External.class);
            if (annotation == null) {
                continue;
            }
            final String path = annotation.path();
            if (!path.startsWith(className)) {
                // It's not from this class.
                continue;
            }
            // Copy value.
            final String sourceFieldName = path.substring(className.length() + 1);
            try {
                final Field sourceField = clazz.getField(sourceFieldName);
                // Get and set.
                field.set(null, sourceField.get(null));
            } catch (NoSuchFieldException | SecurityException |
                    IllegalAccessException | IllegalArgumentException ex) {
                throw new DPUException("Can't copy field: " + sourceFieldName, ex);
            }
        }
    }

    /**
     * Prepare URIs for later use. This function should be called before first usage
     * of {@link #get(java.lang.String)} and after lass call of {@link #load(java.lang.Class)}.
     * 
     * @param valueFactory
     * @throws DPUException
     */
    public void init(ValueFactory valueFactory) throws DPUException {
        // Load dependencies.
        loadExternal();
        // Load fields.
        for (Field field : this.getClass().getFields()) {
            final String fieldValue;
            try {
                fieldValue = (String)field.get(field);
            } catch (IllegalAccessException | IllegalArgumentException ex) {
                throw new DPUException("Can't read field value.", ex);
            }
            try {
                dictionary.put(fieldValue, valueFactory.createURI(fieldValue));
            } catch (IllegalArgumentException ex) {
                throw new DPUException("Current value: " + fieldValue + " is not a valid URI for field" +
                        field.getName(), ex);
            }
        }
    }

    /**
     *
     *
     * @param uri Value must be public static member of the ontology class.
     * @return URI for given ontology URI.
     */
    public URI get(String uri) {
        return dictionary.get(uri);
    }

    /**
     * Use this to load external ontologies.
     *
     * @throws DPUException
     */
    protected void loadExternal() throws DPUException  {
        // No external sources here.
    }

}
