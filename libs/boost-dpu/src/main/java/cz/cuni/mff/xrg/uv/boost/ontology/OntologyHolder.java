package cz.cuni.mff.xrg.uv.boost.ontology;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;

import eu.unifiedviews.dpu.DPUException;

/**
 * Holds ontology. Load interface with public String fields and prepare URI for of of them. Then this
 * class can be asked for translation by {@link #get(java.lang.String)} method.
 *
 * <b>Use conditions</b>
 * <ul>
 * <li>This class assumes that all public static fields are of type string.</li>
 * <li>As the values of filed can change base on presence of other DPUs in the system, do not store them.!</li>
 * <li>In case of OntologyDefinition.UpdateFrom annotation is used, proper method must be called.</li>
 * </ul>
 *
 * <b>Updating ontology</b>
 * In order to OntologyDefinition.UpdateFrom annotation takes effect, a method must be called at the 
 * DPU construct. Find the sample call of this function bellow:
 * We assume that we update only from a single ontology class
 * "cz.cuni.mff.xrg.uv.transformer.xslt.XsltTOntology", if more then one source is used the code must be
 * duplicated for each source. Also in order to work code must be located in DPU's constructor!
 * {@code
 * <pre>
 * 
 * </pre>
 * }
 * Project that is used only as a source of soft update (above) should be marked as a optional in pom.xml.
 *
 * @author Škoda Petr
 */
public class OntologyHolder {

    private class UriDefinition {
        
        /**
         * Value of OntologyDefinition.UpdateFrom if presented.
         */
        private final String updateFromPath;

        /**
         * URI as string.
         */
        private String uriAsString;

        /**
         * URI as URI, translation of {@link #uriAsString}.
         */
        private URI uri = null;

        public UriDefinition(String updateFromPath, String uriAsString) {
            this.updateFromPath = updateFromPath;
            this.uriAsString = uriAsString;
        }
        
    }

    public OntologyHolder() {

    }

    /**
     * Store initialized URIs. Mapping is from original values.
     */
    private final Map<String, UriDefinition> storage = new HashMap<>();


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
            final String updateFromPath;
            OntologyDefinition.UpdateFrom updateFrom = field.getAnnotation(OntologyDefinition.UpdateFrom.class);
            if (updateFrom == null) {
                updateFromPath = null;
            } else {
                updateFromPath = updateFrom.path();
            }
            // Add to the list.
            storage.put(key, new UriDefinition(updateFromPath, uri));
        }
    }

    /**
     * Update loaded ontology from given target.
     *
     * @param clazz
     * @throws DPUException
     */
    public void updateFromSource(Class<?> clazz) throws DPUException {
        final String className = clazz.getCanonicalName();
        for (UriDefinition definition : storage.values()) {
            if (definition.updateFromPath == null) {
                // Does not update from enywhere.
            } else if (definition.updateFromPath.startsWith(className)) {
                final String sourceFieldName = definition.updateFromPath.substring(className.length() + 1);
                try {
                    final Field sourceField = clazz.getField(sourceFieldName);
                    definition.uriAsString = (String)sourceField.get(null);
                } catch (NoSuchFieldException | SecurityException |
                        IllegalAccessException | IllegalArgumentException ex) {
                    throw new DPUException("Can't copy field: " + sourceFieldName, ex);
                }
            }
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
        for (UriDefinition definition : storage.values()) {
            definition.uri = valueFactory.createURI(definition.uriAsString);
        }
    }

    /**
     *
     *
     * @param uri Value must be public static member of the ontology class.
     * @return URI for given ontology URI.
     * @throws DPUException If given URI is not part of the ontology.
     */
    public URI get(String uri) {
        if (storage.containsKey(uri)) {
            return storage.get(uri).uri;
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
