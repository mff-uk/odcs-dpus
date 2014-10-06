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
import cz.cuni.mff.xrg.uv.service.serialization.rdf.SerializationRdf;
import cz.cuni.mff.xrg.uv.service.serialization.rdf.SerializationRdfFactory;
import cz.cuni.mff.xrg.uv.service.serialization.rdf.SerializationRdfFailure;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dpu.config.DPUConfigException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashMap;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide possibility to configure DPU with Rdf data.
 *
 * @author Škoda Petr
 */
public class ConfigurationFromRdf implements
        ConfigurableAddon<ConfigurationFromRdf.Configuration_V1>, ConfigTransformerAddon {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationFromRdf.class);

    public static final String USED_CONFIG_NAME = "addon/configurationFromRdf";

    public static final String ADDON_NAME = "Configure from Rdf";

    public static class ObjectConfiguration_V1 {

        private String mainUri;

        public ObjectConfiguration_V1() {
        }

        public String getMainUri() {
            return mainUri;
        }

        public void setMainUri(String mainUri) {
            this.mainUri = mainUri;
        }

    }

    public static class Configuration_V1 {

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

    public class VaadinDialog extends AddonVaadinDialogBase<Configuration_V1> {

        public final ObjectConfiguration_V1 dpuConfig = new ObjectConfiguration_V1();

        public VaadinDialog() {
            super(Configuration_V1.class);
        }

        @Override
        public void buildLayout() {
            final VerticalLayout mainLayout = new VerticalLayout();
            mainLayout.setSizeFull();
            mainLayout.setSpacing(true);
            mainLayout.setMargin(true);

            final TextField txtMainUri = new TextField("Main uri:",
                    new MethodProperty<String>(dpuConfig, "mainUri"));
            txtMainUri.setWidth("100%");
            mainLayout.addComponent(txtMainUri);

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
                dpuConfig.setMainUri(source.getMainUri());
            }
        }

        @Override
        protected Configuration_V1 getConfiguration() throws DPUConfigException {
            final Configuration_V1 conf = new Configuration_V1();
            conf.getConfig().put(DpuAdvancedBase.DPU_CONFIG_NAME, dpuConfig);
            return conf;
        }

    }

    private DpuAdvancedBase.Context context;

    private Configuration_V1 configuration;

    private final SerializationRdf serialization
            = SerializationRdfFactory.serializationRdfSimple(Object.class);

    private RDFDataUnit configRdfDataUnit = null;

    /**
     * Name of RDFDataUnit field that should be used to configure DPU.
     */
    private final String configRdfDataUnitName;

    private final ValueFactory valueFactory = new ValueFactoryImpl();

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
        // get info
        gatherInformations();
        // create dialog
        return new VaadinDialog();
    }

    @Override
    public void init(DpuAdvancedBase.Context context) throws AddonException {
        this.context = context;
        // get RDFDataUnit
        try {
            final Field field = context.getDpu().getClass().getField(configRdfDataUnitName);
            if (context.getDpu() == null) {
                throw new AddonException("Dpu is NULL.");
            }
            final Object obj = field.get(context.getDpu());
            if (obj == null) {
                LOG.info("Given field is null, name: '{}'", configRdfDataUnitName);
                return;
            }

            if (obj instanceof RDFDataUnit) {
                configRdfDataUnit = (RDFDataUnit) obj;
            } else {
                throw new AddonException("Given field is not instance of RDFDataUnit, name: '" +
                        configRdfDataUnitName + "' class." + obj.getClass().getSimpleName());
            }
        } catch (IllegalAccessException | IllegalArgumentException |
                NoSuchFieldException | SecurityException ex) {
            throw new AddonException("Can't get given field.", ex);
        }
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

        if (configuration.getConfig().containsKey(configName)) {
            ObjectConfiguration_V1 c = configuration.getConfig().get(configName);
            // update
            try {
                serialization.rdfToObject(configRdfDataUnit, valueFactory.createURI(c.getMainUri()), config);
            } catch (SerializationRdfFailure ex) {
                throw new ConfigException("Can't deserialize configuration.", ex);
            }
        }
    }

    /**
     * Gather informations about other add-ons and dpu's configuration.
     */
    private void gatherInformations() {
//        final List<AddonInitializer.AddonInfo> addons = context.getAddons();
//        for (AddonInitializer.AddonInfo addonInfo : addons) {
//            if (addonInfo.getAddon() instanceof ConfigurableAddon) {
//                final Class<?> cnfClass = ((ConfigurableAddon) addonInfo.getAddon()).getConfigClass();
//                // get information about class
//            }
//        }
    }

}
