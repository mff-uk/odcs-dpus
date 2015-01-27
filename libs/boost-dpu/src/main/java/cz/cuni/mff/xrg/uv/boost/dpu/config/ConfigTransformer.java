package cz.cuni.mff.xrg.uv.boost.dpu.config;

/**
 * Can be used to transform configuration before it's loaded by DPU/add-on.
 *
 * @author Škoda Petr
 */
public interface ConfigTransformer {

    /**
     * Configure add-on before any other non {@link ConfigTransformerAddon} or DPU is configured.
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
