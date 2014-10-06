package cz.cuni.mff.xrg.uv.boost.dpu.addon;

import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigException;
import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigManager;

/**
 * Can be used to transform configuration before it's loaded.
 *
 * @author Škoda Petr
 */
public interface ConfigTransformerAddon extends Addon {

    /**
     * Configure add-on.
     *
     * @param configManager
     * @throws ConfigException 
     */
    void configure(ConfigManager configManager) throws ConfigException;

    /**
     * Transform configuration on string level, before it's serialized as
     * an object.
     *
     * @param configName
     * @param config
     * @return
     * @throws cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigException
     */
    String transformString(String configName, String config) throws ConfigException;

    /**
     * Can transform configuration object.
     *
     * @param <TYPE>
     * @param configName
     * @param config
     * @throws cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigException
     */
    <TYPE> void transformObject(String configName, TYPE config) throws ConfigException;

}
