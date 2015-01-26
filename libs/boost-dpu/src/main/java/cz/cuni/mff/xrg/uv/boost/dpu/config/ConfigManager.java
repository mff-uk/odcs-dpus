package cz.cuni.mff.xrg.uv.boost.dpu.config;

import java.util.LinkedList;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.ConfigTransformerAddon;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.uv.boost.dpu.config.serializer.ConfigSerializer;
import cz.cuni.mff.xrg.uv.boost.dpu.config.serializer.XStreamSerializer;
import cz.cuni.mff.xrg.uv.boost.serialization.SerializationXml;

/**
 * Provide access to multiple configurations (strings) under name (string).
 *
 * @author Škoda Petr
 */
public class ConfigManager {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigManager.class);

    /**
     * Master configuration object.
     */
    private MasterConfigObject masterConfig = null;

    /**
     * Add-ons that are used to transform configuration before it's loaded.
     */
    private final List<ConfigTransformerAddon> configTransformers;

    /**
     * Class used to serialise configurations.
     */
    private final List<ConfigSerializer> configSerializers;

    /**
     *
     * @param serializer
     * @param configTransformers
     */
    public ConfigManager(SerializationXml serializer, List<ConfigTransformerAddon> configTransformers) {
        // Configure serializer class.
        serializer.addAlias(MasterConfigObject.class, MasterConfigObject.TYPE_NAME);
        // Init object.
        this.configTransformers = configTransformers;
        // Use xStream as default.
        this.configSerializers = new LinkedList<>();
        this.configSerializers.add(new XStreamSerializer(serializer));
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
        return get(name, ConfigHistory.createNoHistory(clazz));
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
        // Transform string before deserialization, deserialize and transform output object.
        strValue = transformString(name, strValue);
        final TYPE configObject = configHistory.parse(strValue, configSerializers);
        transformObject(name, configObject);
        return configObject;
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
            throw new RuntimeException("Can't set configuration to null master configuration!");
        }
        // Convert and put into configuration.
        final String objectAsStr = serialize(object, name);
        masterConfig.getConfigurations().put(name, objectAsStr);
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
        LOG.debug("createNew({})", clazz);
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new RuntimeException("Can't create configuration object.", ex);
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
        MasterConfigObject newMasterConfig = null;
        // Try to deserialize.
        for (ConfigSerializer serializer : configSerializers) {
            // We use special name for master configuration object.
            if (serializer.canDeserialize(masterConfigStr, MasterConfigObject.TYPE_NAME)) {
                newMasterConfig = serializer.deserialize(masterConfigStr, MasterConfigObject.class);
                if (newMasterConfig != null) {
                    // Success, we can stop trying.
                    break;
                }
            }
        }
        if (newMasterConfig == null) {
            throw new ConfigException("No serializer can deserialize master configuration object.");
        }
        this.masterConfig = newMasterConfig;
        // We got new configuration, configure add-ons.
        configureAddons();
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

    /**
     * Serialize given object.
     *
     * @param <TYPE>
     * @param object
     * @param name
     * @return
     */
    private <TYPE> String serialize(TYPE object, String name) {
        for (ConfigSerializer serializer : configSerializers) {
            final String objectAsString = serializer.serialize(object);
            if (objectAsString != null) {
                return objectAsString;
            }
        }
        return null;
    }

}
