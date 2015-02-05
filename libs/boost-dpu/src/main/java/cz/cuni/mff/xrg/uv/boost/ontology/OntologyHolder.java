package cz.cuni.mff.xrg.uv.boost.ontology;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.dpu.DPUException;

/**
 * Base class for ontology definition.
 *
 * Sample {@link #load(java.lang.Class)} method call, this code should be located in  {@code
 * <pre>
 * @Override
 * protected void loadExternal() throws DPUException {
 *  try {
 *      load(cz.cuni.mff.xrg.uv.transformer.xslt.XsltTOntology.class);
 *  } catch (Exception ex) {
 *      // Do nothing here. Ontology is just not available, default values will be used.
 *  }
 *  try {
 *      // Use another try-catch block to load next dependency.
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
 * <li>As the values can change base on presence of other DPUs in the system, do not store them.!</li>
 * </ul>
 *
 * TODO Petr: Implements test! TODO Petr: Should we initialize the target instance to enable chaining?
 *
 * @author Škoda Petr
 */
public class OntologyHolder {

    private static final Logger LOG = LoggerFactory.getLogger(OntologyHolder.class);

    public OntologyHolder() {

    }

    /**
     * Store initialized URIs. Mapping is from original values.
     */
    private final Map<String, URI> uris = new HashMap<>();

    /**
     * Store mapping from initial values to loaded ones. Used as a temporary storage before {@link #uris} is
     * initialized.
     */
    private final Map<String, String> translations = new HashMap<>();

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
    public void loadDefinitions(Class<?> clazz) throws DPUException {
        // Scan for annotations with this class.
        for (Field field : clazz.getFields()) {
            if (field.getAnnotation(OntologyDefinition.NotUri.class) != null) {
                // Skip as it's not a URI.
                continue;
            }
            final String key;
            // Key is the same value as in static field.
            try {
                key = (String) field.get(null);
            } catch (IllegalAccessException | IllegalArgumentException ex) {
                throw new DPUException("Can't read field: " + field.getName(), ex);
            }
            // Read a value for a key.
            final String uri;
            try {
                uri = (String) field.get(null);
            } catch (IllegalAccessException | IllegalArgumentException ex) {
                throw new DPUException("Can't read field: " + field.getName(), ex);
            }

            // TODO Petr Solve external annotation here!

            // Add to the list.
            translations.put(key, uri);

//            final OntologyDefinition.External annotation = field.getAnnotation(OntologyDefinition.External.class);
//            if (annotation == null) {
//                continue;
//            }
//            final String path = annotation.path();
//            if (!path.startsWith(className)) {
//                // It's not from this class.
//                continue;
//            }
//            // Copy value.
//            final String sourceFieldName = path.substring(className.length() + 1);
//            try {
//                final Field sourceField = clazz.getField(sourceFieldName);
//                // Store in translation list.
//                translations.put((String)field.get(null), (String)sourceField.get(null));
//            } catch (NoSuchFieldException | SecurityException |
//                    IllegalAccessException | IllegalArgumentException ex) {
//                throw new DPUException("Can't copy field: " + sourceFieldName, ex);
//            }
        }
    }

    /**
     * Prepare URIs for later use. This function should be called before first usage of
     * {@link #get(java.lang.String)} and after lass call of {@link #load(java.lang.Class)}.
     *
     * @param valueFactory
     * @throws DPUException
     */
    public void init(ValueFactory valueFactory) throws DPUException {
        // Load dependencies.
        loadExternal();
        // Load fields.
        for (String key : translations.keySet()) {
            uris.put(key, valueFactory.createURI(translations.get(key)));
        }
    }

    /**
     *
     *
     * @param uri Value must be public static member of the ontology class.
     * @return URI for given ontology URI.
     * @throws DPUException
     */
    public URI get(String uri) {
        if (uris.containsKey(uri)) {
            return uris.get(uri);
        } else {
            throw new RuntimeException("Missing URI for: " + uri);
        }
    }

    /**
     * Use this to load external ontologies.
     *
     * @throws DPUException
     */
    protected void loadExternal() throws DPUException {
        // No external sources here.
    }

}
