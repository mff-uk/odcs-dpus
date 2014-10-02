package cz.cuni.mff.xrg.uv.boost.dpu.config.test;

import cz.cuni.mff.xrg.uv.boost.dpu.advanced.DpuAdvancedBase;
import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigManager;
import cz.cuni.mff.xrg.uv.boost.dpu.config.MasterConfigObject;
import cz.cuni.mff.xrg.uv.service.serialization.xml.SerializationXmlFactory;
import cz.cuni.mff.xrg.uv.service.serialization.xml.SerializationXmlFailure;
import cz.cuni.mff.xrg.uv.service.serialization.xml.SerializationXmlGeneral;
import java.util.Collections;

/**
 * Convert given configuration into string. Result string then can be used
 * to configure DPUs.
 *
 * @author Škoda Petr
 */
public class ConfigToString {

    private ConfigToString() {
        
    }

    /**
     * Convert given class into a main DPUs configuration string.
     * @param <C>
     * @param config
     * @return
     * @throws cz.cuni.mff.xrg.uv.service.serialization.xml.SerializationXmlFailure
     */
    public static <C> String convert(C config) throws SerializationXmlFailure {
        SerializationXmlGeneral serialization = SerializationXmlFactory.serializationXmlGeneral();
        MasterConfigObject masterConfig = new MasterConfigObject();        
        ConfigManager configManager = new ConfigManager(serialization,
                Collections.EMPTY_LIST);
        configManager.setMasterConfig(masterConfig);
        configManager.set(config, DpuAdvancedBase.DPU_CONFIG_NAME);
        return serialization.convert(masterConfig);
    }

}
