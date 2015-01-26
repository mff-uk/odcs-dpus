package cz.cuni.mff.xrg.uv.boost.dpu.addon.impl;

import com.vaadin.data.util.MethodProperty;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonException;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.ConfigTransformerAddon;
import cz.cuni.mff.xrg.uv.boost.dpu.advanced.DpuAdvancedBase;
import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigException;
import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigManager;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.AddonVaadinDialogBase;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.ConfigurableAddon;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dpu.config.DPUConfigException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.AdvancedVaadinDialogBase;
import cz.cuni.mff.xrg.uv.boost.serialization.rdf.SerializationRdf;
import cz.cuni.mff.xrg.uv.boost.serialization.rdf.SerializationRdfFactory;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.helpers.dataunit.rdfhelper.RDFHelper;

/**
 * Provide possibility to configure DPU with RDF data from {@link RDFDataUnit}. Currently support update of
 * only the main DPU's configuration.
 *
 * Known limitations:
 * <ul>
 * <li>Does not support cycles in classes, if configuration structure must be a tree.</li>
 * <li>Maps are not supported.</li>
 * </ul>
 *
 * See UK-E-HttpDownload for sample usage.
 *
 * <b>Experimental add-on!!</b>
 *
 * @see cz.cuni.mff.xrg.uv.boost.dpu.addonAddon
 * @author Škoda Petr
 */
public class ConfigurationFromRdf implements
        ConfigurableAddon<ConfigurationFromRdf.Configuration_V1>, ConfigTransformerAddon {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationFromRdf.class);

    public static final String USED_CONFIG_NAME = "addon/configurationFromRdf";

    public static final String ADDON_NAME = "Configure from Rdf";

    /**
     * Represents configuration for a single "named configuration" ie. configuration that should be edited.
     */
    public static class ObjectConfiguration_V1 {

        /**
         * Subject used as root for configuration. Can be null to disable the configuration process.
         */
        private String mainSubjectClass = null;

        /**
         * Prefix used for non-binded configuration properties.
         */
        private String ontologyUriPrefix = "http://unifiedviews.eu/ontology/config/";

        /**
         * Translation-binding for configuration properties. Store (URI, binding), where binding is property
         * name.
         */
        private HashMap<String, String> binding = new LinkedHashMap<>();

        public ObjectConfiguration_V1() {
        }

        public String getMainSubjectClass() {
            return mainSubjectClass;
        }

        public void setMainSubjectClass(String mainSubjectClass) {
            this.mainSubjectClass = mainSubjectClass;
        }

        public String getOntologyUriPrefix() {
            return ontologyUriPrefix;
        }

        public void setOntologyUriPrefix(String ontologyUriPrefix) {
            this.ontologyUriPrefix = ontologyUriPrefix;
        }

        public HashMap<String, String> getBinding() {
            return binding;
        }

        public void setBinding(HashMap<String, String> binding) {
            this.binding = binding;
        }

    }

    /**
     * Add-ons configuration.
     */
    public static class Configuration_V1 {

        /**
         * Store configurations under configuration names.
         */
        private HashMap<String, ObjectConfiguration_V1> config = new LinkedHashMap<>();

        public Configuration_V1() {

        }

        public HashMap<String, ObjectConfiguration_V1> getConfig() {
            return config;
        }

        public void setConfig(HashMap<String, ObjectConfiguration_V1> config) {
            this.config = config;
        }

    }

    /**
     * Configuration dialog.
     */
    public class VaadinDialog extends AddonVaadinDialogBase<Configuration_V1> {

        public final ObjectConfiguration_V1 dpuConfig = new ObjectConfiguration_V1();

        /**
         * Table with property to URI binding.
         */
        public Table table;

        public VaadinDialog() {
            super(Configuration_V1.class);
        }

        @Override
        public void buildLayout() {
            final VerticalLayout mainLayout = new VerticalLayout();
            mainLayout.setSizeFull();
            mainLayout.setSpacing(true);
            mainLayout.setMargin(true);

            final Label description = new Label("'uri' in table must be full and valid URI."
                    + "Leave the text field blank to use auto value = property name, where '.' are replaced with '/', prefixed by 'base ontology prefix'.</br>"
                    + "'Binding configuraion.uri' must be unique, ie. is not possible to set two properties from a single statement.",
                    ContentMode.HTML);

            mainLayout.addComponent(description);

            final TextField txtRootClass = new TextField("Configuration class:",
                    new MethodProperty<String>(dpuConfig, "mainSubjectClass"));
            txtRootClass.setWidth("100%");
            mainLayout.addComponent(txtRootClass);

            final TextField txtOntologyPrefix = new TextField("Base ontology prefix:",
                    new MethodProperty<String>(dpuConfig, "ontologyUriPrefix"));
            txtOntologyPrefix.setWidth("100%");
            mainLayout.addComponent(txtOntologyPrefix);

            // Generate table for binding.
            table = new Table("Binding configuration:");
            table.setSizeFull();
            table.addContainerProperty("name", Label.class, null);
            table.addContainerProperty("uri", TextField.class, null);
            mainLayout.addComponent(table);
            // Add lines ..
            for (String fieldName : dpuConfigurationInfo) {
                TextField txtURL = new TextField();
                txtURL.setWidth("100%");
                txtURL.setNullRepresentation("");
                txtURL.setNullSettingAllowed(true);
                table.addItem(new Object[]{new Label(fieldName), txtURL}, fieldName);
            }
            setCompositionRoot(mainLayout);
        }

        @Override
        protected String getConfigClassName() {
            return USED_CONFIG_NAME;
        }

        @Override
        protected void setConfiguration(Configuration_V1 conf) throws DPUConfigException {
            if (conf.getConfig().containsKey(DpuAdvancedBase.DPU_CONFIG_NAME)) {
                ObjectConfiguration_V1 source = conf.getConfig().get(DpuAdvancedBase.DPU_CONFIG_NAME);
                // Create reverse hash map.
                Map<String, String> reverseMap = new HashMap<>();
                for (String key : source.getBinding().keySet()) {
                    reverseMap.put(source.getBinding().get(key), key);
                }
                // Load values into table.
                for (String fieldName : dpuConfigurationInfo) {
                    final TextField txtURI = (TextField) table.getItem(fieldName)
                            .getItemProperty("uri").getValue();
                    txtURI.setValue(reverseMap.get(fieldName));
                }
                // Load other values.
                dpuConfig.setMainSubjectClass(source.getMainSubjectClass());
                dpuConfig.setOntologyUriPrefix(source.getOntologyUriPrefix());
            }
        }

        @Override
        protected Configuration_V1 getConfiguration() throws DPUConfigException {
            // Save data from table into dpuConfig.
            dpuConfig.setBinding(new HashMap<String, String>());
            for (String fieldName : dpuConfigurationInfo) {
                final TextField txtURI = (TextField) table.getItem(fieldName)
                        .getItemProperty("uri").getValue();
                final String uri = txtURI.getValue();
                if (uri != null && !uri.isEmpty()) {
                    if (dpuConfig.getBinding().containsKey(uri)) {
                        throw new DPUConfigException("Duplicit uri detected in " + getCaption() + ".");
                    } else {
                        dpuConfig.getBinding().put(uri, fieldName);
                    }
                }
            }
            // Store dpuConfig to configuration and return.
            final Configuration_V1 conf = new Configuration_V1();
            conf.getConfig().put(DpuAdvancedBase.DPU_CONFIG_NAME, dpuConfig);
            return conf;
        }

    }

    /**
     * Configuration.
     */
    private Configuration_V1 configuration;

    /**
     * Provides conversion from RDF into limited POJO.
     */
    private final SerializationRdf serialization = SerializationRdfFactory.rdfSimple();

    /**
     * Source of triples with configurations.
     */
    private RDFDataUnit configRdfDataUnit = null;

    /**
     * Name of RDFDataUnit field that should be used to configure DPU.
     */
    private final String configRdfDataUnitName;

    /**
     * Factory for RDF related objects.
     */
    private final ValueFactory valueFactory = new ValueFactoryImpl();

    /**
     * Holds list of field names in DPU's configuration class.
     */
    private List<String> dpuConfigurationInfo = null;

    public ConfigurationFromRdf(String configRdfDataUnitName) {
        this.configRdfDataUnitName = configRdfDataUnitName;
    }

    @Override
    public Class<Configuration_V1> getConfigClass() {
        return Configuration_V1.class;
    }

    @Override
    public String getDialogCaption() {
        return ADDON_NAME;
    }

    @Override
    public AddonVaadinDialogBase<Configuration_V1> getDialog() {
        // Create dialog. The dialog utilize previously created knowledge.
        return new VaadinDialog();
    }

    @Override
    public void init(DpuAdvancedBase.Context context) throws AddonException {
        // Get RDFDataUnit that will be used as a source for triples.
        try {
            final Field field = context.getDpu().getClass().getField(configRdfDataUnitName);
            if (context.getDpu() == null) {
                throw new AddonException("Dpu is NULL.");
            }
            final Object obj = field.get(context.getDpu());
            if (obj == null) {
                LOG.info("Given field is null, name: '{}' -> no RDF configuration.", configRdfDataUnitName);
                return;
            }

            if (obj instanceof RDFDataUnit) {
                configRdfDataUnit = (RDFDataUnit) obj;
            } else {
                throw new AddonException("Given field is not instance of RDFDataUnit, name: '"
                        + configRdfDataUnitName + "' class." + obj.getClass().getSimpleName());
            }
        } catch (IllegalAccessException | IllegalArgumentException |
                NoSuchFieldException | SecurityException ex) {
            throw new AddonException("Can't get given field.", ex);
        }
    }

    @Override
    public void init(AdvancedVaadinDialogBase.Context context) {
        // Gather information about DPU's configuration.
        dpuConfigurationInfo = gatherInformations(context.getConfigHistory().getFinalClass());
    }

    @Override
    public void configure(ConfigManager configManager) throws ConfigException {
        configuration = configManager.get(USED_CONFIG_NAME, Configuration_V1.class);
        if (configuration == null) {
            configuration = new Configuration_V1();
        }
    }

    @Override
    public String transformString(String configName, String config) {
        return config;
    }

    @Override
    public <TYPE> void transformObject(String configName, TYPE config) throws ConfigException {
        if (configRdfDataUnit == null) {
            return;
        }
        // Should we transform/update this configuration/object?
        if (configuration.getConfig().containsKey(configName)) {
            // Get configuration for given object.
            final ObjectConfiguration_V1 c = configuration.getConfig().get(configName);
            if (c == null) {
                LOG.warn("Null value stored in configuration under '{}'.", configName);
                return;
            }
            // Prepare configuration for conversion.
            final SerializationRdf.Configuration serializerConfig = new SerializationRdf.Configuration();
//            serializerConfig.setOntologyPrefix(c.getOntologyUriPrefix());
//            serializerConfig.getPropertyMap().putAll(c.getBinding());
            // Get subjects of required type.
            if (c.mainSubjectClass == null || c.mainSubjectClass.isEmpty()) {
                // No configuration class is set, ignore.
                return;
            }
            final URI subjectClass = valueFactory.createURI(c.mainSubjectClass);
            // Get first subject.
            List<Resource> resources = getSubjectsOfClass(configRdfDataUnit, subjectClass);
            if (resources.isEmpty()) {
                // No configuration object of our class.
                return;
            }
            // Use the first object to configure.
//            try {
//                serialization.rdfToObject(configRdfDataUnit, resources.get(0), config, serializerConfig);
//            } catch (SerializationRdfFailure ex) {
//                throw new ConfigException("Can't deserialize configuration.", ex);
//            }
        }
    }

    /**
     * Gather informations about DPU's configuration.
     *
     * @param clazz Class to gather info about.
     * @return Field names in given class and sub classes.
     */
    private List<String> gatherInformations(Class<?> clazz) {
        final List<String> fieldList = new ArrayList<>(10);
        for (Field field : clazz.getDeclaredFields()) {
            // Not a primitive type?
            LOG.info("Field: {}", field.getName());
            LOG.info("\tclass: {}", field.getType().getName());
            LOG.info("\tisSynthetic: {}", field.isSynthetic());
            if (field.isSynthetic() || Collection.class.isAssignableFrom(field.getType())) {
                final List<String> subFieldList = gatherInformations(getUsableFieldType(field));
                final String fieldName = field.getName();
                for (String value : subFieldList) {
                    fieldList.add(fieldName + "." + value);
                }
            }
            // In every case add the mapping for the given value.
            fieldList.add(field.getName());
        }
        return fieldList;
    }

    /**
     *
     * @param field
     * @return Field type or inner type in case of collection.
     */
    private Class<?> getUsableFieldType(Field field) {
        Class<?> clazz = field.getType();
        if (Collection.class.isAssignableFrom(clazz)) {
//            clazz = FieldTypeGetter.getCollectionGenericType(field.getGenericType());
        }
        return clazz;
    }

    /**
     *
     * @param dataUnit
     * @param subjectClass
     * @return Subject of given class.
     */
    private List<Resource> getSubjectsOfClass(RDFDataUnit dataUnit, URI subjectClass) {
        RepositoryConnection connection = null;
        RepositoryResult<Statement> repoResult = null;
        final List<Resource> result = new ArrayList<>(10);
        try {
            connection = configRdfDataUnit.getConnection();
//            repoResult = connection.getStatements(null,
//                    valueFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
//                    subjectClass, true, RDFHelper.getGraphsURIArray(dataUnit));
            while (repoResult.hasNext()) {
                result.add(repoResult.next().getSubject());
            }
        } catch (DataUnitException | RepositoryException ex) {
            LOG.error("Can't obtain configuration subjects.", ex);
            return Collections.EMPTY_LIST;
        } finally {
            if (repoResult != null) {
                try {
                    repoResult.close();
                } catch (RepositoryException ex) {
                    LOG.warn("Close method failed.", ex);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (RepositoryException ex) {
                    LOG.warn("Close method failed.", ex);
                }
            }
        }
        return result;
    }

}
