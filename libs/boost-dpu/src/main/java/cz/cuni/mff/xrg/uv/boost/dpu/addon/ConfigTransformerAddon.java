package cz.cuni.mff.xrg.uv.boost.dpu.addon;

/**
 * Can be used to transform configuration before it's loaded.
 *
 * @author Škoda Petr
 */
public interface ConfigTransformerAddon extends Addon {

    /**
     * Transform configuration on string level, before it's serialized as
     * an object.
     *
     * @param configName
     * @param config
     * @return
     */
    String transformString(String configName, String config);

    /**
     * Can transform configuration object.
     *
     * @param <TYPE>
     * @param configName
     * @param config
     */
    <TYPE> void transformObject(String configName, TYPE config);

}
