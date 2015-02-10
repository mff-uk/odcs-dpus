package cz.cuni.mff.xrg.uv.boost.test;

import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import cz.cuni.mff.xrg.uv.boost.dpu.advanced.AbstractDpu;
import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigException;
import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigManager;
import cz.cuni.mff.xrg.uv.boost.dpu.config.MasterConfigObject;
import cz.cuni.mff.xrg.uv.boost.serialization.SerializationFailure;
import cz.cuni.mff.xrg.uv.boost.serialization.SerializationXml;
import cz.cuni.mff.xrg.uv.boost.serialization.SerializationXmlFactory;
import cz.cuni.mff.xrg.uv.boost.serialization.SerializationXmlFailure;

/**
 *
 * @author Škoda Petr
 */
public class ConfigurationBuilder {

    private final SerializationXml serialization = SerializationXmlFactory.serializationXml();

    private final MasterConfigObject masterConfig = new MasterConfigObject();

    private final ConfigManager configManager = new ConfigManager(serialization);

    public ConfigurationBuilder() {
        try {
            configManager.setMasterConfig(masterConfig);
        } catch (ConfigException ex) {
            throw new RuntimeException("Can't set master configuration.", ex);
        }
    }

    public ConfigurationBuilder setDpuConfiguration(Object configuration) {
        configManager.set(configuration, AbstractDpu.DPU_CONFIG_NAME);
        return this;
    }

    @Override
    public String toString() {
        try {
            return serialization.convert(masterConfig);
        } catch (SerializationFailure | SerializationXmlFailure ex) {
            throw new RuntimeException("Can't serialize configuration.", ex);
        }
    }

}
