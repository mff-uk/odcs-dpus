package cz.cuni.mff.xrg.uv.boost.dpu.config;

import cz.cuni.mff.xrg.uv.service.serialization.xml.SerializationXmlFailure;
import cz.cuni.mff.xrg.uv.service.serialization.xml.SerializationXmlGeneral;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide access to multiple configurations (strings) under name (string).
 *
 * @author Škoda Petr
 */
public class ConfigManager {

    private static final Logger LOG = LoggerFactory.getLogger(
            ConfigManager.class);

    private final MasterConfigObject masterConfig;

    private final SerializationXmlGeneral serializer;

    public ConfigManager() {
        this.masterConfig = null;
        this.serializer = null;
    }

    /**
     *
     * @param masterConfig Can be null.
     * @param serializer
     */
    public ConfigManager(MasterConfigObject masterConfig,
            SerializationXmlGeneral serializer) {
        this.masterConfig = masterConfig;
        this.serializer = serializer;
    }

    /**
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
        final String strValue = masterConfig.getConfigurations().get(name);
        if (strValue == null) {
            LOG.trace("get({}, ...) -> null as no value found", name);
            return null;
        }

        try {
            return serializer.convert(clazz, strValue);
        } catch (SerializationXmlFailure ex) {
            throw new ConfigException("Serialization failed", ex);
        }
    }

    public <TYPE> TYPE get(String name, ConfigHistory<TYPE> configHistory)
            throws ConfigException {
        if (masterConfig == null) {
            return null;
        }
        final String strValue = masterConfig.getConfigurations().get(name);
        if (strValue == null) {
            return null;
        }
        try {
            return configHistory.parse(strValue, serializer);
        } catch (SerializationXmlFailure ex) {
            throw new ConfigException("Serialization failed", ex);
        }
    }

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

    public <TYPE> TYPE createNew(Class<TYPE> clazz) throws ConfigException {
        try {
            return serializer.createInstance(clazz);
        } catch (SerializationXmlFailure ex) {
            throw new RuntimeException("Serialization failed.", ex);
        }
    }

    public MasterConfigObject getMasterConfig() {
        return masterConfig;
    }

}
