package cz.cuni.mff.xrg.uv.boost.dpu.config;

import cz.cuni.mff.xrg.uv.boost.dpu.addon.ConfigTransformerAddon;
import cz.cuni.mff.xrg.uv.service.serialization.xml.SerializationXmlFailure;
import cz.cuni.mff.xrg.uv.service.serialization.xml.SerializationXmlGeneral;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide access to multiple configurations (strings) under name (string).
 *
 * @author Škoda Petr
 */
public class ConfigManager {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigManager.class);

    private MasterConfigObject masterConfig = null;

    private final SerializationXmlGeneral serializer;

    /**
     * Add-ons that are used to transform configuration before it's loaded.
     */
    private final List<ConfigTransformerAddon> configTransformers;

    /**
     *
     * @param serializer
     * @param configTransformers
     */
    public ConfigManager(SerializationXmlGeneral serializer, List<ConfigTransformerAddon> configTransformers) {
        this.serializer = serializer;
        this.configTransformers = configTransformers;
    }

    /**
     * Get configuration of given class.
     *
     * @param <TYPE>
     * @param name
     * @param clazz
     * @return Null if configuration is not presented.
     * @throws cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigException
     */
    public <TYPE> TYPE get(String name, Class<TYPE> clazz) throws ConfigException {
        if (masterConfig == null) {
            LOG.trace("get({}, ...) -> null as masterConfig is null", name);
            return null;
        }
        String strValue = masterConfig.getConfigurations().get(name);
        if (strValue == null) {
            LOG.trace("get({}, ...) -> null as no value found", name);
            return null;
        }
        strValue = transformString(name, strValue);
        try {
            final TYPE configObject = serializer.convert(clazz, strValue);
            transformObject(name, configObject);
            return configObject;
        } catch (SerializationXmlFailure ex) {
            throw new ConfigException("Serialization failed", ex);
        }
    }

    /**
     * Get configuration for given configuration history, ie. auto convert older configuration
     * into the newer version.
     *
     * @param <TYPE>
     * @param name
     * @param configHistory
     * @return
     * @throws ConfigException
     */
    public <TYPE> TYPE get(String name, ConfigHistory<TYPE> configHistory) throws ConfigException {
        if (masterConfig == null) {
            LOG.trace("get({}, ...) -> null as masterConfig is null", name);
            return null;
        }
        String strValue = masterConfig.getConfigurations().get(name);
        if (strValue == null) {
            LOG.trace("get({}, ...) -> null as no value found", name);
            return null;
        }
        strValue = transformString(name, strValue);
        try {
            final TYPE configObject = configHistory.parse(strValue, serializer);
            transformObject(name, configObject);
            return configObject;
        } catch (SerializationXmlFailure ex) {
            throw new ConfigException("Serialization failed", ex);
        }
    }

    /**
     * Store configuration string under given name to master configuration.
     *
     * @param <TYPE>
     * @param object
     * @param name
     */
    public <TYPE> void set(TYPE object, String name) {
        if (masterConfig == null) {
            return;
        }

        try {
            final String objectAsStr = serializer.convert(object);
            // put into configuration
            masterConfig.getConfigurations().put(name, objectAsStr);
        } catch (SerializationXmlFailure ex) {
            throw new RuntimeException("Serialization failed.", ex);
        }
    }

    /**
     * Create new instance of configuration of given type.
     *
     * @param <TYPE>
     * @param clazz
     * @return
     * @throws ConfigException
     */
    public <TYPE> TYPE createNew(Class<TYPE> clazz) throws ConfigException {
        try {
            return serializer.createInstance(clazz);
        } catch (SerializationXmlFailure ex) {
            throw new RuntimeException("Serialization failed.", ex);
        }
    }

    /**
     *
     * @param masterConfig New master configuration, ie. dictionary of other configurations.
     * @throws ConfigException
     */
    public void setMasterConfig(MasterConfigObject masterConfig) throws ConfigException {
        this.masterConfig = masterConfig;
        configureAddons();
    }

    /**
     * Try to set {@link MasterConfigObject} from string. Apply {@link #configTransformers} on a string form
     * of a given {@link MasterConfigObject}.
     *
     * @param masterConfigStr
     * @throws cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigException
     */
    public void setMasterConfig(String masterConfigStr) throws ConfigException {
        masterConfigStr = transformString(MasterConfigObject.CONFIG_NAME, masterConfigStr);
        try {
            masterConfig = serializer.convert(MasterConfigObject.class, masterConfigStr);
        } catch (SerializationXmlFailure | RuntimeException ex) {
            throw new ConfigException("Conversion of master config failed.", ex);
        }
        // We got new configuration, configure add-ons.
        configureAddons();
        
        // TODO update : move into addon ~ can be used to load DPU configuration as a root
//        try {
//            // parseconfiguration
//            final MasterConfigObject masterConfig
//                    = this.masterContext.serializationXml.convert(
//                            MasterConfigObject.class, config);
//            // wrap inside ConfigManager
//            this.masterContext.configManager = createConfigManager(masterConfig);
//        } catch (SerializationXmlFailure ex) {
//            throw new DPUConfigException("Conversion failed.", ex);
//        } catch (java.lang.ClassCastException e) {
//            // try direct conversion
//            try {
//                final CONFIG dpuConfig = masterContext.serializationXml.convert(
//                        masterContext.configHistory.getFinalClass(), config);
//                final MasterConfigObject masterConfig = new MasterConfigObject();
//                masterContext.configManager = createConfigManager(masterConfig);
//
//                if (masterContext.configHistory == null) {
//                    masterContext.configManager.set(dpuConfig, DPU_CONFIG_NAME);
//                } else {
//                    masterContext.configManager.set(dpuConfig, DPU_CONFIG_NAME);
//                }
//            } catch (SerializationXmlFailure ex) {
//                throw new DPUConfigException("Conversion failed for prime class", ex);
//            }
//        }

    }

    /**
     *
     * @return Master configuration object.
     */
    public MasterConfigObject getMasterConfig() {
        return masterConfig;
    }

    /**
     * Transform given configuration by {@link #configTransformers}.
     *
     * @param name Configuration name.
     * @param value Configuration as string.
     * @return
     * @throws ConfigException
     */
    private String transformString(String name, String value) throws ConfigException{
        for (ConfigTransformerAddon addon : configTransformers) {
            value = addon.transformString(name, value);
        }
        return value;
    }

    /**
     * Transform given configuration by {@link #configTransformers}.
     *
     * @param <TYPE>
     * @param name Configuration name.
     * @param value Configuration as object.
     * @throws ConfigException
     */
    private <TYPE> void transformObject(String name, TYPE value) throws ConfigException{
        for (ConfigTransformerAddon addon : configTransformers) {
            addon.transformObject(name, value);
        }
    }

    /**
     * Load configuration into add-ons. So they are ready to be used.
     * 
     * @throws ConfigException
     */
    private void configureAddons() throws ConfigException {
        for (ConfigTransformerAddon addon : configTransformers) {
            addon.configure(this);
        }
    }

}
